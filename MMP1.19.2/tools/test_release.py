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
    "pack.toml": 'name = "Test Pack"\nversion = "0.0.0"\npack-format = "packwiz:1.1.0"\n\n'
                 '[index]\nfile = "index.toml"\nhash-format = "sha256"\nhash = ""\n\n'
                 '[versions]\nforge = "43.2.23"\nminecraft = "1.19.2"\n',
    "index.toml": 'hash-format = "sha256"\n',
    "CHANGELOG.md": "# Changelog\n\n## Unreleased\n\n- The first release.\n",
    "config/bcc-common.toml": '[general]\n\tmodpackVersion = "0.0.0"\n',
    "tools/release.toml": 'tag-prefix = "test-v"\nslug = "test-pack"\ncurseforge-project = 1\n'
                          'github-repo = "example/test-pack"\n',
    "server_files/server-setup-config.yaml": "install:\n    modpackUrl: <the-client-zip-url>\n"
                                             "    formatSpecific:\n        ignoreProject: []\n"
                                             "    additionalFiles:\n        - url: https://example.com/extra.jar\n"
                                             "          destination: mods/extra.jar\n"
                                             "        #- url: https://example.com/old.jar\n",
    "server_files/start-server.sh": "#!/bin/bash\njava -jar serverstarter.jar\n",
}


def mod(name, filename, project, side="both"):
    return (f'name = "{name}"\nfilename = "{filename}"\nside = "{side}"\n\n'
            f'[update.curseforge]\nfile-id = {project}0001\nproject-id = {project}\n')


class PackFolder:
    """A pack folder one level down in a fresh git repo, like MMP1.19.2/ in this one."""

    def __init__(self, test):
        folder = tempfile.TemporaryDirectory()
        test.addCleanup(folder.cleanup)
        self.repo = Path(folder.name)
        self.root = self.repo / "pack"
        self.git("init", "-q", "-b", "main")
        for key, value in [("user.name", "Test"), ("user.email", "test@example.com"),
                           ("commit.gpgsign", "false"), ("tag.gpgsign", "false")]:
            self.git("config", key, value)
        self.write(".gitignore", "releases\n*.zip\n", at=self.repo)
        for path, text in FILES.items():
            self.write(path, text)
        self.write("mods/a.pw.toml", mod("A", "a-1.0.jar", 11))
        self.write("mods/zoom.pw.toml", mod("Zoom", "zoom-1.0.jar", 13, side="client"))
        self.write("resourcepacks/look.pw.toml", mod("Look", "look.zip", 15))
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

    def cut(self, version, packwiz=None):
        release.cut(self.pack, version, packwiz or FakePackwiz(self.root), out=self.out.append)

    def publish(self, version, curseforge, github, **options):
        release.publish(self.pack, version, curseforge, github, out=self.out.append, **options)

    def record(self, version):
        return release.Record(self.pack.client_zip(version).parent / "record.json")


