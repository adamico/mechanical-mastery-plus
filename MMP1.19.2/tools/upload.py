#!/usr/bin/env python3
# Upload the released <version>: the zip tools/release.sh exported, checked against the sha256 its
# tag carries, goes to CurseForge with its server pack, and to the tag's GitHub release, with that
# version's changelog section as its notes.
#
#   tools/upload.py <version> [--dry-run] [--site curseforge|server|github] [--client-file-id <id>]
#
# Each site goes on its own: a failure on one leaves the others, and --site retries just one. A site
# that already has the version is refused, since a published version never changes.
#
# The server pack is server_files/ as tagged, with its ServerStarter config pointed at the client
# zip on CurseForge's CDN, so it is uploaded as a child of the client file once that exists, and
# attached to the GitHub release beside the client zip. On their own, --site server and --site github
# need that file's id, which the CurseForge upload prints: --client-file-id. Servers updated from git
# copy server_files/ as committed, so once the client file is up, its url is written into the repo's
# config and committed, to push after the upload.
#
# CurseForge takes $CURSEFORGE_TOKEN, an upload API token, which is never printed. When it is
# missing, the script runs itself again through `op run --env-file=tools/upload.env`, which fills
# it in from 1Password for that run only. $CURSEFORGE_PROJECT_ID overrides the pack's project.
# GitHub goes through `gh`, and needs the tag pushed first. --dry-run prints what it would send and
# contacts nothing.
import hashlib
import io
import json
import os
import re
import shlex
import shutil
import subprocess
import sys
import tempfile
import tomllib
import urllib.error
import urllib.parse
import urllib.request
import uuid
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TAG_PREFIX = "mmp1.19.2-v"
SLUG = "mechanical-mastery-plus"
CURSEFORGE_PROJECT = "914505"
CURSEFORGE_UPLOAD = "https://minecraft.curseforge.com"
# The upload API can't list a project's files, so the website's own listing, which needs no key, does.
CURSEFORGE_API = "https://www.curseforge.com"
REPO = "adamico/mechanical-mastery-plus"
SERVER_CONFIG = "server-setup-config.yaml"


class Refused(Exception):
    pass


class Published(Refused):
    """The site already has the version, so there is nothing to retry."""


def fail(message):
    sys.exit(f"upload: {message}")


def run(*args):
    return subprocess.run(args, cwd=ROOT, capture_output=True, text=True)


def send(method, url, headers, body=None):
    request = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request) as response:
            return json.loads(response.read() or b"null")
    except urllib.error.HTTPError as error:
        raise Refused(f"{method} {url} answered {error.code}: {error.read().decode(errors='replace')[:500]}")
    except urllib.error.URLError as error:
        raise Refused(f"{method} {url} failed: {error.reason}")


def multipart(fields):
    boundary = uuid.uuid4().hex
    body = b""
    for name, filename, content_type, data in fields:
        disposition = f'form-data; name="{name}"' + (f'; filename="{filename}"' if filename else "")
        body += (f"--{boundary}\r\nContent-Disposition: {disposition}\r\n"
                 f"Content-Type: {content_type}\r\n\r\n").encode() + data + b"\r\n"
    return f"multipart/form-data; boundary={boundary}", body + f"--{boundary}--\r\n".encode()


def through_op(args):
    """Runs the script again under `op run` when the CurseForge token is missing, once."""
    env_file = ROOT / "tools/upload.env"
    if (os.environ.get("CURSEFORGE_TOKEN") or os.environ.get("UPLOAD_THROUGH_OP")
            or not env_file.is_file() or not shutil.which("op")):
        return
    os.environ["UPLOAD_THROUGH_OP"] = "1"
    sys.stdout.flush()
    os.execvp("op", ["op", "run", f"--env-file={env_file}", "--", sys.executable, __file__, *args])


def cdn_url(file_id, file_name):
    """Where CurseForge's CDN serves a file: its id split after four digits, without leading zeros.
    The older media.forgecdn.net host refuses every download now; mediafilez serves the same paths."""
    digits = str(file_id)
    return f"https://mediafilez.forgecdn.net/files/{int(digits[:4])}/{int(digits[4:])}/{file_name}"


def pointed_at(data, url):
    """The ServerStarter config with its modpackUrl set to <url>; refused unless it has exactly one."""
    data, found = re.subn(rb"(modpackUrl:\s*)\S+", lambda m: m.group(1) + url.encode(), data)
    if found != 1:
        raise Refused(f"server_files/{SERVER_CONFIG} has {found} modpackUrl lines; it needs exactly one.")
    return data


