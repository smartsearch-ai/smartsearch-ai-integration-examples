"""Offline learning-path regressions; requires the public SDK and its dependencies.

Run from examples/python: python -m unittest discover -s tests -v
"""

import contextlib
import importlib
import io
import json
import os
import pathlib
import re
import types
import unittest
from unittest import mock

import httpx
import smartsearch_ai

from examples import _common


class ExampleTests(unittest.TestCase):
    """Exercise the actual SDK with local in-memory HTTP fixtures."""

    def test_all_examples_execute_offline(self):
        """All 44 teaching modules use the public wire contract without live credentials."""
        names = re.findall(
            r"\| \d+ \| `([^`]+)` \|", pathlib.Path("README.md").read_text()
        )
        self.assertEqual(len(names), 44)
        seen = []
        job_id = "00000000-0000-4000-8000-000000000003"

        def handle(request):
            seen.append(request)
            route = request.url.path
            body = (
                json.loads(request.content)
                if request.headers.get("content-type", "").startswith(
                    "application/json"
                )
                and request.content
                else {}
            )
            if route.endswith("/token"):
                return httpx.Response(
                    200,
                    json={
                        "access_token": "fake-safe-token",
                        "expires_in": 120,
                        "token_type": "Bearer",
                    },
                )
            if "/core/" in route:
                if "no-such-project" in route:
                    return httpx.Response(404, json={"message": "Not found"})
                hits = {"hits": [{"_source": {"title": "Example movie"}}]}
                return httpx.Response(
                    200,
                    json={
                        "code": 1,
                        "message": "Success",
                        "effective_neural_mode": "BM25",
                        "result": {
                            "hits": hits,
                            "responses": [{"hits": hits}] * 3,
                            "aggregations": {
                                "languages": {
                                    "buckets": [{"key": "en", "doc_count": 1}]
                                },
                                "genres": {"buckets": []},
                            },
                        },
                    },
                )
            if body.get("options", {}).get("stream"):
                payload = {
                    "status": "COMPLETED",
                    "answer": "Final fixture answer",
                    "sources": [],
                    "session_id": "demo-session",
                    "memory": {},
                }
                return httpx.Response(
                    200,
                    headers={"Content-Type": "text/event-stream"},
                    content="event: run.completed\ndata: "
                    + json.dumps({"payload": payload})
                    + "\n\n",
                )
            if route.endswith("/jobs"):
                return httpx.Response(
                    202, json={"job_id": job_id, "state": "QUEUED"}
                )
            if route.endswith("/" + job_id):
                return httpx.Response(200, json={"state": "SUCCEEDED"})
            return httpx.Response(
                200,
                json={
                    "documents": [],
                    "sources": [],
                    "values": [],
                    "answer": "Fixture answer",
                    "session_id": "demo-session",
                    "mode": "answer",
                    "status": "COMPLETED",
                    "memory": {},
                },
            )

        original_client = httpx.Client

        def client_factory(*args, **kwargs):
            kwargs["transport"] = httpx.MockTransport(handle)
            kwargs["trust_env"] = False
            return original_client(*args, **kwargs)

        env = {
            "SMARTSEARCH_API_BASE_URL": "https://api.example.com",
            "SMARTSEARCH_ADMIN_BASE_URL": "https://admin.example.com",
            "SMARTSEARCH_AUTH_BASE_URL": "https://auth.example.com",
            "SMARTSEARCH_REALM": "example",
            "SMARTSEARCH_CLIENT_ID": "svc-example",
            "SMARTSEARCH_CLIENT_SECRET": "fake-secret",
            "SMARTSEARCH_PROJECT_ID": "demo-project",
            "SMARTSEARCH_USECASE_ID": "demo-usecase",
            "SMARTSEARCH_WORKSPACE_ID": "00000000-0000-4000-8000-000000000001",
            "SMARTSEARCH_SOURCE_ID": "00000000-0000-4000-8000-000000000002",
            "SMARTSEARCH_INTEGRATION_ID": "demo-integration",
            "SMARTSEARCH_TENANT_ID": "demo-tenant",
            "SMARTSEARCH_USER_ACCESS_TOKEN": "fake-user-token",
            "SMARTSEARCH_USER_ASSERTION": "fake-assertion",
        }
        output = io.StringIO()
        with mock.patch.dict(os.environ, env, clear=True), mock.patch(
            "httpx.Client", client_factory
        ), contextlib.redirect_stdout(output):
            for name in names:
                topic = next(
                    p.parent.name
                    for p in pathlib.Path("examples").glob("*/*.py")
                    if p.stem == name
                )
                with self.subTest(example=name):
                    importlib.import_module(f"examples.{topic}.{name}").main([])
        self.assertGreater(len(seen), 80)
        self.assertIn("Final fixture answer", output.getvalue())

    def test_application_failure_is_not_empty_results(self):
        """HTTP success with code zero must fail before displaying an empty list."""
        result = smartsearch_ai.SearchResult(
            {"code": 0, "result": {"hits": {"hits": []}}}
        )
        with self.assertRaisesRegex(ValueError, "application failure"):
            _common.hits_of(result)
        with self.assertRaisesRegex(ValueError, "application failure"):
            _common.print_hits(result)

    def test_terminal_job_and_timeout(self):
        """Only terminal states are presented as finished."""
        for state in _common.FINISHED:
            users = types.SimpleNamespace(
                get_job=lambda _: types.SimpleNamespace(body={"state": state})
            )
            self.assertEqual(
                _common.wait_for_job(users, "demo", 0)["state"], state
            )
        users = types.SimpleNamespace(
            get_job=lambda _: types.SimpleNamespace(body={"state": "RUNNING"})
        )
        with self.assertRaises(TimeoutError):
            _common.wait_for_job(users, "demo", 0)


if __name__ == "__main__":
    unittest.main()