class Cut(unittest.TestCase):
    def setUp(self):
        self.folder = PackFolder(self)

    def test_refuses_a_pack_folder_with_changes(self):
        self.folder.write("mods/b.pw.toml", mod("B", "b-1.0.jar", 12))
        with self.assertRaisesRegex(Refused, "has changes"):
            self.folder.cut("1.0.0")

    def test_refuses_a_version_already_tagged(self):
        self.folder.cut("1.0.0")
        self.folder.write("CHANGELOG.md", self.folder.read("CHANGELOG.md").replace(
            "## Unreleased\n", "## Unreleased\n\n- More.\n"))
        self.folder.commit("More")
        with self.assertRaisesRegex(Refused, "test-v1.0.0 already exists"):
            self.folder.cut("1.0.0")

    def test_a_failed_export_leaves_every_file_as_it_was(self):
        before = {path: self.folder.read(path) for path in release.CUT_FILES}
        with self.assertRaisesRegex(Refused, "export failed"):
            self.folder.cut("1.0.0", FakePackwiz(self.folder.root, failing=True))
        self.assertEqual(before, {path: self.folder.read(path) for path in release.CUT_FILES})
        self.assertEqual("", self.folder.git("status", "--porcelain", "--ignored"))
        self.assertEqual("", self.folder.git("tag", "-l"))

    def test_writes_the_changelog_section_bumps_the_version_and_tags_the_zip(self):
        self.folder.cut("1.0.0")
        self.folder.write("mods/a.pw.toml", mod("A", "a-1.1.jar", 11))
        self.folder.write("mods/b.pw.toml", mod("B", "b-1.0.jar", 12))
        self.folder.write("CHANGELOG.md", self.folder.read("CHANGELOG.md").replace(
            "## Unreleased\n", "## Unreleased\n\n- Polonium is cheaper.\n"))
        self.folder.commit("Update A, add B")

        self.folder.cut("1.1.0")

        changelog = self.folder.read("CHANGELOG.md")
        self.assertIn("## Unreleased\n\n## 1.1.0\n\n- Polonium is cheaper.\n\n### Mods\n\n"
                      "- Added B (`b-1.0.jar`)\n- Updated A: `a-1.0.jar` → `a-1.1.jar`\n\n## 1.0.0\n", changelog)
        self.assertIn('version = "1.1.0"', self.folder.read("pack.toml"))
        self.assertIn('modpackVersion = "1.1.0"', self.folder.read("config/bcc-common.toml"))
        zip_data = self.folder.pack.client_zip("1.1.0").read_bytes()
        self.assertIn(f"zip sha256 {hashlib.sha256(zip_data).hexdigest()}",
                      self.folder.git("tag", "-l", "--format=%(contents)", "test-v1.1.0"))
        self.assertEqual("Test Pack 1.1.0", self.folder.git("log", "-1", "--format=%s").strip())
        self.assertEqual("", self.folder.git("status", "--porcelain"))

    def test_refuses_a_release_with_nothing_in_it(self):
        self.folder.cut("1.0.0")
        with self.assertRaisesRegex(Refused, "nothing changed since test-v1.0.0"):
            self.folder.cut("1.0.1")


