#!/usr/bin/env python3
# Cut and publish this pack's releases.
#
#   tools/release.py cut <version>
#   tools/release.py publish <version> [--dry-run] [--site curseforge|server|github] [--client-file-id <id>]
#   tools/release.py mods [<ref>]
#
# cut makes the release from HEAD, all of it local: the changelog's Unreleased entries and the mods
# changed since the last release become <version>'s section, the version goes into pack.toml and the
# compatibility checker's config, the client zip is exported into releases/<version>/, and the
# release is committed and tagged with the zip's sha256. It runs the tool's tests first, and a
# failure before the commit puts every file back. A later export never matches the zip byte for
# byte, so the zip it exported is the release's.
#
# publish sends a cut release to its sites, in order: the client zip to CurseForge, the server pack
# (server_files/ as tagged, pointed at the client zip on CurseForge's CDN, with every tagged mod but
# the side = "client" ones downloaded straight from the CDN) as its child file, and
# both zips to the tag's GitHub release, with the version's changelog section as their notes. The
# release record beside the zip keeps the client zip's file id and the sites reached, so after a
# failure, publish again does what is left; --site sends to one site whatever the record says, and
# --client-file-id stands in for a lost record. A site that already has the version is refused,
# since a published version never changes. --dry-run prints what each site would be sent.
#
# mods lists what changed in mods/ and pack.toml since <ref>, the last release's tag by default, as
# the release's changelog would.
#
# CurseForge takes $CURSEFORGE_TOKEN, an upload API token, which is never printed. When it is
# missing, publish runs itself again through `op run --env-file=tools/publish.env`, which fills it in
# from 1Password for that run only. GitHub goes through `gh`, and needs the tag pushed first.
import functools
import hashlib
import io
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import tomllib
import urllib.parse
import zipfile
from pathlib import Path

import sites
from sites import Published, Refused

ROOT = Path(__file__).resolve().parent.parent
VERSION = re.compile(r"\d+\.\d+\.\d+")
# What a cut changes, and puts back when it fails.
CUT_FILES = ["pack.toml", "index.toml", "CHANGELOG.md", "config/bcc-common.toml"]
SERVER_CONFIG = "server_files/server-setup-config.yaml"
SITES = ["curseforge", "server", "github"]


class Pack:
    """A pack folder: its manifest, changelog and identity, and the git history its releases are tagged in."""

    def __init__(self, root):
        self.root = root
        identity = tomllib.loads((root / "tools/release.toml").read_text())
        self.tag_prefix, self.slug = identity["tag-prefix"], identity["slug"]
        self.curseforge_project, self.github_repo = str(identity["curseforge-project"]), identity["github-repo"]
        self.name = tomllib.loads((root / "pack.toml").read_text())["name"]

    def git(self, *args, check=True):
        ran = subprocess.run(["git", "-C", str(self.root), *args], capture_output=True)
        if check and ran.returncode:
            raise Refused(f"git {' '.join(args)} failed: {ran.stderr.decode(errors='replace').strip()}")
        return ran.stdout

    def text(self, *args, check=True):
        return self.git(*args, check=check).decode()

    def tag(self, version):
        return f"{self.tag_prefix}{version}"

    def client_zip(self, version):
        return self.root / "releases" / version / f"{self.slug}-{version}.zip"

    def last_release(self):
        """The newest release tag reachable from HEAD, or None before the first."""
        return self.text("describe", "--tags", "--abbrev=0", "--match", f"{self.tag_prefix}*", check=False).strip() or None

    def manifest_at(self, ref):
        """pack.toml and every mod's .pw.toml at <ref>, or in the working tree when <ref> is None."""
        pack, files = self.files_at(ref)
        return pack, [m for _, m in files]

    def files_at(self, ref):
        """pack.toml, and the folder and .pw.toml of every mod and resource pack at <ref>, or in the working tree."""
        if ref is None:
            pack = tomllib.loads((self.root / "pack.toml").read_text())
            files = [(d, tomllib.loads(p.read_text())) for d in ("mods", "resourcepacks")
                     for p in sorted((self.root / d).glob("*.pw.toml"))]
            return pack, files
        # Paths are this pack folder's, so ./ keeps git show inside it.
        pack = tomllib.loads(self.text("show", f"{ref}:./pack.toml"))
        paths = [p for p in self.text("ls-tree", "-r", "--name-only", ref, "--", "mods/", "resourcepacks/").splitlines()
                 if p.endswith(".pw.toml")]
        return pack, [(p.split("/")[0], tomllib.loads(self.text("show", f"{ref}:./{p}"))) for p in paths]

    def mod_changes(self, ref):
        """The changelog lines for what changed between <ref> and the working tree; none when nothing did."""
        old_pack, old_mods = self.manifest_at(ref)
        new_pack, new_mods = self.manifest_at(None)
        lines = []
        for key, label in [("minecraft", "Minecraft"), ("forge", "Forge")]:
            old, new = old_pack["versions"].get(key), new_pack["versions"].get(key)
            if old != new:
                lines.append(f"- {label} {old} → {new}")
        old, new = by_project(old_mods), by_project(new_mods)
        by_name = lambda mods: lambda project: mods[project]["name"].casefold()
        for p in sorted(new.keys() - old.keys(), key=by_name(new)):
            lines.append(f"- Added {new[p]['name']} (`{new[p]['filename']}`)")
        for p in sorted(old.keys() & new.keys(), key=by_name(new)):
            if old[p]["filename"] != new[p]["filename"]:
                lines.append(f"- Updated {new[p]['name']}: `{old[p]['filename']}` → `{new[p]['filename']}`")
        for p in sorted(old.keys() - new.keys(), key=by_name(old)):
            lines.append(f"- Removed {old[p]['name']}")
        return lines


