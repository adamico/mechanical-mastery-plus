# The Release module through its interface, cut and publish, on a pack folder in a throwaway git
# repo, with fakes for packwiz, CurseForge and GitHub.
#
#   python3 -m unittest discover -s tools     (or: mise run test)
import hashlib
import subprocess
import tempfile
import unittest
from pathlib import Path

import release
from sites import DryCurseForge, DryGitHub, FakeCurseForge, FakeGitHub, FakePackwiz, Published, Refused

FILES = {
    "pack.toml": 'name = "Test Pack"\nauthor = "kc00l"\nversion = "0.0.0"\npack-format = "packwiz:1.1.0"\n\n'
                 '[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = ""\n\n'
                 '[versions]\nminecraft = "26.1.2"\nneoforge = "26.1.2.126"\n',
    "index.toml": 'hash-format = "sha256"\n',
    "CHANGELOG.md": "# Changelog\n\n## Unreleased\n\n- The first alpha.\n",
    "tools/release.toml": 'tag-prefix = "test-v"\nslug = "test-pack"\ncurseforge-project = 1\n'
                          'github-repo = "example/test-pack"\n',
}


def mod(name, filename, project, side="both"):
    return (f'name = "{name}"\nfilename = "{filename}"\nside = "{side}"\n\n'
            f'[update.curseforge]\nfile-id = {project}0001\nproject-id = {project}\n')


class PackFolder:
    """A pack folder one level down in a fresh git repo, like MMR26.1.2/ in this one."""

    def __init__(self, test, files=FILES):
        folder = tempfile.TemporaryDirectory()
        test.addCleanup(folder.cleanup)
        self.repo = Path(folder.name)
        self.root = self.repo / "pack"
        self.git("init", "-q", "-b", "main")
        for key, value in [("user.name", "Test"), ("user.email", "test@example.com"),
                           ("commit.gpgsign", "false"), ("tag.gpgsign", "false")]:
            self.git("config", key, value)
        self.write(".gitignore", "releases\n*.zip\n", at=self.repo)
        for path, text in files.items():
            self.write(path, text)
        self.write("mods/a.pw.toml", mod("A", "a-1.0.jar", 11))
        self.write("mods/zoom.pw.toml", mod("Zoom", "zoom-1.0.jar", 13, side="client"))
        self.commit("Test Pack")
        self.pack = release.Pack(self.root)
        self.out = []

    def git(self, *args):
        return subprocess.run(["git", "-C", str(self.repo), *args], check=True, capture_output=True, text=True).stdout

    def write(self, path, text, at=None):
        path = (at or self.root) / path
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)

    def read(self, path):
        return (self.root / path).read_text()

    def commit(self, message):
        self.git("add", "-A")
        self.git("commit", "-q", "-m", message)

    def add_entry(self, entry):
        self.write("CHANGELOG.md", self.read("CHANGELOG.md").replace("## Unreleased\n", f"## Unreleased\n\n- {entry}\n"))
        self.commit(entry)

    def cut(self, version, packwiz=None):
        release.cut(self.pack, version, packwiz or FakePackwiz(self.root), out=self.out.append)

    def publish(self, version, curseforge, github, **options):
        release.publish(self.pack, version, curseforge, github, out=self.out.append, **options)

    def record(self, version):
        return release.Record(self.pack.client_zip(version).parent / "record.json")