class Publish(unittest.TestCase):
    def setUp(self):
        self.folder = PackFolder(self)
        self.folder.cut("1.0.0")
        self.curseforge, self.github = FakeCurseForge(), FakeGitHub()

    def publish(self, **options):
        self.folder.publish("1.0.0", self.curseforge, self.github, **options)

    def uploaded(self, file_name):
        return next((file_id, metadata, data) for file_id, (metadata, name, data) in self.curseforge.uploads.items()
                    if name == file_name)

    def test_sends_the_client_zip_then_the_server_pack_under_it_then_the_github_release(self):
        self.publish()
        client_id, client, _ = self.uploaded("test-pack-1.0.0.zip")
        _, server, _ = self.uploaded("test-pack-Server-1.0.0.zip")
        self.assertEqual(("Test Pack 1.0.0", [1, 2], "- The first release."),
                         (client["displayName"], client["gameVersions"], client["changelog"]))
        self.assertEqual(("Test Pack Server 1.0.0", client_id), (server["displayName"], server["parentFileId"]))
        title, notes, files = self.github.releases["test-v1.0.0"]
        self.assertEqual({"test-pack-1.0.0.zip", "test-pack-Server-1.0.0.zip"}, set(files))
        self.assertEqual(["curseforge", "server", "github"], self.folder.record("1.0.0").sites)

    def test_refuses_a_zip_the_tag_did_not_cut(self):
        self.folder.pack.client_zip("1.0.0").write_bytes(b"another export")
        with self.assertRaisesRegex(Refused, "isn't the zip test-v1.0.0 cut"):
            self.publish()
        self.assertEqual({}, self.curseforge.uploads)

    def test_a_site_that_has_the_version_is_refused_and_the_others_still_run(self):
        self.github.releases["test-v1.0.0"] = ("Test Pack 1.0.0", "", {})
        self.publish()
        self.assertEqual(2, len(self.curseforge.uploads))
        self.assertIn("github: GitHub already has a release for test-v1.0.0", "\n".join(self.folder.out))

    def test_a_release_every_site_has_is_refused_as_published(self):
        self.curseforge.names |= {"test-pack-1.0.0.zip", "test-pack-Server-1.0.0.zip"}
        self.github.releases["test-v1.0.0"] = ("Test Pack 1.0.0", "", {})
        with self.assertRaises(Published):
            self.publish(client_file_id=9338333)

    def test_a_failed_server_pack_is_retried_from_the_record_without_a_file_id(self):
        self.curseforge.refusing.add("test-pack-Server-1.0.0.zip")
        with self.assertRaisesRegex(Refused, "server failed"):
            self.publish()
        client_id, _, _ = self.uploaded("test-pack-1.0.0.zip")
        self.assertEqual((client_id, ["curseforge", "github"]),
                         (self.folder.record("1.0.0").client_file_id, self.folder.record("1.0.0").sites))

        self.curseforge.refusing.clear()
        self.publish()

        _, server, _ = self.uploaded("test-pack-Server-1.0.0.zip")
        self.assertEqual(client_id, server["parentFileId"])
        self.assertEqual(2, len(self.curseforge.uploads))
        self.assertEqual(["curseforge", "github", "server"], self.folder.record("1.0.0").sites)

    def test_the_server_pack_holds_the_tagged_server_files_pointed_at_the_client_zip(self):
        # server_files/ moving on after the tag doesn't change the release's server pack.
        self.folder.write("server_files/start-server.sh", "#!/bin/bash\necho changed\n")
        self.folder.commit("Change the server files")
        self.publish()
        client_id, _, _ = self.uploaded("test-pack-1.0.0.zip")
        _, _, data = self.uploaded("test-pack-Server-1.0.0.zip")
        with zipfile.ZipFile(io.BytesIO(data)) as server_pack:
            self.assertEqual({"server-setup-config.yaml", "start-server.sh"}, set(server_pack.namelist()))
            self.assertEqual(FILES["server_files/start-server.sh"], server_pack.read("start-server.sh").decode())
            url = release.cdn_url(client_id, "test-pack-1.0.0.zip")
            self.assertEqual(f"install:\n    modpackUrl: {url}\n    formatSpecific:\n        ignoreProject:\n"
                             f"            - 11 # A\n            - 13 # Zoom\n            - 15 # Look\n"
                             f"    additionalFiles:\n        - url: https://example.com/extra.jar\n"
                             f"          destination: mods/extra.jar\n"
                             f"        - url: https://mediafilez.forgecdn.net/files/1100/1/a-1.0.jar\n"
                             f"          destination: mods/a-1.0.jar\n"
                             f"        #- url: https://example.com/old.jar\n",
                             server_pack.read("server-setup-config.yaml").decode())
        self.assertEqual("https://mediafilez.forgecdn.net/files/9338/333/test-pack-1.0.0.zip",
                         release.cdn_url(9338333, "test-pack-1.0.0.zip"))
        self.assertEqual("https://mediafilez.forgecdn.net/files/4153/347/Clumps-forge-1.19.2-9.0.0%2B14.jar",
                         release.cdn_url(4153347, "Clumps-forge-1.19.2-9.0.0+14.jar"))

    def server_config(self):
        _, _, data = self.uploaded("test-pack-Server-1.0.0.zip")
        with zipfile.ZipFile(io.BytesIO(data)) as server_pack:
            return server_pack.read("server-setup-config.yaml").decode()

    def test_the_server_pack_downloads_the_mods_of_the_tagged_manifest_but_the_client_only_ones(self):
        # Marking a mod client-only after the tag doesn't change the release's server pack.
        self.folder.write("mods/a.pw.toml", mod("A", "a-1.0.jar", 11, side="client"))
        self.folder.commit("A is client-only")
        self.publish()
        self.assertIn("destination: mods/a-1.0.jar\n", self.server_config())
        self.assertNotIn("zoom-1.0.jar", self.server_config())
        self.assertNotIn("look.zip", self.server_config())

    def test_a_server_config_that_lists_a_mod_of_the_pack_by_hand_is_refused(self):
        self.folder = PackFolder(self)
        self.folder.write("server_files/server-setup-config.yaml", FILES["server_files/server-setup-config.yaml"]
                          .replace("mods/extra.jar", "mods/a-1.0.jar"))
        self.folder.commit("A by hand")
        self.folder.cut("1.0.0")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            self.publish()
        self.assertIn("lists a-1.0.jar in additionalFiles by hand", "\n".join(self.folder.out))

    def test_a_server_config_that_lists_ignored_projects_by_hand_is_refused(self):
        self.folder = PackFolder(self)
        self.folder.write("server_files/server-setup-config.yaml",
                          "install:\n    modpackUrl: <url>\n        ignoreProject:\n            - 99 # Old\n"
                          "    additionalFiles: []\n")
        self.folder.commit("A hand-kept list")
        self.folder.cut("1.0.0")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            self.publish()
        self.assertIn("lists ignoreProject by hand", "\n".join(self.folder.out))

    def test_the_ignore_list_keeps_the_server_configs_line_endings(self):
        self.folder.write("server_files/server-setup-config.yaml", FILES["server_files/server-setup-config.yaml"]
                          .replace("\n", "\r\n"))
        self.folder.write("CHANGELOG.md", self.folder.read("CHANGELOG.md").replace(
            "## Unreleased\n", "## Unreleased\n\n- More.\n"))
        self.folder.commit("CRLF")
        self.folder.cut("1.0.1")
        self.folder.publish("1.0.1", self.curseforge, self.github)
        _, _, data = self.uploaded("test-pack-Server-1.0.1.zip")
        with zipfile.ZipFile(io.BytesIO(data)) as server_pack:
            config = server_pack.read("server-setup-config.yaml")
        self.assertIn(b"        ignoreProject:\r\n            - 11 # A\r\n", config)
        self.assertIn(b"          destination: mods/a-1.0.jar\r\n        #- url", config)
        self.assertNotIn(b"\n", config.replace(b"\r\n", b""))

    def test_a_mod_without_a_curseforge_file_is_refused(self):
        self.folder.write("mods/zoom.pw.toml", 'name = "Zoom"\nfilename = "zoom-1.0.jar"\nside = "client"\n')
        self.folder.write("CHANGELOG.md", self.folder.read("CHANGELOG.md").replace(
            "## Unreleased\n", "## Unreleased\n\n- More.\n"))
        self.folder.commit("Zoom from elsewhere")
        self.folder.cut("1.0.1")
        with self.assertRaisesRegex(Refused, "server, github failed"):
            self.folder.publish("1.0.1", self.curseforge, self.github)
        self.assertIn("Zoom has no CurseForge file", "\n".join(self.folder.out))

    def test_github_is_refused_until_origin_has_the_tag(self):
        self.github.pushed = False
        with self.assertRaisesRegex(Refused, "github failed"):
            self.publish()
        self.assertIn("push it first", "\n".join(self.folder.out))
        self.assertEqual({}, self.github.releases)

        self.github.pushed = True
        self.publish()
        self.assertIn("test-v1.0.0", self.github.releases)
        self.assertEqual(2, len(self.curseforge.uploads))

    def test_server_and_github_need_a_file_id_when_the_record_has_none(self):
        with self.assertRaisesRegex(Refused, "server failed"):
            self.publish(only="server")
        self.assertIn("--client-file-id", "\n".join(self.folder.out))
        self.publish(only="server", client_file_id=9338333)
        _, server, _ = self.uploaded("test-pack-Server-1.0.0.zip")
        self.assertEqual(9338333, server["parentFileId"])

    def test_a_dry_run_prints_the_server_pack_for_the_given_file_id_and_keeps_no_record(self):
        out = self.folder.out
        self.folder.publish("1.0.0", DryCurseForge("1", out.append), DryGitHub("example/test-pack", out.append),
                            dry_run=True, client_file_id=9338333)
        self.assertIn("modpackUrl https://mediafilez.forgecdn.net/files/9338/333/test-pack-1.0.0.zip", "\n".join(out))
        self.assertIn("ignoreProject 3 projects; additionalFiles adds 1 mods from the CDN", "\n".join(out))
        self.assertFalse((self.folder.pack.client_zip("1.0.0").parent / "record.json").exists())


if __name__ == "__main__":
    unittest.main()
