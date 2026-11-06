# The Release module through its interface, cut and publish, on a pack folder in a throwaway git
# repo, with fakes for packwiz, CurseForge and GitHub.
#
#   python3 -m unittest discover -s tools     (or: mise run test)
import hashlib
import io
import subprocess
import tempfile
import unittest
import zipfile
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
    "server_files/server-setup-config.yaml": "install:\n    loaderVersion: <the-neoforge-version>\n"
                                             "    modpackUrl: <the-client-zip-url>\n"
                                             "    formatSpecific:\n        ignoreProject: []\n"
                                             "    additionalFiles: []\n",
    "server_files/start-server.sh": "#!/bin/bash\njava -jar serverstarter.jar\n",
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

    def server_config(self, version="0.0.1"):
        _, _, data = self.uploaded(f"test-pack-Server-{version}.zip")
        with zipfile.ZipFile(io.BytesIO(data)) as server_pack:
            return server_pack.read("server-setup-config.yaml").decode()

    def test_sends_the_client_zip_then_the_server_pack_under_it_then_both_to_the_tags_github_release(self):
        self.publish()
        client_id, client, data = self.uploaded("test-pack-0.0.1.zip")
        self.assertEqual(self.folder.pack.client_zip("0.0.1").read_bytes(), data)
        self.assertEqual(([1, 2], "- The first alpha.", "markdown"),
                         (client["gameVersions"], client["changelog"], client["changelogType"]))
        _, server, _ = self.uploaded("test-pack-Server-0.0.1.zip")
        self.assertEqual(("Test Pack Server 0.0.1 alpha", client_id, "release"),
                         (server["displayName"], server["parentFileId"], server["releaseType"]))
        title, notes, files = self.github.releases["test-v0.0.1"]
        self.assertEqual(("Test Pack 0.0.1 alpha", "- The first alpha.", {"test-pack-0.0.1.zip", "test-pack-Server-0.0.1.zip"}),
                         (title, notes, set(files)))
        record = self.folder.record("0.0.1")
        self.assertEqual((client_id, ["curseforge", "server", "github"]), (record.client_file_id, record.sites))

    def test_an_alpha_is_a_release_file_named_as_an_alpha(self):
        self.publish()
        _, client, _ = self.uploaded("test-pack-0.0.1.zip")
        self.assertEqual(("release", "Test Pack 0.0.1 alpha"), (client["releaseType"], client["displayName"]))

    def test_from_0_1_0_a_release_file_is_no_longer_named_as_an_alpha(self):
        self.folder.add_entry("Playable without caveats.")
        self.folder.cut("0.1.0")
        self.publish("0.1.0")
        _, client, _ = self.uploaded("test-pack-0.1.0.zip")
        _, server, _ = self.uploaded("test-pack-Server-0.1.0.zip")
        self.assertEqual(("release", "Test Pack 0.1.0"), (client["releaseType"], client["displayName"]))
        self.assertEqual(("release", "Test Pack Server 0.1.0"), (server["releaseType"], server["displayName"]))

    def test_the_server_pack_holds_the_tagged_server_files_pointed_at_the_client_zip_and_neoforge(self):
        # server_files/ moving on after the tag doesn't change the release's server pack.
        self.folder.write("server_files/start-server.sh", "#!/bin/bash\necho changed\n")
        self.folder.commit("Change the server files")
        self.publish()
        client_id, _, _ = self.uploaded("test-pack-0.0.1.zip")
        _, _, data = self.uploaded("test-pack-Server-0.0.1.zip")
        with zipfile.ZipFile(io.BytesIO(data)) as server_pack:
            self.assertEqual({"server-setup-config.yaml", "start-server.sh"}, set(server_pack.namelist()))
            self.assertEqual(FILES["server_files/start-server.sh"], server_pack.read("start-server.sh").decode())
        url = release.cdn_url(client_id, "test-pack-0.0.1.zip")
        self.assertEqual(f"install:\n    loaderVersion: 26.1.2.126\n    modpackUrl: {url}\n"
                         f"    formatSpecific:\n        ignoreProject:\n"
                         f"            - 11 # A\n            - 13 # Zoom\n"
                         f"    additionalFiles:\n"
                         f"        - url: https://mediafilez.forgecdn.net/files/1100/1/a-1.0.jar\n"
                         f"          destination: mods/a-1.0.jar\n",
                         self.server_config())
        self.assertEqual("https://mediafilez.forgecdn.net/files/9338/333/test-pack-0.0.1.zip",
                         release.cdn_url(9338333, "test-pack-0.0.1.zip"))
        self.assertEqual("https://mediafilez.forgecdn.net/files/8008/320/BetterF3-18.0.2%2BNeoForge-26.1.jar",
                         release.cdn_url(8008320, "BetterF3-18.0.2+NeoForge-26.1.jar"))

    def test_the_server_pack_installs_the_tagged_neoforge(self):
        # ServerStarter can't read a NeoForge version from the manifest, so the server pack names the tag's.
        self.folder.write("pack.toml", self.folder.read("pack.toml").replace("26.1.2.126", "26.1.2.130"))
        self.folder.add_entry("A newer NeoForge.")
        self.folder.cut("0.0.2")
        self.publish("0.0.2")
        self.assertIn("    loaderVersion: 26.1.2.130\n", self.server_config("0.0.2"))

    def test_the_server_pack_downloads_the_mods_of_the_tagged_manifest_but_the_client_only_ones(self):
        # Marking a mod client-only after the tag doesn't change the release's server pack.
        self.folder.write("mods/a.pw.toml", mod("A", "a-1.0.jar", 11, side="client"))
        self.folder.commit("A is client-only")
        self.publish()
        self.assertIn("destination: mods/a-1.0.jar\n", self.server_config())
        self.assertNotIn("zoom-1.0.jar", self.server_config())
        # A client-only mod is still ignored, so ServerStarter never looks it up.
        self.assertIn("            - 13 # Zoom\n", self.server_config())

    def test_a_server_config_that_lists_a_mod_of_the_pack_by_hand_is_refused(self):
        folder = PackFolder(self, {**FILES, "server_files/server-setup-config.yaml": FILES[
            "server_files/server-setup-config.yaml"].replace(
            "additionalFiles: []\n", "additionalFiles:\n        - url: https://example.com/a-1.0.jar\n"
                                     "          destination: mods/a-1.0.jar\n")})
        folder.cut("0.0.1")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            folder.publish("0.0.1", self.curseforge, self.github)
        self.assertIn("lists a-1.0.jar in additionalFiles by hand", "\n".join(folder.out))

    def test_a_server_config_that_lists_ignored_projects_by_hand_is_refused(self):
        folder = PackFolder(self, {**FILES, "server_files/server-setup-config.yaml": FILES[
            "server_files/server-setup-config.yaml"].replace(
            "ignoreProject: []\n", "ignoreProject:\n            - 99 # Old\n")})
        folder.cut("0.0.1")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            folder.publish("0.0.1", self.curseforge, self.github)
        self.assertIn("lists ignoreProject by hand", "\n".join(folder.out))

    def test_a_server_config_without_its_neoforge_placeholder_is_refused(self):
        folder = PackFolder(self, {**FILES, "server_files/server-setup-config.yaml": FILES[
            "server_files/server-setup-config.yaml"].replace("<the-neoforge-version>", "26.1.2.100")})
        folder.cut("0.0.1")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            folder.publish("0.0.1", self.curseforge, self.github)
        self.assertIn("loaderVersion", "\n".join(folder.out))

    def test_the_server_pack_keeps_the_server_configs_line_endings(self):
        self.folder.write("server_files/server-setup-config.yaml", FILES["server_files/server-setup-config.yaml"]
                          .replace("\n", "\r\n"))
        self.folder.add_entry("CRLF.")
        self.folder.cut("0.0.2")
        self.publish("0.0.2")
        config = self.server_config("0.0.2").encode()
        self.assertIn(b"        ignoreProject:\r\n            - 11 # A\r\n", config)
        self.assertIn(b"          destination: mods/a-1.0.jar\r\n", config)
        self.assertNotIn(b"\n", config.replace(b"\r\n", b""))

    def test_a_mod_without_a_curseforge_file_is_refused(self):
        self.folder.write("mods/zoom.pw.toml", 'name = "Zoom"\nfilename = "zoom-1.0.jar"\nside = "client"\n')
        self.folder.add_entry("Zoom from elsewhere.")
        self.folder.cut("0.0.2")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            self.publish("0.0.2")
        self.assertIn("Zoom has no CurseForge file", "\n".join(self.folder.out))

    def test_a_dry_run_shows_both_files_as_release_files_named_as_an_alpha_and_keeps_no_record(self):
        out = self.folder.out
        self.folder.publish("0.0.1", DryCurseForge("1", out.append), DryGitHub("example/test-pack", out.append),
                            dry_run=True)
        printed = "\n".join(out)
        self.assertIn("POST https://minecraft.curseforge.com/api/projects/1/upload-file", printed)
        self.assertIn('"displayName": "Test Pack 0.0.1 alpha"', printed)
        self.assertIn('"displayName": "Test Pack Server 0.0.1 alpha"', printed)
        self.assertIn('"releaseType": "release"', printed)
        self.assertIn("file: test-pack-0.0.1.zip", printed)
        self.assertIn("file: test-pack-Server-0.0.1.zip", printed)
        self.assertIn("ignoreProject 2 projects; additionalFiles adds 1 mods from the CDN", printed)
        self.assertIn("gh release create test-v0.0.1", printed)
        self.assertFalse((self.folder.pack.client_zip("0.0.1").parent / "record.json").exists())

    def test_a_dry_run_points_the_server_pack_at_a_given_file_id(self):
        out = self.folder.out
        self.folder.publish("0.0.1", DryCurseForge("1", out.append), DryGitHub("example/test-pack", out.append),
                            dry_run=True, client_file_id=9338333)
        self.assertIn("modpackUrl https://mediafilez.forgecdn.net/files/9338/333/test-pack-0.0.1.zip", "\n".join(out))

    def test_a_failed_server_pack_is_retried_from_the_record_without_a_file_id(self):
        self.curseforge.refusing.add("test-pack-Server-0.0.1.zip")
        with self.assertRaisesRegex(Refused, "server failed"):
            self.publish()
        client_id, _, _ = self.uploaded("test-pack-0.0.1.zip")
        record = self.folder.record("0.0.1")
        self.assertEqual((client_id, ["curseforge", "github"]), (record.client_file_id, record.sites))

        self.curseforge.refusing.clear()
        self.publish()

        _, server, _ = self.uploaded("test-pack-Server-0.0.1.zip")
        self.assertEqual(client_id, server["parentFileId"])
        self.assertEqual(2, len(self.curseforge.uploads))
        self.assertEqual(["curseforge", "github", "server"], self.folder.record("0.0.1").sites)

    def test_server_and_github_need_a_file_id_when_the_record_has_none(self):
        with self.assertRaisesRegex(Refused, "server failed"):
            self.publish(only="server")
        self.assertIn("--client-file-id", "\n".join(self.folder.out))
        self.publish(only="server", client_file_id=9338333)
        _, server, _ = self.uploaded("test-pack-Server-0.0.1.zip")
        self.assertEqual(9338333, server["parentFileId"])

    def test_refuses_a_zip_the_tag_did_not_cut(self):
        self.folder.pack.client_zip("0.0.1").write_bytes(b"another export")
        with self.assertRaisesRegex(Refused, "isn't the zip test-v0.0.1 cut"):
            self.publish()
        self.assertEqual({}, self.curseforge.uploads)

    def test_a_site_that_has_the_version_is_refused_and_the_others_still_run(self):
        self.github.releases["test-v0.0.1"] = ("Test Pack 0.0.1 alpha", "", {})
        self.publish()
        self.assertEqual(2, len(self.curseforge.uploads))
        self.assertIn("github: GitHub already has a release for test-v0.0.1", "\n".join(self.folder.out))

    def test_a_release_every_site_has_is_refused_as_published(self):
        self.curseforge.names |= {"test-pack-0.0.1.zip", "test-pack-Server-0.0.1.zip"}
        self.github.releases["test-v0.0.1"] = ("Test Pack 0.0.1 alpha", "", {})
        with self.assertRaises(Published):
            self.publish(client_file_id=9338333)

    def test_github_is_refused_until_origin_has_the_tag(self):
        self.github.pushed = False
        with self.assertRaisesRegex(Refused, "github failed"):
            self.publish()
        self.assertIn("push it first", "\n".join(self.folder.out))
        self.assertEqual({}, self.github.releases)

        self.github.pushed = True
        self.publish()
        self.assertIn("test-v0.0.1", self.github.releases)
        self.assertEqual(2, len(self.curseforge.uploads))

    def test_one_site_is_sent_whatever_the_record_says(self):
        self.publish()
        self.github.releases.clear()
        self.publish(only="github")
        self.assertIn("test-v0.0.1", self.github.releases)
        self.assertEqual(2, len(self.curseforge.uploads))

    def test_every_site_is_refused_until_the_pack_has_a_curseforge_project(self):
        # The server pack names the client zip's CurseForge download, and GitHub's release carries it.
        folder = PackFolder(self, {**FILES, "tools/release.toml": 'tag-prefix = "test-v"\nslug = "test-pack"\n'
                                                                  'github-repo = "example/test-pack"\n'})
        folder.cut("0.0.1")
        with self.assertRaisesRegex(Refused, "curseforge, server, github failed"):
            folder.publish("0.0.1", self.curseforge, self.github)
        self.assertIn("no curseforge-project", "\n".join(folder.out))
        self.assertEqual({}, self.curseforge.uploads)
        self.assertEqual({}, self.github.releases)


if __name__ == "__main__":
    unittest.main()