class Release:
    def __init__(self, version, dry_run, client_file_id):
        self.version, self.dry_run, self.client_file_id = version, dry_run, client_file_id
        self.tag = f"{TAG_PREFIX}{version}"
        pack = tomllib.loads((ROOT / "pack.toml").read_text())
        self.name = f"{pack['name']} {version}"
        self.server_name = f"{pack['name']} Server {version}"
        self.zip = ROOT / f"{SLUG}-{version}.zip"
        self.server_zip = f"{SLUG}-Server-{version}.zip"
        message = run("git", "tag", "-l", "--format=%(contents)", self.tag).stdout
        if not message:
            fail(f"{self.tag} doesn't exist; only a version tools/release.sh tagged is uploaded.")
        released = next((line.split()[-1] for line in message.splitlines() if line.startswith("zip sha256 ")), None)
        if not released:
            fail(f"{self.tag} names no zip sha256, so there is no zip to check against it.")
        if not self.zip.is_file():
            fail(f"{self.zip.name} is missing; a later export never matches the released zip, so it can't be rebuilt.")
        self.data = self.zip.read_bytes()
        if hashlib.sha256(self.data).hexdigest() != released:
            fail(f"{self.zip.name} isn't the zip {self.tag} released (sha256 {released}).")
        # The tag's own pack.toml, whatever HEAD has moved on to.
        tagged = tomllib.loads(run("git", "show", f"{self.tag}:./pack.toml").stdout)
        self.minecraft = tagged["versions"]["minecraft"]
        self.notes = run(sys.executable, str(ROOT / "tools/changelog.py"), "notes", version)
        if self.notes.returncode:
            fail(self.notes.stderr.strip())
        self.notes = self.notes.stdout.strip()
        self.agent = f"{REPO}/{version}"

    def described(self):
        return f"{self.zip.name} ({len(self.data)} bytes)"

    def project(self):
        return os.environ.get("CURSEFORGE_PROJECT_ID", CURSEFORGE_PROJECT)

    def urls(self):
        upload = os.environ.get("CURSEFORGE_UPLOAD_URL", CURSEFORGE_UPLOAD).rstrip("/")
        api = os.environ.get("CURSEFORGE_API_URL", CURSEFORGE_API).rstrip("/")
        return upload, f"{api}/api/v1/mods/{self.project()}/files"

    def upload_headers(self):
        if not os.environ.get("CURSEFORGE_TOKEN"):
            raise Refused("$CURSEFORGE_TOKEN is not set, and tools/upload.env didn't fill it in through op run.")
        return {"X-Api-Token": os.environ["CURSEFORGE_TOKEN"], "User-Agent": self.agent}

    def upload_file(self, upload, headers, metadata, file_name, data):
        content_type, body = multipart([("metadata", None, "application/json", json.dumps(metadata).encode()),
                                        ("file", file_name, "application/zip", data)])
        return send("POST", f"{upload}/api/projects/{self.project()}/upload-file",
                    {**headers, "Content-Type": content_type}, body)

    def curseforge(self):
        upload, files = self.urls()
        api_headers = {"User-Agent": self.agent, "Accept": "application/json"}
        # Every upload is a release: CurseForge's app only offers a pack that has one.
        metadata = {"changelog": self.notes, "changelogType": "markdown",
                    "displayName": self.name, "releaseType": "release"}
        if self.dry_run:
            print(f"GET {files}")
            print(f"GET {upload}/api/game/version-types and {upload}/api/game/versions, "
                  f"for the ids of Minecraft {self.minecraft} and Forge")
            print(f"POST {upload}/api/projects/{self.project()}/upload-file")
            print(f"  metadata: {json.dumps({**metadata, 'gameVersions': f'<ids of {self.minecraft}, Forge>'}, indent=2)}")
            print(f"  file: {self.described()}")
            print(f"Then point server_files/{SERVER_CONFIG} at the client file's CDN url and commit it.")
            self.server()
            return
        headers = self.upload_headers()
        # A published version is final on CurseForge: it is never replaced.
        if {self.zip.name, self.name} & self.curseforge_files(files, api_headers):
            raise Published(f"CurseForge already has {self.version}, and a published version never changes.")
        metadata["gameVersions"] = self.curseforge_game_versions(upload, headers)
        created = self.upload_file(upload, headers, metadata, self.zip.name, self.data)
        self.client_file_id = created.get("id")
        if not self.client_file_id:
            raise Refused(f"CurseForge took the upload but returned no file id: {created}")
        print(f"Uploaded {self.zip.name} to CurseForge as {self.version} ({self.client_file_id}); it shows once reviewed.")
        self.point_repo_server_config()
        try:
            self.server()
        except Refused as refused:
            raise Refused(f"{refused}\nThe client file is up; retry the server pack with "
                          f"--site server --client-file-id {self.client_file_id}")

    def server_pack(self):
        """server_files/ as tagged, zipped, with ServerStarter pointed at the client zip on the CDN."""
        url = cdn_url(self.client_file_id, self.zip.name) if self.client_file_id else "<the client file's CDN url>"
        paths = run("git", "ls-tree", "-r", "--name-only", self.tag, "--", "server_files/").stdout.splitlines()
        if not any(p.endswith(f"server_files/{SERVER_CONFIG}") for p in paths):
            raise Refused(f"{self.tag} has no server_files/{SERVER_CONFIG}.")
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, "w", zipfile.ZIP_DEFLATED) as out:
            for path in paths:
                data = subprocess.run(["git", "show", f"{self.tag}:./{path}"], cwd=ROOT, capture_output=True, check=True).stdout
                if path.endswith(SERVER_CONFIG):
                    data = pointed_at(data, url)
                out.writestr(path.removeprefix("server_files/"), data)
        return url, buffer.getvalue()

    def point_repo_server_config(self):
        """Servers updated from git copy server_files/ as committed, so it names the new zip too."""
        config = ROOT / "server_files" / SERVER_CONFIG
        url = cdn_url(self.client_file_id, self.zip.name)
        relative = f"server_files/{SERVER_CONFIG}"
        if run("git", "diff", "--quiet", "HEAD", "--", relative).returncode:
            print(f"upload: {relative} has local changes, so it isn't pointed at {url}; set its modpackUrl by hand.",
                  file=sys.stderr)
            return
        config.write_bytes(pointed_at(config.read_bytes(), url))
        committed = run("git", "commit", "-q", "-m", f"Server files install {self.name}", "--", relative)
        if committed.returncode:
            print(f"upload: couldn't commit {relative}: {committed.stderr.strip()}", file=sys.stderr)
            return
        print(f"Committed {relative} pointing at {url}; push it with: git push origin HEAD")

    def server(self):
        upload, files = self.urls()
        url, data = self.server_pack()
        metadata = {"changelog": self.notes, "changelogType": "markdown", "displayName": self.server_name,
                    "parentFileId": self.client_file_id or "<the client file's id>", "releaseType": "release"}
        if self.dry_run:
            print(f"POST {upload}/api/projects/{self.project()}/upload-file")
            print(f"  metadata: {json.dumps(metadata, indent=2)}")
            print(f"  file: {self.server_zip} ({len(data)} bytes), modpackUrl {url}")
            return
        if not self.client_file_id:
            raise Refused("the server pack needs the client file's id: --client-file-id <id>")
        headers = self.upload_headers()
        api_headers = {"User-Agent": self.agent, "Accept": "application/json"}
        if {self.server_zip, self.server_name} & self.curseforge_files(files, api_headers):
            raise Published(f"CurseForge already has the {self.version} server pack.")
        created = self.upload_file(upload, headers, metadata, self.server_zip, data)
        print(f"Uploaded {self.server_zip} to CurseForge ({created.get('id')}), under {self.client_file_id}.")

    def curseforge_files(self, url, headers):
        """The file and display names of every file the project has, across the listing's pages."""
        names, page_index, seen = set(), 0, 0
        while True:
            page = send("GET", f"{url}?{urllib.parse.urlencode({'pageIndex': page_index, 'pageSize': 50})}", headers)
            names |= {f.get("fileName") for f in page["data"]} | {f.get("displayName") for f in page["data"]}
            seen += len(page["data"])
            if not page["data"] or seen >= page["pagination"].get("totalCount", 0):
                return names
            page_index += 1

    def curseforge_game_versions(self, upload, headers):
        """The ids of the Minecraft version and of Forge."""
        types = send("GET", f"{upload}/api/game/version-types", headers)
        minecraft = {t["id"] for t in types if t["slug"].startswith("minecraft-")}
        loaders = {t["id"] for t in types if t["slug"] == "modloader"}
        versions = send("GET", f"{upload}/api/game/versions", headers)
        ids = []
        for name, kinds in [(self.minecraft, minecraft), ("Forge", loaders)]:
            found = [v["id"] for v in versions if v["name"] == name and v["gameVersionTypeID"] in kinds]
            if len(found) != 1:
                raise Refused(f"CurseForge names {len(found)} game versions {name}; it needs exactly one.")
            ids += found
        return ids

    def github(self):
        # The server pack is attached too; it names the client zip's CDN url, so it needs the file id.
        url, server_data = self.server_pack()
        with tempfile.TemporaryDirectory() as folder:
            server_zip = Path(folder) / self.server_zip
            server_zip.write_bytes(server_data)
            self.github_release(server_zip, url)

    def github_release(self, server_zip, url):
        create = ["gh", "release", "create", self.tag, str(self.zip), str(server_zip), "--repo", REPO,
                  "--verify-tag", "--title", self.name, "--notes-file", "-"]
        if self.dry_run:
            print(shlex.join(create))
            print(f"  notes: {len(self.notes)} characters from CHANGELOG.md; files: {self.described()}, "
                  f"{self.server_zip} ({server_zip.stat().st_size} bytes), modpackUrl {url}")
            return
        if not self.client_file_id:
            raise Refused("the server pack names the client file's CDN url, so it needs its id: --client-file-id <id>")
        if not shutil.which("gh"):
            raise Refused("gh is not installed.")
        remote = run("git", "ls-remote", "origin", f"refs/tags/{self.tag}^{{}}").stdout.split()
        local = run("git", "rev-parse", f"{self.tag}^{{commit}}").stdout.strip()
        if not remote or remote[0] != local:
            raise Refused(f"origin doesn't have {self.tag} as tagged here; push it first: git push origin {self.tag}")
        if run("gh", "release", "view", self.tag, "--repo", REPO).returncode == 0:
            raise Published(f"GitHub already has a release for {self.tag}, and a published version never changes.")
        created = subprocess.run(create, cwd=ROOT, input=self.notes, capture_output=True, text=True)
        if created.returncode:
            raise Refused(f"gh release create failed: {created.stderr.strip()}")
        print(f"Released {self.zip.name} and {self.server_zip} on GitHub: {created.stdout.strip()}")