def by_project(mods):
    """Each mod by its CurseForge project, which stays the same when its file is updated."""
    return {m.get("update", {}).get("curseforge", {}).get("project-id", m["filename"]): m for m in mods}


# The changelog: a preamble, then "## " sections, the first of them "## Unreleased".

def sections(text):
    preamble, found = [], []
    for line in text.splitlines():
        if line.startswith("## "):
            found.append((line[3:], []))
        elif found:
            found[-1][1].append(line)
        else:
            preamble.append(line)
    return preamble, found


def trimmed(lines):
    while lines and not lines[0].strip():
        lines = lines[1:]
    while lines and not lines[-1].strip():
        lines = lines[:-1]
    return lines


def released(text, version, mods, previous):
    """The changelog with "## Unreleased" turned into "## <version>", the mod changes under "### Mods"."""
    preamble, found = sections(text)
    if not found or found[0][0] != "Unreleased":
        raise Refused('CHANGELOG.md must open with "## Unreleased".')
    if any(heading == version for heading, _ in found):
        raise Refused(f'CHANGELOG.md already has "## {version}".')
    entries = trimmed(found[0][1])
    if not entries and not mods:
        raise Refused(f"nothing changed since {previous}: Unreleased is empty and so is the mod list.")
    body = entries + ([""] if entries and mods else []) + (["### Mods", ""] + mods if mods else [])
    out = preamble + ["## Unreleased", "", f"## {version}", "", *body, ""]
    for heading, lines in found[1:]:
        out += [f"## {heading}", *lines]
    return "\n".join(trimmed(out)) + "\n"


def notes(text, version):
    for heading, lines in sections(text)[1]:
        if heading == version:
            return "\n".join(trimmed(lines))
    raise Refused(f'CHANGELOG.md has no "## {version}".')


def replaced(path, pattern, replacement):
    text, found = re.subn(pattern, replacement, path.read_text(), flags=re.MULTILINE)
    if found != 1:
        raise Refused(f"{path.name} has {found} lines matching {pattern}; it needs exactly one.")
    path.write_text(text)


