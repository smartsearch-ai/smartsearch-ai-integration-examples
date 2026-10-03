"""Run direct Java response readers against a loopback fixture, never production.

First compile/build the classpath as run.sh does. Then run:
    python3 tests/test_public_examples.py
"""

import http.server
import json
import os
import pathlib
import subprocess
import threading
import unittest


class Handler(http.server.BaseHTTPRequestHandler):
    """Minimal public token, Core and Workplace fixture."""

    core_requests = 0

    def log_message(self, *_args):
        """Keep fixture logs free of request data."""

    def do_POST(self):
        """Serve synthetic credentials, application failures or a final-only stream."""
        self.rfile.read(int(self.headers.get("Content-Length", 0)))
        if self.path.endswith("/token"):
            content = json.dumps({"access_token": "fake-safe-token", "expires_in": 120,
                                  "token_type": "Bearer"}).encode()
            content_type = "application/json"
        elif "/core/" in self.path:
            Handler.core_requests += 1
            content = json.dumps({"code": 0, "message": "do-not-log", "result": {}}).encode()
            content_type = "application/json"
        else:
            content = b'event: run.completed\ndata: {"payload":{"status":"COMPLETED","answer":"Final fixture answer","sources":[]}}\n\n'
            content_type = "text/event-stream"
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(content)))
        self.end_headers()
        self.wfile.write(content)


class PublicExampleTests(unittest.TestCase):
    """Check Java application-failure guards and final stream output through the SDK."""

    @classmethod
    def setUpClass(cls):
        cls.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), Handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        origin = f"http://127.0.0.1:{cls.server.server_port}"
        cls.environment = {**os.environ,
            "SMARTSEARCH_API_BASE_URL": origin, "SMARTSEARCH_ADMIN_BASE_URL": origin,
            "SMARTSEARCH_AUTH_BASE_URL": origin, "SMARTSEARCH_REALM": "example",
            "SMARTSEARCH_CLIENT_ID": "svc-example", "SMARTSEARCH_CLIENT_SECRET": "fake-secret",
            "SMARTSEARCH_PROJECT_ID": "demo-project",
            "SMARTSEARCH_WORKSPACE_ID": "00000000-0000-4000-8000-000000000001"}
        cls.classpath = "target/classes:" + pathlib.Path("target/example-classpath.txt").read_text().strip()

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join()

    def run_example(self, name, topic):
        return subprocess.run(["java", "-cp", self.classpath, f"examples.{topic}.{name}"],
                              env=self.environment, capture_output=True, text=True, timeout=30)

    def test_core_application_failures(self):
        """Every direct reader refuses code-zero HTTP-success responses before showing empty hits."""
        for name in ["FirstSearch", "FacetCounts", "HighlightMatches", "RunSeveralSearchesAtOnce",
                     "TurnOffQueryExpansion", "HandleErrorsAndWarnings", "KeywordVsSemanticVsHybrid"]:
            with self.subTest(example=name):
                count = Handler.core_requests
                result = self.run_example(name, "search")
                self.assertGreater(Handler.core_requests, count, result.stderr)
                self.assertEqual(result.returncode, 1)
                self.assertIn("application failure", result.stderr)
                self.assertNotIn("do-not-log", result.stdout + result.stderr)
                self.assertNotIn("hits=0", result.stdout)

    def test_final_answer_without_deltas(self):
        """A completion without provisional answer deltas still displays the authoritative answer."""
        for name in ["StreamAnAnswer", "StreamChatAnswer"]:
            with self.subTest(example=name):
                result = self.run_example(name, "workplace")
                self.assertEqual(result.returncode, 0, result.stderr)
                self.assertIn("Final fixture answer", result.stdout)
                self.assertIn("COMPLETED", result.stdout)


if __name__ == "__main__":
    unittest.main()
