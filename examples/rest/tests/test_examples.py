"""Offline REST regressions. Run: python3 -m unittest discover -s tests -v.

Only loopback fixtures and synthetic credentials are used. A real .env is never read.
"""

import base64
import http.server
import json
import os
import pathlib
import subprocess
import tempfile
import threading
import unittest
import urllib.parse

ROOT = pathlib.Path(__file__).resolve().parents[1]


class Fixture(http.server.BaseHTTPRequestHandler):
    """Serve synthetic public API responses."""

    requests = []
    scenario = "normal"

    def log_message(self, *_args):
        """Suppress request logging."""

    def do_GET(self):
        self.handle_request()

    def do_POST(self):
        self.handle_request()

    def handle_request(self):
        raw = self.rfile.read(int(self.headers.get("Content-Length", 0)))
        content_type = self.headers.get("Content-Type", "")
        body = json.loads(raw) if "application/json" in content_type and raw else {}
        form = (
            urllib.parse.parse_qs(raw.decode())
            if "x-www-form-urlencoded" in content_type
            else {}
        )
        self.requests.append(
            {
                "path": self.path,
                "method": self.command,
                "body": body,
                "form": form,
                "auth": self.headers.get("Authorization"),
                "idempotency": self.headers.get("Idempotency-Key"),
            }
        )
        if self.path.endswith("/token"):
            self.reply(
                {
                    "access_token": "fake-safe-token",
                    "expires_in": 120,
                    "token_type": "Bearer",
                }
            )
        elif self.scenario == "redirect":
            self.send_response(302)
            self.send_header("Location", "/redirect-destination")
            self.send_header("Content-Length", "0")
            self.end_headers()
        elif self.scenario == "denied":
            self.reply(
                {"error": {"code": "SCOPE_DENIED", "message": "fake-secret &+=?"}}, 403
            )
        elif "/core/" in self.path:
            if self.scenario == "core_failure":
                self.reply({"code": 0, "message": "fake-secret &+=?", "result": {}})
            elif "no-such-project" in self.path:
                self.reply({"code": "PROJECT_NOT_FOUND"}, 404)
            else:
                hits = {
                    "hits": [
                        {
                            "_source": {"title": "Fixture movie"},
                            "highlight": {"title": ["<em>Fixture</em> movie"]},
                        }
                    ]
                }
                self.reply(
                    {
                        "code": 1,
                        "effective_neural_mode": "BM25",
                        "result": {
                            "hits": hits,
                            "responses": [{"hits": hits}] * 3,
                            "aggregations": {
                                "languages": {
                                    "buckets": [{"key": "en", "doc_count": 1}]
                                }
                            },
                        },
                    }
                )
        elif body.get("options", {}).get("stream"):
            frame = 'event: run.started\r\ndata: {"payload":{"session_id":"demo-session"}}\r\n\r\n'
            frame += (
                'event: answer.delta\r\ndata: {"payload":{"text":"café ☕"}}\r\n\r\n'
            )
            if self.scenario != "incomplete":
                frame += 'event: run.completed\r\ndata: {"payload":{"status":"COMPLETED","answer":"Final fixture answer","sources":[]}}\r\n\r\n'
            self.reply_bytes(frame.encode(), "text/event-stream")
        elif self.path.endswith("/jobs"):
            self.reply({"job_id": "demo-job", "state": "QUEUED"}, 202)
        elif self.path.endswith("/demo-job"):
            self.reply(
                {"state": "RUNNING" if self.scenario == "job_timeout" else "SUCCEEDED"}
            )
        else:
            self.reply(
                {
                    "documents": [],
                    "sources": [{"title": "Permitted evidence"}],
                    "values": [],
                    "answer": "Fixture answer",
                    "session_id": "demo-session",
                    "status": "COMPLETED",
                }
            )

    def reply(self, body, status=200):
        self.reply_bytes(json.dumps(body).encode(), "application/json", status)

    def reply_bytes(self, content, content_type, status=200):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(content)))
        self.end_headers()
        self.wfile.write(content)


