"""Offline launcher regression: shell arguments arrive unchanged at Java.

Run from examples/java: python3 tests/test_runner.py
Maven and Java are replaced with local mocks; no service calls are made.
"""

import json
import os
import pathlib
import subprocess
import sys
import tempfile
import unittest


class RunnerTests(unittest.TestCase):
    """Keep shell quoting out of Maven's string-based exec argument parser."""

    def test_argv_preserved(self):
        """Apostrophes, quotes, whitespace, Unicode and empty arguments survive."""
        with tempfile.TemporaryDirectory() as directory:
            mock_path = pathlib.Path(directory)
            maven = mock_path / "mvn"
            maven.write_text(
                "#!/usr/bin/env bash\nmkdir -p target\nprintf '%s' dummy > target/example-classpath.txt\n"
            )
            java = mock_path / "java"
            java.write_text(
                "#!" + sys.executable + "\nimport json, sys\nprint(json.dumps(sys.argv[4:]))\n"
            )
            maven.chmod(0o755)
            java.chmod(0o755)
            values = ["What's new?", 'say "hello"', "two words", "", "café ☕", "$(no-command)"]
            environment = {**os.environ, "PATH": str(mock_path) + os.pathsep + os.environ["PATH"]}
            result = subprocess.run(
                ["./run.sh", "FirstSearch", *values], env=environment,
                check=True, capture_output=True, text=True,
            )
            self.assertEqual(json.loads(result.stdout), values)


if __name__ == "__main__":
    unittest.main()