class Cut(unittest.TestCase):
    def setUp(self):
        self.folder = PackFolder(self)

    def test_the_first_alpha_writes_its_changelog_section_version_zip_commit_and_tag(self):
        head = self.folder.git("rev-parse", "HEAD")
        self.folder.cut("0.0.1")
        self.assertEqual("# Changelog\n\n## Unreleased\n\n## 0.0.1\n\n- The first alpha.\n",
                         self.folder.read("CHANGELOG.md"))
        self.assertIn('version = "0.0.1"', self.folder.read("pack.toml"))
        zip_data = self.folder.pack.client_zip("0.0.1").read_bytes()
        self.assertEqual("releases/0.0.1/test-pack-0.0.1.zip",
                         str(self.folder.pack.client_zip("0.0.1").relative_to(self.folder.root)))
        self.assertIn(f"zip sha256 {hashlib.sha256(zip_data).hexdigest()}",
                      self.folder.git("tag", "-l", "--format=%(contents)", "test-v0.0.1"))
        self.assertEqual(head, self.folder.git("rev-parse", "HEAD~1"))
        self.assertEqual("Test Pack 0.0.1", self.folder.git("log", "-1", "--format=%s").strip())
        self.assertEqual("", self.folder.git("status", "--porcelain"))
        # Cut is local: it says how to push, and pushes nothing.
        self.assertEqual("", self.folder.git("remote"))
        self.assertIn("Push with:    git push origin HEAD test-v0.0.1", self.folder.out)

    def test_refuses_a_pack_folder_with_changes(self):
        self.folder.write("mods/b.pw.toml", mod("B", "b-1.0.jar", 12))
        with self.assertRaisesRegex(Refused, "has changes"):
            self.folder.cut("0.0.1")

    def test_refuses_off_main(self):
        # Only main commits index.toml and pack.toml (AGENTS.md), and a cut commits both.
        self.folder.git("checkout", "-q", "-b", "topic")
        with self.assertRaisesRegex(Refused, "on main"):
            self.folder.cut("0.0.1")
        self.assertEqual("", self.folder.git("tag", "-l"))

    def test_refuses_a_version_already_tagged(self):
        self.folder.cut("0.0.1")
        self.folder.add_entry("More.")
        with self.assertRaisesRegex(Refused, "test-v0.0.1 already exists"):
            self.folder.cut("0.0.1")

    def test_a_failed_export_leaves_every_file_as_it_was(self):
        before = {path: self.folder.read(path) for path in release.CUT_FILES}
        with self.assertRaisesRegex(Refused, "export failed"):
            self.folder.cut("0.0.1", FakePackwiz(self.folder.root, failing=True))
        self.assertEqual(before, {path: self.folder.read(path) for path in release.CUT_FILES})
        self.assertEqual("", self.folder.git("status", "--porcelain", "--ignored"))
        self.assertEqual("", self.folder.git("tag", "-l"))

    def test_lists_the_mods_and_neoforge_changed_since_the_last_release(self):
        self.folder.cut("0.0.1")
        self.folder.write("mods/a.pw.toml", mod("A", "a-1.1.jar", 11))
        self.folder.write("mods/b.pw.toml", mod("B", "b-1.0.jar", 12))
        self.folder.write("pack.toml", self.folder.read("pack.toml").replace("26.1.2.126", "26.1.2.130"))
        self.folder.commit("Update A and NeoForge, add B")
        self.folder.add_entry("Essence is cheaper.")

        self.folder.cut("0.0.2")

        self.assertIn("## Unreleased\n\n## 0.0.2\n\n- Essence is cheaper.\n\n### Mods\n\n"
                      "- NeoForge 26.1.2.126 → 26.1.2.130\n- Added B (`b-1.0.jar`)\n"
                      "- Updated A: `a-1.0.jar` → `a-1.1.jar`\n\n## 0.0.1\n", self.folder.read("CHANGELOG.md"))

    def test_refuses_a_first_release_with_nothing_in_it(self):
        folder = PackFolder(self, {**FILES, "CHANGELOG.md": "# Changelog\n\n## Unreleased\n"})
        with self.assertRaisesRegex(Refused, "Unreleased is empty"):
            folder.cut("0.0.1")

    def test_refuses_a_release_with_nothing_in_it(self):
        self.folder.cut("0.0.1")
        with self.assertRaisesRegex(Refused, "nothing changed since test-v0.0.1"):
            self.folder.cut("0.0.2")