def cut(pack, version, packwiz, out=print):
    if not VERSION.fullmatch(version):
        raise Refused("the version is <major.minor.patch>.")
    tag = pack.tag(version)
    if pack.text("status", "--porcelain", "--", "."):
        raise Refused("the pack folder has changes; commit or stash them first.")
    if pack.git("rev-parse", "-q", "--verify", f"refs/tags/{tag}", check=False):
        raise Refused(f"{tag} already exists.")
    previous = pack.last_release()
    # Before the first tagged release there is no manifest to compare with, so no mod list.
    mods = pack.mod_changes(previous) if previous else []
    changelog = pack.root / "CHANGELOG.md"
    text = released(changelog.read_text(), version, mods, previous)
    client_zip = pack.client_zip(version)
    # Until the commit, a failure puts the files back, so a failed cut leaves nothing behind.
    try:
        changelog.write_text(text)
        replaced(pack.root / "pack.toml", r'^version = ".*"$', f'version = "{version}"')
        # Better Compatibility Checker tells players and servers which pack version they run.
        replaced(pack.root / "config/bcc-common.toml", r'modpackVersion = "[^"]*"', f'modpackVersion = "{version}"')
        packwiz.refresh()
        exported = packwiz.export()
        if not exported.is_file():
            raise Refused(f"packwiz cf export didn't write {exported.name}.")
        client_zip.parent.mkdir(parents=True, exist_ok=True)
        # The server pack downloads the zip by this name from CurseForge's CDN, so it has no spaces.
        exported.replace(client_zip)
        pack.git("add", "--", *CUT_FILES)
        pack.git("commit", "-q", "-m", f"{pack.name} {version}", "--", *CUT_FILES)
    except BaseException:
        pack.git("reset", "-q", "--", *CUT_FILES, check=False)
        pack.git("checkout", "--", *CUT_FILES, check=False)
        shutil.rmtree(client_zip.parent, ignore_errors=True)
        raise
    sha = hashlib.sha256(client_zip.read_bytes()).hexdigest()
    pack.git("tag", "-a", tag, "-m", f"{pack.name} {version}", "-m", f"zip sha256 {sha}")
    out(f"Cut {pack.name} {version}: {client_zip.relative_to(pack.root)}, zip sha256 {sha}")
    out(f"Push with:    git push origin HEAD {tag}")
    out(f"Then publish: tools/release.py publish {version}")


def cdn_url(file_id, file_name):
    """Where CurseForge's CDN serves a file: its id split after four digits, without leading zeros, and its
    name percent-encoded, since the CDN refuses a literal + (403). The older media.forgecdn.net host refuses
    every download now; mediafilez serves the same paths."""
    digits = str(file_id)
    return f"https://mediafilez.forgecdn.net/files/{int(digits[:4])}/{int(digits[4:])}/{urllib.parse.quote(file_name)}"


def pointed_at(data, url):
    """The ServerStarter config with its modpackUrl set to <url>; refused unless it has exactly one."""
    data, found = re.subn(rb"(modpackUrl:\s*)\S+", lambda m: m.group(1) + url.encode(), data)
    if found != 1:
        raise Refused(f"{SERVER_CONFIG} has {found} modpackUrl lines; it needs exactly one.")
    return data


IGNORE_PROJECT = re.compile(rb"^( *)ignoreProject:[^\r\n]*((?:\r?\n\1 +-[^\r\n]*)*)", re.MULTILINE)
# The header and its entries, up to the first commented-out one.
ADDITIONAL_FILES = re.compile(rb"^( *)additionalFiles:( *\[\])?[ \t]*((?:\r?\n\1 +(?![ #])[^\r\n]*)*)", re.MULTILINE)


def newline_of(data):
    # The committed config has CRLF line endings, and what a server pack adds keeps them.
    return b"\r\n" if b"\r\n" in data else b"\n"


def ignoring(data, projects):
    """The ServerStarter config with ignoreProject listing <projects>, every CurseForge project of the tagged
    manifest, so that ServerStarter never asks cursemeta for one (cursemeta is gone, #177). A config with its
    own list is refused: the manifest is the only record of the pack's mods, so the list stays []."""
    found = list(IGNORE_PROJECT.finditer(data))
    if len(found) != 1:
        raise Refused(f"{SERVER_CONFIG} has {len(found)} ignoreProject lines; it needs exactly one.")
    match = found[0]
    if match.group(2).strip():
        raise Refused(f"{SERVER_CONFIG} lists ignoreProject by hand; the manifest is the only record of the "
                      f"pack's mods, so the list stays []: each server pack fills it in.")
    indent = match.group(1)
    lines = [indent + b"ignoreProject:" + (b"" if projects else b" []")]
    lines += [indent + f"    - {project} # {name}".encode() for project, name in projects]
    return data[:match.start()] + newline_of(data).join(lines) + data[match.end():]


