# The sites a release is published to, CurseForge and GitHub, and packwiz, which exports it, each
# behind a small interface the Release module in tools/release.py uses. Each has a real adapter,
# a dry-run adapter that prints what the real one would send and contacts nothing, and a fake that
# the tests drive.
import json
import shlex
import shutil
import subprocess
import tomllib
import urllib.error
import urllib.parse
import urllib.request
import uuid
from pathlib import Path


class Refused(Exception):
    pass


class Published(Refused):
    """The site already has the version, and a published version never changes."""


# CurseForge: the names of the files a project has, the ids of a Minecraft version and Forge, and
# uploading a file with its metadata, which answers the new file's id.

class CurseForge:
    UPLOAD = "https://minecraft.curseforge.com"
    # The upload API can't list a project's files, so the website's own listing, which needs no key, does.
    API = "https://www.curseforge.com"

    def __init__(self, project, token, agent):
        self.project, self.token, self.agent = project, token, agent

    def headers(self):
        if not self.token:
            raise Refused("$CURSEFORGE_TOKEN is not set, and tools/publish.env didn't fill it in through op run.")
        return {"X-Api-Token": self.token, "User-Agent": self.agent}

    def file_names(self):
        """The file and display names of every file the project has, across the listing's pages."""
        url = f"{self.API}/api/v1/mods/{self.project}/files"
        headers = {"User-Agent": self.agent, "Accept": "application/json"}
        names, page_index, seen = set(), 0, 0
        while True:
            page = send("GET", f"{url}?{urllib.parse.urlencode({'pageIndex': page_index, 'pageSize': 50})}", headers)
            names |= {f.get("fileName") for f in page["data"]} | {f.get("displayName") for f in page["data"]}
            seen += len(page["data"])
            if not page["data"] or seen >= page["pagination"].get("totalCount", 0):
                return names
            page_index += 1

    def game_version_ids(self, minecraft):
        """The ids of the Minecraft version and of Forge."""
        headers = self.headers()
        types = send("GET", f"{self.UPLOAD}/api/game/version-types", headers)
        kinds = {"minecraft": {t["id"] for t in types if t["slug"].startswith("minecraft-")},
                 "Forge": {t["id"] for t in types if t["slug"] == "modloader"}}
        versions = send("GET", f"{self.UPLOAD}/api/game/versions", headers)
        ids = []
        for name, kind in [(minecraft, "minecraft"), ("Forge", "Forge")]:
            found = [v["id"] for v in versions if v["name"] == name and v["gameVersionTypeID"] in kinds[kind]]
            if len(found) != 1:
                raise Refused(f"CurseForge names {len(found)} game versions {name}; it needs exactly one.")
            ids += found
        return ids

    def upload(self, metadata, file_name, data):
        content_type, body = multipart([("metadata", None, "application/json", json.dumps(metadata).encode()),
                                        ("file", file_name, "application/zip", data)])
        created = send("POST", f"{self.UPLOAD}/api/projects/{self.project}/upload-file",
                       {**self.headers(), "Content-Type": content_type}, body)
        if not created or not created.get("id"):
            raise Refused(f"CurseForge took {file_name} but returned no file id: {created}")
        return created["id"]


class DryCurseForge:
    """Prints what CurseForge would be sent. It knows no files, and answers no file id."""

    def __init__(self, project, out=print):
        self.project, self.out = project, out

    def file_names(self):
        self.out(f"GET {CurseForge.API}/api/v1/mods/{self.project}/files")
        return set()

    def game_version_ids(self, minecraft):
        self.out(f"GET {CurseForge.UPLOAD}/api/game/version-types and {CurseForge.UPLOAD}/api/game/versions, "
                 f"for the ids of Minecraft {minecraft} and Forge")
        return f"<ids of {minecraft}, Forge>"

    def upload(self, metadata, file_name, data):
        self.out(f"POST {CurseForge.UPLOAD}/api/projects/{self.project}/upload-file")
        self.out(f"  metadata: {json.dumps(metadata, indent=2)}")
        self.out(f"  file: {file_name} ({len(data)} bytes)")
        return None