class RestTests(unittest.TestCase):
    """Test the real curl scripts, using no SDK or production credentials."""

    @classmethod
    def setUpClass(cls):
        cls.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), Fixture)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.directory = tempfile.TemporaryDirectory()
        origin = f"http://127.0.0.1:{cls.server.server_port}"
        cls.env = {
            "PATH": os.environ["PATH"],
            "HOME": cls.directory.name,
            "LANG": "en_US.UTF-8",
            "SMARTSEARCH_REST_ENV_FILE": "/dev/null",
            "SMARTSEARCH_API_BASE_URL": origin + "/api",
            "SMARTSEARCH_ADMIN_BASE_URL": origin + "/admin",
            "SMARTSEARCH_AUTH_BASE_URL": origin + "/auth",
            "SMARTSEARCH_REALM": "example",
            "SMARTSEARCH_CLIENT_ID": "svc-example",
            "SMARTSEARCH_CLIENT_SECRET": "fake-secret &+=?",
            "SMARTSEARCH_PROJECT_ID": "demo-project",
            "SMARTSEARCH_USECASE_ID": "demo-usecase",
            "SMARTSEARCH_WORKSPACE_ID": "demo-workspace",
            "SMARTSEARCH_SOURCE_ID": "demo-source",
            "SMARTSEARCH_TENANT_ID": "demo tenant",
            "SMARTSEARCH_INTEGRATION_ID": "demo-integration",
            "SMARTSEARCH_USER_ACCESS_TOKEN": "fake-user-token",
            "SMARTSEARCH_USER_ASSERTION": "fake.user.assertion",
        }

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()
        cls.thread.join()
        cls.directory.cleanup()

    def setUp(self):
        Fixture.scenario = "normal"
        Fixture.requests.clear()

    def run_example(self, name, *args, **overrides):
        return subprocess.run(
            ["/bin/bash", str(ROOT / "run.sh"), name, *args],
            env={**self.env, **overrides},
            capture_output=True,
            text=True,
            timeout=20,
        )

    def test_all_44_examples(self):
        """All scripts complete with their actual routes, bodies and curl calls."""
        rows = [
            row.split("|") for row in (ROOT / "examples.tsv").read_text().splitlines()
        ]
        self.assertEqual(len(rows), 44)
        for step, name, group, description in rows:
            with self.subTest(example=name):
                start = len(Fixture.requests)
                result = self.run_example(name)
                self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
                self.assertNotIn("fake-secret", result.stdout + result.stderr)
                self.assertNotIn("fake-safe-token", result.stdout + result.stderr)
                requests = [
                    row["body"] for row in Fixture.requests[start:] if row["body"]
                ]
                script = (ROOT / group / (name + ".sh")).read_text()
                if requests:
                    sample = script.split("# Request JSON:\n", 1)[1].split(
                        "# End request JSON.", 1
                    )[0]
                    lines = [
                        line[2:]
                        for line in sample.splitlines()
                        if line.startswith("# ")
                    ]
                    first = next(
                        i for i, line in enumerate(lines) if line in ("{", "[")
                    )
                    self.assertEqual(json.loads("\n".join(lines[first:])), requests[0])
        self.assertGreater(len(Fixture.requests), 100)
        for request in Fixture.requests:
            self.assertRegex(
                request["path"],
                r"^/(auth/realms/example/protocol/openid-connect/token|api/(core/(projects|usecases)/[^/]+/(search|mSearch)|workspace/v1/workspaces/[^/]+/(search|query|chat))|admin/search-admin/api/provisioning/v1/(capabilities|jobs(/demo-job(/items)?)?|integrations/demo-integration/principals))(\?.*)?$",
            )

    def test_auth_encoding_and_delegation(self):
        """Secrets are form-encoded; exchange includes repeated audiences and both token types."""
        result = self.run_example("search_workplace_as_user")
        self.assertEqual(result.returncode, 0, result.stderr)
        form = Fixture.requests[0]["form"]
        self.assertEqual(form["client_secret"], ["fake-secret &+=?"])
        self.assertEqual(
            form["audience"], ["workspace-api", "cloud-gateway", "search-admin"]
        )
        for field in ["subject_token_type", "requested_token_type"]:
            self.assertEqual(
                form[field], ["urn:ietf:params:oauth:token-type:access_token"]
            )
        Fixture.requests.clear()
        result = self.run_example("sign_in_with_your_identity_provider")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(
            Fixture.requests[0]["form"]["grant_type"],
            ["urn:ietf:params:oauth:grant-type:jwt-bearer"],
        )
        self.assertEqual(
            Fixture.requests[0]["form"]["assertion"], ["fake.user.assertion"]
        )

    def test_source_grants_idempotency_and_quoting(self):
        """Platform GUEST, source groups and source roles are separate; revision zero creates grants."""
        result = self.run_example("register_users_with_workspace_access")
        self.assertEqual(result.returncode, 0, result.stderr)
        request = next(row for row in Fixture.requests if row["path"].endswith("/jobs"))
        self.assertEqual(request["idempotency"], "sdk-example-onboard-users-v1")
        user = request["body"]["items"][0]
        self.assertEqual(user["platform_role"], "GUEST")
        grant = user["workspaces"][0]["source_grants"][0]
        self.assertEqual(grant["expected_revision"], 0)
        self.assertEqual(
            grant["grants"], {"groups": ["everyone"], "roles": [], "security_keys": []}
        )
        result = self.run_example(
            "filter_by_full_text_match", "What's new?", 'say "hello"'
        )
        self.assertEqual(result.returncode, 0, result.stderr)
        body = Fixture.requests[-1]["body"]
        self.assertEqual(body["q"], "What's new?")
        self.assertEqual(body["filters"]["all"][0]["value"], 'say "hello"')

    def test_failures_not_empty_success(self):
        """HTTP failure, code-zero search, incomplete SSE and unfinished jobs fail."""
        for scenario, name, code in [
            ("denied", "first_search", "SCOPE_DENIED"),
            ("core_failure", "first_search", "SEARCH_APPLICATION_FAILED"),
            ("incomplete", "stream_an_answer", "STREAM_FAILED"),
            ("job_timeout", "register_users", "PROVISIONING_JOB_TIMEOUT"),
        ]:
            with self.subTest(scenario=scenario):
                Fixture.scenario = scenario
                result = self.run_example(
                    name, SMARTSEARCH_REST_JOB_TIMEOUT_SECONDS="0"
                )
                self.assertNotEqual(result.returncode, 0)
                self.assertIn(code, result.stderr)
                self.assertNotIn("fake-secret", result.stderr)

    def test_help_and_missing_setting_offline(self):
        """Help/list do not require settings; missing client secret stops before HTTP."""
        for name in ["--list", "first_search"]:
            args = [] if name == "--list" else ["--help"]
            result = subprocess.run(
                ["/bin/bash", str(ROOT / "run.sh"), name, *args],
                env={"PATH": os.environ["PATH"]},
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.returncode, 0)
        result = self.run_example("first_search", SMARTSEARCH_CLIENT_SECRET="")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("SMARTSEARCH_CLIENT_SECRET", result.stderr)
        self.assertEqual(Fixture.requests, [])
        for example, setting in [
            ("search_workplace_as_user", "SMARTSEARCH_USER_ACCESS_TOKEN"),
            ("sign_in_with_your_identity_provider", "SMARTSEARCH_USER_ASSERTION"),
        ]:
            result = self.run_example(example, **{setting: ""})
            self.assertNotEqual(result.returncode, 0)
            self.assertIn(setting, result.stderr)
            self.assertEqual(Fixture.requests, [])

    def test_curlrc_ignored_and_redirects_refused(self):
        """User curl defaults cannot enable redirects, verbose credentials or insecure TLS."""
        (pathlib.Path(self.directory.name) / ".curlrc").write_text(
            "location\nverbose\ninsecure\n"
        )
        Fixture.scenario = "redirect"
        result = self.run_example("first_search")
        self.assertNotEqual(result.returncode, 0)
        self.assertNotIn("fake-safe-token", result.stderr)
        self.assertFalse(
            any(row["path"] == "/redirect-destination" for row in Fixture.requests)
        )

    def test_sse_framing_and_errors(self):
        """LF/CRLF/bare-CR complete frames succeed; EOF/failure/invalid frames fail."""
        for separator in ["\n", "\r\n", "\r"]:
            frame = f'event: run.completed{separator}data: {{"payload":{{"status":"COMPLETED","answer":"café ☕"}}}}{separator}{separator}'
            result = subprocess.run(
                ["python3", str(ROOT / "lib/sse.py")],
                input=frame,
                capture_output=True,
                text=True,
            )
            self.assertEqual(result.returncode, 0, result.stderr)
            self.assertIn("café ☕", result.stdout)
        for frame in [
            'event: answer.delta\ndata: {"payload":{"text":"unfinished"}}\n\n',
            'event: run.failed\ndata: {"payload":{"message":"fake-secret"}}\n\n',
            "event: run.completed\ndata: invalid\n\n",
        ]:
            result = subprocess.run(
                ["python3", str(ROOT / "lib/sse.py")],
                input=frame,
                capture_output=True,
                text=True,
            )
            self.assertNotEqual(result.returncode, 0)
            self.assertNotIn("fake-secret", result.stderr)

    def test_assertion_permissions_and_signature(self):
        """The printed public key verifies the protected, never-printed JWT."""
        with tempfile.TemporaryDirectory() as directory:
            result = subprocess.run(
                ["python3", str(ROOT / "lib/assertion.py"), directory, "demo-subject"],
                env=self.env,
                capture_output=True,
                text=True,
                check=True,
            )
            path = pathlib.Path(directory) / "assertion.jwt"
            self.assertEqual(path.stat().st_mode & 0o777, 0o600)
            assertion = path.read_text()
            self.assertNotIn(assertion, result.stdout)
            header, claims, signature = assertion.split(".")
            decoded = json.loads(
                base64.urlsafe_b64decode(claims + "=" * (-len(claims) % 4))
            )
            self.assertEqual(decoded["exp"] - decoded["iat"], 120)
            jwks = json.loads(result.stdout.split("Public JWKS: ", 1)[1])["keys"][0]
            self.assertNotIn("d", jwks)

            def length(value):
                size = len(value)
                if size < 128:
                    return bytes([size])
                count = (size.bit_length() + 7) // 8
                return bytes([0x80 | count]) + size.to_bytes(count, "big")

            def integer(value):
                raw = base64.urlsafe_b64decode(value + "=" * (-len(value) % 4))
                if raw[0] & 0x80:
                    raw = b"\0" + raw
                return b"\x02" + length(raw) + raw

            content = integer(jwks["n"]) + integer(jwks["e"])
            public = pathlib.Path(directory) / "public.der"
            public.write_bytes(b"\x30" + length(content) + content)
            message = pathlib.Path(directory) / "message"
            message.write_text(header + "." + claims)
            signature_path = pathlib.Path(directory) / "signature"
            signature_path.write_bytes(
                base64.urlsafe_b64decode(signature + "=" * (-len(signature) % 4))
            )
            checked = subprocess.run(
                [
                    "openssl",
                    "dgst",
                    "-sha256",
                    "-verify",
                    str(public),
                    "-keyform",
                    "DER",
                    "-signature",
                    str(signature_path),
                    str(message),
                ],
                capture_output=True,
                text=True,
            )
            self.assertEqual(checked.returncode, 0, checked.stderr)


if __name__ == "__main__":
    unittest.main()