def downloading(data, downloads):
    """The ServerStarter config with <downloads>, the server's mods as (url, file name), after the
    additionalFiles it lists by hand; refused unless it has exactly one list, or when it lists a mod by hand."""
    found = list(ADDITIONAL_FILES.finditer(data))
    if len(found) != 1:
        raise Refused(f"{SERVER_CONFIG} has {len(found)} additionalFiles lines; it needs exactly one.")
    match = found[0]
    listed = set(re.findall(rb"destination: *(\S+)", match.group(3)))
    by_hand = sorted(name for _, name in downloads if f"mods/{name}".encode() in listed)
    if by_hand:
        raise Refused(f"{SERVER_CONFIG} lists {', '.join(by_hand)} in additionalFiles by hand, and the pack has "
                      f"it too; each server pack adds the pack's mods itself.")
    indent, newline = match.group(1), newline_of(data)
    lines = [indent + b"additionalFiles:" + (b"" if downloads or match.group(3) else b" []") + match.group(3)]
    for url, name in downloads:
        lines += [indent + f"    - url: {url}".encode(), indent + f"      destination: mods/{name}".encode()]
    return data[:match.start()] + newline.join(lines) + data[match.end():]


class Record:
    """The release record: the client zip's CurseForge file id, and the sites the release has reached."""

    def __init__(self, path):
        self.path = path
        data = json.loads(path.read_text()) if path.is_file() else {}
        self.client_file_id, self.sites = data.get("client_file_id"), data.get("sites", [])

    def reached(self, site):
        if site not in self.sites:
            self.sites.append(site)

    def save(self):
        self.path.write_text(json.dumps({"client_file_id": self.client_file_id, "sites": self.sites}, indent=2) + "\n")


class CutRelease:
    """A release as cut: its tag, and the client zip beside the manifest, checked against the tag's sha256."""

    def __init__(self, pack, version):
        if not VERSION.fullmatch(version):
            raise Refused("the version is <major.minor.patch>.")
        self.pack, self.version, self.tag = pack, version, pack.tag(version)
        message = pack.text("tag", "-l", "--format=%(contents)", self.tag)
        if not message.strip():
            raise Refused(f"{self.tag} doesn't exist; only a version tools/release.py cut is published.")
        sha = next((line.split()[-1] for line in message.splitlines() if line.startswith("zip sha256 ")), None)
        if not sha:
            raise Refused(f"{self.tag} names no zip sha256, so there is no zip to check against it.")
        self.zip = pack.client_zip(version)
        if not self.zip.is_file():
            raise Refused(f"{self.zip.relative_to(pack.root)} is missing; a later export never matches the cut zip, "
                          f"so it can't be rebuilt.")
        self.data = self.zip.read_bytes()
        if hashlib.sha256(self.data).hexdigest() != sha:
            raise Refused(f"{self.zip.name} isn't the zip {self.tag} cut (sha256 {sha}).")
        # The tag's own pack.toml and changelog, whatever HEAD has moved on to.
        tagged = tomllib.loads(pack.text("show", f"{self.tag}:./pack.toml"))
        self.minecraft = tagged["versions"]["minecraft"]
        self.notes = notes(pack.text("show", f"{self.tag}:./CHANGELOG.md"), version)
        self.title = f"{tagged['name']} {version}"
        self.server_title = f"{tagged['name']} Server {version}"
        self.server_zip = f"{pack.slug}-Server-{version}.zip"
        self.record = Record(self.zip.parent / "record.json")

    @functools.cached_property
    def server_mods(self):
        return server_mods(self.pack.files_at(self.tag)[1])

    def metadata(self, title, **more):
        # Every upload is a release: CurseForge's app only offers a pack that has one.
        return {"changelog": self.notes, "changelogType": "markdown", "displayName": title,
                "releaseType": "release", **more}

    def server_pack(self, client_file_id):
        """server_files/ as tagged, zipped, with ServerStarter pointed at the client zip on the CDN."""
        url = cdn_url(client_file_id, self.zip.name) if client_file_id else "<the client zip's CDN url>"
        paths = self.pack.text("ls-tree", "-r", "--name-only", self.tag, "--", "server_files/").splitlines()
        if SERVER_CONFIG not in paths:
            raise Refused(f"{self.tag} has no {SERVER_CONFIG}.")
        buffer = io.BytesIO()
        with zipfile.ZipFile(buffer, "w", zipfile.ZIP_DEFLATED) as out:
            for path in paths:
                data = self.pack.git("show", f"{self.tag}:./{path}")
                if path == SERVER_CONFIG:
                    projects, downloads = self.server_mods
                    data = downloading(ignoring(pointed_at(data, url), projects), downloads)
                out.writestr(path.removeprefix("server_files/"), data)
        return url, buffer.getvalue()


