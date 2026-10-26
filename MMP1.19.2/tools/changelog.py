#!/usr/bin/env python3
# The pack's changelog: the hand-written lines under "## Unreleased", and the mods a release adds,
# updates and removes, read from the manifest rather than written by hand.
#
#   tools/changelog.py mods [<ref>]      what changed in mods/ and pack.toml since <ref>, the last
#                                        release's tag by default, as the release would list it
#   tools/changelog.py release <version> turns "## Unreleased" into "## <version>", with the mod
#                                        changes under "### Mods"; refuses when nothing changed
#   tools/changelog.py notes <version>   prints <version>'s section, the notes an upload sends
#
# The repo holds other packs too, so this pack's release tags are mmp1.19.2-v<version>.
import subprocess
import sys
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CHANGELOG = ROOT / "CHANGELOG.md"
TAG_PREFIX = "mmp1.19.2-v"


def fail(message):
    sys.exit(f"changelog: {message}")


def git(*args):
    return subprocess.run(["git", "-C", str(ROOT), *args], check=True, capture_output=True, text=True).stdout


def last_release():
    """The newest release tag reachable from HEAD, or None before the first."""
    found = subprocess.run(["git", "-C", str(ROOT), "describe", "--tags", "--abbrev=0", "--match", f"{TAG_PREFIX}*"],
                           capture_output=True, text=True)
    return found.stdout.strip() or None


def manifest_at(ref):
    """pack.toml and every mod's .pw.toml at <ref>, or in the working tree when <ref> is None."""
    if ref is None:
        pack = tomllib.loads((ROOT / "pack.toml").read_text())
        mods = [tomllib.loads(p.read_text()) for d in ("mods", "resourcepacks") for p in sorted((ROOT / d).glob("*.pw.toml"))]
        return pack, mods
    # Paths are this pack folder's, so ./ keeps git show inside it.
    pack = tomllib.loads(git("show", f"{ref}:./pack.toml"))
    paths = [p for p in git("ls-tree", "-r", "--name-only", ref, "--", "mods/", "resourcepacks/").splitlines()
             if p.endswith(".pw.toml")]
    return pack, [tomllib.loads(git("show", f"{ref}:./{p}")) for p in paths]


def by_project(mods):
    """Each mod by its CurseForge project, which stays the same when its file is updated."""
    return {m.get("update", {}).get("curseforge", {}).get("project-id", m["filename"]): m for m in mods}


def mod_changes(ref):
    """The Markdown lines for what changed between <ref> and the working tree; none when nothing did."""
    old_pack, old_mods = manifest_at(ref)
    new_pack, new_mods = manifest_at(None)
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


def sections():
    """CHANGELOG.md as its preamble and its "## " sections, each a heading and its lines."""
    preamble, found = [], []
    for line in CHANGELOG.read_text().splitlines():
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


def release(version):
    preamble, found = sections()
    if not found or found[0][0] != "Unreleased":
        fail('CHANGELOG.md must open with "## Unreleased".')
    if any(heading == version for heading, _ in found):
        fail(f'CHANGELOG.md already has "## {version}".')
    entries = trimmed(found[0][1])
    previous = last_release()
    # Before the first tagged release there is no manifest to compare with, so no mod list.
    mods = mod_changes(previous) if previous else []
    if not entries and not mods:
        fail(f"nothing changed since {previous}: Unreleased is empty and so is the mod list.")
    body = entries + ([""] if entries and mods else []) + (["### Mods", ""] + mods if mods else [])
    out = preamble + ["## Unreleased", "", f"## {version}", "", *body, ""]
    for heading, lines in found[1:]:
        out += [f"## {heading}", *lines]
    CHANGELOG.write_text("\n".join(trimmed(out)) + "\n")


def notes(version):
    for heading, lines in sections()[1]:
        if heading == version:
            return "\n".join(trimmed(lines))
    fail(f'CHANGELOG.md has no "## {version}".')


def main(args):
    if args[:1] == ["mods"] and len(args) <= 2:
        ref = args[1] if len(args) == 2 else last_release()
        if ref is None:
            fail("there is no release tag yet to compare with.")
        print("\n".join(mod_changes(ref)) or f"No mod changes since {ref}.")
    elif args[:1] == ["release"] and len(args) == 2:
        release(args[1])
    elif args[:1] == ["notes"] and len(args) == 2:
        print(notes(args[1]))
    else:
        fail("usage: tools/changelog.py mods [<ref>] | release <version> | notes <version>")


if __name__ == "__main__":
    main(sys.argv[1:])
