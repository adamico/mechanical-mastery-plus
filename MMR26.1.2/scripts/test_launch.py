import importlib.util
import os
from pathlib import Path

spec = importlib.util.spec_from_file_location("launch", Path(__file__).with_name("launch.py"))
launch = importlib.util.module_from_spec(spec)
spec.loader.exec_module(launch)


def touch(path, mtime):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("")
    os.utime(path, (mtime, mtime))


def test_no_jar_is_stale(tmp_path):
    touch(tmp_path / "src/A.java", 100)
    assert launch.stale(tmp_path)


def test_jar_older_than_a_source_is_stale(tmp_path):
    touch(tmp_path / "build/libs/m-1.0.jar", 100)
    touch(tmp_path / "src/main/resources/x.json", 200)
    assert launch.stale(tmp_path)


def test_jar_newer_than_every_source_is_fresh(tmp_path):
    touch(tmp_path / "src/A.java", 100)
    touch(tmp_path / "build.gradle", 100)
    touch(tmp_path / "build/libs/m-1.0.jar", 200)
    touch(tmp_path / "build/libs/m-1.0-sources.jar", 50)
    assert not launch.stale(tmp_path)


def test_a_newer_sources_jar_does_not_count_as_the_build(tmp_path):
    touch(tmp_path / "src/A.java", 150)
    touch(tmp_path / "build/libs/m-1.0.jar", 100)
    touch(tmp_path / "build/libs/m-1.0-sources.jar", 200)
    assert launch.stale(tmp_path)