# The server pack goes with the client file in a full run; on its own, it is a retry.
SITES = {"curseforge": ("CurseForge", Release.curseforge), "server": ("CurseForge server pack", Release.server),
         "github": ("GitHub", Release.github)}
FULL_RUN = ["curseforge", "github"]


def main(args):
    usage = ("usage: tools/upload.py <major.minor.patch> [--dry-run] [--site curseforge|server|github] "
             "[--client-file-id <id>]")
    dry_run = "--dry-run" in args
    rest = [a for a in args if a != "--dry-run"]
    sites, client_file_id = FULL_RUN, None
    if "--site" in rest:
        at = rest.index("--site")
        if at + 1 >= len(rest) or rest[at + 1] not in SITES:
            fail(usage)
        sites, rest = [rest[at + 1]], rest[:at] + rest[at + 2:]
    if "--client-file-id" in rest:
        at = rest.index("--client-file-id")
        if at + 1 >= len(rest) or not rest[at + 1].isdigit():
            fail(usage)
        client_file_id, rest = int(rest[at + 1]), rest[:at] + rest[at + 2:]
    if len(rest) != 1 or not all(p.isdigit() for p in rest[0].split(".")) or rest[0].count(".") != 2:
        fail(usage)

    release = Release(rest[0], dry_run, client_file_id)
    if {"curseforge", "server"} & set(sites) and not dry_run:
        through_op(args)
    failed, published = [], []
    for site in sites:
        name, upload = SITES[site]
        try:
            upload(release)
        except Refused as refused:
            print(f"upload: {name}: {refused}", file=sys.stderr)
            (published if isinstance(refused, Published) else failed).append(site)
    if published and not failed:
        sys.exit(1)
    if failed:
        done = "" if len(failed) == len(sites) else "; the other site is done"
        file_id = f" --client-file-id {release.client_file_id or '<id>'}" if {"server", "github"} & set(failed) else ""
        fail(f"retry with {' and '.join('--site ' + site for site in failed)}{file_id} once fixed{done}.")


if __name__ == "__main__":
    main(sys.argv[1:])