def server_mods(files):
    """What ServerStarter is told about the manifest's files, as (projects, downloads): every CurseForge
    project with its name, which it ignores, and the CDN url and file name of each mod not marked
    side = "client", which it downloads instead. Resource packs aren't the server's."""
    projects, downloads = [], []
    for folder, m in files:
        curseforge = m.get("update", {}).get("curseforge", {})
        # Any other file rides in the client zip's overrides, where neither list reaches it.
        if not {"project-id", "file-id"} <= curseforge.keys():
            raise Refused(f"{m['name']} has no CurseForge file, so the server pack can't tell ServerStarter about it.")
        projects.append((curseforge["project-id"], m["name"]))
        if folder == "mods" and m.get("side") != "client":
            downloads.append((cdn_url(curseforge["file-id"], m["filename"]), m["filename"]))
    return sorted(projects), sorted(downloads, key=lambda d: d[1].casefold())


def publish(pack, version, curseforge, github, only=None, dry_run=False, client_file_id=None, out=print):
    """Sends the cut release to the sites the record hasn't reached, or to <only>; refuses with what failed."""
    release = CutRelease(pack, version)
    record = release.record
    if client_file_id:
        record.client_file_id = client_file_id
    todo = [only] if only else [site for site in SITES if site not in record.sites]
    if not todo:
        out(f"{release.title} is on every site already.")
        return
    steps = {"curseforge": lambda: to_curseforge(release, curseforge, dry_run, out),
             "server": lambda: to_server(release, curseforge, dry_run, out),
             "github": lambda: to_github(release, github, dry_run, out)}
    failed, published = [], []
    for site in todo:
        try:
            steps[site]()
            record.reached(site)
        except Published as refused:
            out(f"{site}: {refused}")
            record.reached(site)
            published.append(site)
        except Refused as refused:
            out(f"{site}: {refused}")
            failed.append(site)
        if not dry_run:
            record.save()
    if failed:
        raise Refused(f"{', '.join(failed)} failed; once fixed, run tools/release.py publish {version} again.")
    if len(published) == len(todo):
        raise Published(f"{release.title} was already on {', '.join(published)}; nothing was sent.")


def needs_client_file_id(release, dry_run):
    if not release.record.client_file_id and not dry_run:
        raise Refused("this needs the client zip's CurseForge file id, which the release record doesn't have: "
                      "publish to curseforge first, or pass --client-file-id <id>.")
    return release.record.client_file_id


def to_curseforge(release, curseforge, dry_run, out):
    if {release.zip.name, release.title} & curseforge.file_names():
        raise Published(f"CurseForge already has {release.version}, and a published version never changes.")
    metadata = release.metadata(release.title, gameVersions=curseforge.game_version_ids(release.minecraft))
    file_id = curseforge.upload(metadata, release.zip.name, release.data)
    # A dry run answers no id, and keeps the one it was given.
    release.record.client_file_id = file_id or release.record.client_file_id
    if not dry_run:
        out(f"Uploaded {release.zip.name} to CurseForge ({release.record.client_file_id}); it shows once reviewed.")


def to_server(release, curseforge, dry_run, out):
    client_file_id = needs_client_file_id(release, dry_run)
    url, data = release.server_pack(client_file_id)
    if {release.server_zip, release.server_title} & curseforge.file_names():
        raise Published(f"CurseForge already has the {release.version} server pack.")
    metadata = release.metadata(release.server_title, parentFileId=client_file_id or "<the client zip's file id>")
    created = curseforge.upload(metadata, release.server_zip, data)
    if dry_run:
        projects, downloads = release.server_mods
        out(f"  ignoreProject {len(projects)} projects; additionalFiles adds {len(downloads)} mods from the CDN")
    out(f"  modpackUrl {url}" if dry_run else
        f"Uploaded {release.server_zip} to CurseForge ({created}), under {client_file_id}, installing {url}.")


