#!/usr/bin/env bash
# Release the pack at <version> from HEAD: the changelog's Unreleased entries and the mods changed
# since the last release become <version>'s section, the version goes into pack.toml and the
# compatibility checker's config, the manifest checks out, the zip is exported, and the release is
# committed and tagged mmp1.19.2-v<version> with the zip's sha256.
#
#   tools/release.sh <version>
#
# It pushes and uploads nothing, since both are public and for good: it ends by printing the push
# and upload commands. The zip stays beside the manifest (*.zip is ignored) until tools/upload.py
# sends it; a later export never matches it byte for byte, so that zip is the release.
set -euo pipefail
cd "$(dirname "$0")/.."

fail() { echo "release: $*" >&2; exit 1; }
packwiz() { if command -v packwiz > /dev/null; then command packwiz "$@"; else mise exec -- packwiz "$@"; fi; }

version="${1:-}"
[[ $# -eq 1 && "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "usage: tools/release.sh <major.minor.patch>"
tag="mmp1.19.2-v$version"
name="$(sed -n 's/^name = "\(.*\)"$/\1/p' pack.toml)"
zip="mechanical-mastery-plus-$version.zip"
changed=(pack.toml index.toml CHANGELOG.md config/bcc-common.toml)

[[ -z "$(git status --porcelain -- .)" ]] || fail "the pack folder has changes; commit or stash them first."
! git rev-parse -q --verify "refs/tags/$tag" > /dev/null || fail "$tag already exists."

# Until the commit, a failure puts the files back, so a failed release leaves nothing behind.
trap 'git checkout -- "${changed[@]}"; git reset -q -- "${changed[@]}"' EXIT
tools/changelog.py release "$version"
sed -i "s/^version = \".*\"$/version = \"$version\"/" pack.toml
# Better Compatibility Checker tells players and servers which pack version they run.
sed -i "s/modpackVersion = \"[^\"]*\"/modpackVersion = \"$version\"/" config/bcc-common.toml
packwiz refresh > /dev/null
git add -- "${changed[@]}"
tools/check.sh
git commit -q -m "$name $version" -- "${changed[@]}"
trap - EXIT

rm -f "$zip" "$name-$version.zip"
packwiz cf export > /dev/null
[[ -f "$name-$version.zip" ]] || fail "packwiz cf export didn't write $name-$version.zip."
# The server pack downloads the zip by this name from CurseForge's CDN, so it has no spaces.
mv "$name-$version.zip" "$zip"
sha="$(sha256sum "$zip" | cut -d' ' -f1)"
git tag -a "$tag" -m "$name $version" -m "zip sha256 $sha"

echo "Released $zip"
echo "zip sha256 $sha"
echo "Push with:   git push origin HEAD $tag"
echo "Then upload: tools/upload.py $version"