class FakeCurseForge:
    """A project in memory: it holds what was uploaded, and refuses the file names it is told to."""

    def __init__(self, existing=(), refusing=()):
        self.names, self.refusing, self.uploads = set(existing), set(refusing), {}

    def file_names(self):
        return set(self.names)

    def game_version_ids(self, minecraft):
        return [1, 2]

    def upload(self, metadata, file_name, data):
        if file_name in self.refusing:
            raise Refused(f"CurseForge refused {file_name}")
        file_id = 9338333 + len(self.uploads)
        self.uploads[file_id] = (metadata, file_name, data)
        self.names |= {file_name, metadata["displayName"]}
        return file_id


# GitHub: whether origin has a tag as tagged here, whether the tag has a release, and creating one.

class GitHub:
    def __init__(self, repo, root):
        self.repo, self.root = repo, root

    def run(self, *args, **kwargs):
        return subprocess.run(args, cwd=self.root, capture_output=True, text=True, **kwargs)

    def tag_pushed(self, tag):
        remote = self.run("git", "ls-remote", "origin", f"refs/tags/{tag}^{{}}").stdout.split()
        local = self.run("git", "rev-parse", f"{tag}^{{commit}}").stdout.strip()
        return bool(remote) and remote[0] == local

    def has_release(self, tag):
        if not shutil.which("gh"):
            raise Refused("gh is not installed.")
        return self.run("gh", "release", "view", tag, "--repo", self.repo).returncode == 0

    def create_release(self, tag, title, notes, files):
        created = self.run("gh", "release", "create", tag, *map(str, files), "--repo", self.repo, "--verify-tag",
                           "--title", title, "--notes-file", "-", input=notes)
        if created.returncode:
            raise Refused(f"gh release create failed: {created.stderr.strip()}")
        return created.stdout.strip()


class DryGitHub:
    """Prints the release gh would create, as if origin had the tag and no release yet."""

    def __init__(self, repo, out=print):
        self.repo, self.out = repo, out

    def tag_pushed(self, tag):
        return True

    def has_release(self, tag):
        return False

    def create_release(self, tag, title, notes, files):
        self.out(shlex.join(["gh", "release", "create", tag, *map(str, files), "--repo", self.repo, "--verify-tag",
                             "--title", title, "--notes-file", "-"]))
        self.out(f"  notes: {len(notes)} characters; files: "
                 + ", ".join(f"{Path(f).name} ({Path(f).stat().st_size} bytes)" for f in files))
        return None


class FakeGitHub:
    def __init__(self, pushed=True):
        self.pushed, self.releases = pushed, {}

    def tag_pushed(self, tag):
        return self.pushed

    def has_release(self, tag):
        return tag in self.releases

    def create_release(self, tag, title, notes, files):
        self.releases[tag] = (title, notes, {Path(f).name: Path(f).read_bytes() for f in files})
        return f"https://github.com/example/{tag}"


# Packwiz: refreshing the index from the files beside it, and exporting the CurseForge zip, which it
# names after pack.toml's name and version and writes beside the manifest.

class Packwiz:
    def __init__(self, root):
        self.root = root

    def run(self, *args):
        command = ["packwiz"] if shutil.which("packwiz") else ["mise", "exec", "--", "packwiz"]
        ran = subprocess.run([*command, *args], cwd=self.root, capture_output=True, text=True)
        if ran.returncode:
            raise Refused(f"packwiz {' '.join(args)} failed: {(ran.stderr or ran.stdout).strip()}")

    def refresh(self):
        self.run("refresh")

    def export(self):
        self.run("cf", "export")
        return exported_zip(self.root)


class FakePackwiz:
    """Refreshes nothing, and exports a zip holding pack.toml, or fails to when told to."""

    def __init__(self, root, failing=False):
        self.root, self.failing = root, failing

    def refresh(self):
        pass

    def export(self):
        if self.failing:
            raise Refused("packwiz cf export failed")
        path = exported_zip(self.root)
        path.write_bytes(b"zip of " + (self.root / "pack.toml").read_bytes())
        return path


def exported_zip(root):
    pack = tomllib.loads((root / "pack.toml").read_text())
    return root / f"{pack['name']}-{pack['version']}.zip"


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