class Publish(unittest.TestCase):
    def setUp(self):
        self.folder = PackFolder(self)
        self.folder.cut("0.0.1")
        self.curseforge, self.github = FakeCurseForge(), FakeGitHub()

    def publish(self, version="0.0.1", **options):
        self.folder.publish(version, self.curseforge, self.github, **options)

    def uploaded(self, file_name):
        return next((file_id, metadata, data) for file_id, (metadata, name, data) in self.curseforge.uploads.items()
                    if name == file_name)

    def test_sends_the_client_zip_to_curseforge_then_to_the_tags_github_release(self):
        self.publish()
        client_id, client, data = self.uploaded("test-pack-0.0.1.zip")
        self.assertEqual(self.folder.pack.client_zip("0.0.1").read_bytes(), data)
        self.assertEqual(([1, 2], "- The first alpha.", "markdown"),
                         (client["gameVersions"], client["changelog"], client["changelogType"]))
        title, notes, files = self.github.releases["test-v0.0.1"]
        self.assertEqual(("Test Pack 0.0.1 alpha", "- The first alpha.", {"test-pack-0.0.1.zip"}),
                         (title, notes, set(files)))
        record = self.folder.record("0.0.1")
        self.assertEqual((client_id, ["curseforge", "github"]), (record.client_file_id, record.sites))

    def test_an_alpha_is_a_release_file_named_as_an_alpha(self):
        self.publish()
        _, client, _ = self.uploaded("test-pack-0.0.1.zip")
        self.assertEqual(("release", "Test Pack 0.0.1 alpha"), (client["releaseType"], client["displayName"]))

    def test_from_0_1_0_a_release_file_is_no_longer_named_as_an_alpha(self):
        self.folder.add_entry("Playable without caveats.")
        self.folder.cut("0.1.0")
        self.publish("0.1.0")
        _, client, _ = self.uploaded("test-pack-0.1.0.zip")
        self.assertEqual(("release", "Test Pack 0.1.0"), (client["releaseType"], client["displayName"]))

    def test_a_dry_run_shows_a_release_file_named_as_an_alpha_and_keeps_no_record(self):
        out = self.folder.out
        self.folder.publish("0.0.1", DryCurseForge("1", out.append), DryGitHub("example/test-pack", out.append),
                            dry_run=True)
        printed = "\n".join(out)
        self.assertIn("POST https://minecraft.curseforge.com/api/projects/1/upload-file", printed)
        self.assertIn('"displayName": "Test Pack 0.0.1 alpha"', printed)
        self.assertIn('"releaseType": "release"', printed)
        self.assertIn("file: test-pack-0.0.1.zip", printed)
        self.assertIn("gh release create test-v0.0.1", printed)
        self.assertFalse((self.folder.pack.client_zip("0.0.1").parent / "record.json").exists())

    def test_a_retry_after_a_failed_publish_resumes_from_the_record(self):
        self.curseforge.refusing.add("test-pack-0.0.1.zip")
        with self.assertRaisesRegex(Refused, "curseforge failed"):
            self.publish()
        self.assertEqual(["github"], self.folder.record("0.0.1").sites)

        self.curseforge.refusing.clear()
        self.github.releases.clear()  # a second GitHub release would be refused as published
        self.publish()

        self.assertEqual(1, len(self.curseforge.uploads))
        self.assertEqual({}, self.github.releases)
        self.assertEqual(["github", "curseforge"], self.folder.record("0.0.1").sites)

    def test_refuses_a_zip_the_tag_did_not_cut(self):
        self.folder.pack.client_zip("0.0.1").write_bytes(b"another export")
        with self.assertRaisesRegex(Refused, "isn't the zip test-v0.0.1 cut"):
            self.publish()
        self.assertEqual({}, self.curseforge.uploads)

    def test_a_site_that_has_the_version_is_refused_and_the_others_still_run(self):
        self.github.releases["test-v0.0.1"] = ("Test Pack 0.0.1 alpha", "", {})
        self.publish()
        self.assertEqual(1, len(self.curseforge.uploads))
        self.assertIn("github: GitHub already has a release for test-v0.0.1", "\n".join(self.folder.out))

    def test_a_release_every_site_has_is_refused_as_published(self):
        self.curseforge.names |= {"test-pack-0.0.1.zip"}
        self.github.releases["test-v0.0.1"] = ("Test Pack 0.0.1 alpha", "", {})
        with self.assertRaises(Published):
            self.publish()

    def test_github_is_refused_until_origin_has_the_tag(self):
        self.github.pushed = False
        with self.assertRaisesRegex(Refused, "github failed"):
            self.publish()
        self.assertIn("push it first", "\n".join(self.folder.out))
        self.assertEqual({}, self.github.releases)

        self.github.pushed = True
        self.publish()
        self.assertIn("test-v0.0.1", self.github.releases)
        self.assertEqual(1, len(self.curseforge.uploads))

    def test_one_site_is_sent_whatever_the_record_says(self):
        self.publish()
        self.github.releases.clear()
        self.publish(only="github")
        self.assertIn("test-v0.0.1", self.github.releases)
        self.assertEqual(1, len(self.curseforge.uploads))

    def test_curseforge_is_refused_until_the_pack_has_a_curseforge_project(self):
        folder = PackFolder(self, {**FILES, "tools/release.toml": 'tag-prefix = "test-v"\nslug = "test-pack"\n'
                                                                  'github-repo = "example/test-pack"\n'})
        folder.cut("0.0.1")
        with self.assertRaisesRegex(Refused, "curseforge failed"):
            folder.publish("0.0.1", self.curseforge, self.github)
        self.assertIn("no curseforge-project", "\n".join(folder.out))
        self.assertEqual({}, self.curseforge.uploads)
        self.assertIn("test-v0.0.1", self.github.releases)


if __name__ == "__main__":
    unittest.main()