def to_github(release, github, dry_run, out):
    # The server pack is attached too; it names the client zip's CDN url, so it needs the file id.
    client_file_id = needs_client_file_id(release, dry_run)
    if not github.tag_pushed(release.tag):
        raise Refused(f"origin doesn't have {release.tag} as tagged here; push it first: git push origin {release.tag}")
    if github.has_release(release.tag):
        raise Published(f"GitHub already has a release for {release.tag}, and a published version never changes.")
    url, data = release.server_pack(client_file_id)
    with tempfile.TemporaryDirectory() as folder:
        server_zip = Path(folder) / release.server_zip
        server_zip.write_bytes(data)
        link = github.create_release(release.tag, release.title, release.notes, [release.zip, server_zip])
    out(f"  modpackUrl {url}" if dry_run else f"Released {release.zip.name} and {release.server_zip} on GitHub: {link}")


def through_op(args):
    """Runs publish again under `op run` when the CurseForge token is missing, once."""
    env_file = ROOT / "tools/publish.env"
    if (os.environ.get("CURSEFORGE_TOKEN") or os.environ.get("PUBLISH_THROUGH_OP")
            or not env_file.is_file() or not shutil.which("op")):
        return
    os.environ["PUBLISH_THROUGH_OP"] = "1"
    sys.stdout.flush()
    os.execvp("op", ["op", "run", f"--env-file={env_file}", "--", sys.executable, __file__, *args])


USAGE = ("usage: tools/release.py cut <version> | publish <version> [--dry-run] [--site curseforge|server|github] "
         "[--client-file-id <id>] | mods [<ref>]")


def option(rest, name, valid):
    if name not in rest:
        return None, rest
    at = rest.index(name)
    if at + 1 >= len(rest) or not valid(rest[at + 1]):
        sys.exit(USAGE)
    return rest[at + 1], rest[:at] + rest[at + 2:]


def main(args):
    command, rest = (args[0], args[1:]) if args else (None, [])
    pack = Pack(ROOT)
    if command == "cut" and len(rest) == 1:
        tests = subprocess.run([sys.executable, "-m", "unittest", "discover", "-s", str(ROOT / "tools"), "-q"],
                               capture_output=True, text=True)
        if tests.returncode:
            sys.exit(f"release: the release tool's tests fail, so nothing is cut:\n{tests.stderr[-3000:]}")
        cut(pack, rest[0], sites.Packwiz(ROOT))
    elif command == "publish":
        dry_run = "--dry-run" in rest
        rest = [a for a in rest if a != "--dry-run"]
        only, rest = option(rest, "--site", lambda v: v in SITES)
        client_file_id, rest = option(rest, "--client-file-id", str.isdigit)
        if len(rest) != 1:
            sys.exit(USAGE)
        if dry_run:
            curseforge, github = sites.DryCurseForge(pack.curseforge_project), sites.DryGitHub(pack.github_repo)
        else:
            if only in (None, "curseforge", "server"):
                through_op(args)
            curseforge = sites.CurseForge(pack.curseforge_project, os.environ.get("CURSEFORGE_TOKEN"),
                                          f"{pack.github_repo}/{rest[0]}")
            github = sites.GitHub(pack.github_repo, ROOT)
        publish(pack, rest[0], curseforge, github, only, dry_run, int(client_file_id) if client_file_id else None)
    elif command == "mods" and len(rest) <= 1:
        ref = rest[0] if rest else pack.last_release()
        if ref is None:
            raise Refused("there is no release tag yet to compare with.")
        print("\n".join(pack.mod_changes(ref)) or f"No mod changes since {ref}.")
    else:
        sys.exit(USAGE)


if __name__ == "__main__":
    try:
        main(sys.argv[1:])
    except Published as published:
        sys.exit(f"release: {published}")
    except Refused as refused:
        sys.exit(f"release: {refused}")
