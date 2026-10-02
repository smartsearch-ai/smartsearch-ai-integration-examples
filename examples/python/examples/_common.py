"""Shared plumbing for the examples: settings from environment variables, the SDK client, one
readable ERROR line on failure, and printers for search and Workplace results.

Example plumbing, not part of the SDK. Copy or replace it in your own code.

All settings come from your SmartSearch AI administrator (names match ``.env.example`` at the
repository root)::

    SMARTSEARCH_API_BASE_URL    https://api.your-company.example.com
                                The SmartSearch API gateway. Project search and Workplace calls go here.
    SMARTSEARCH_ADMIN_BASE_URL  https://admin.your-company.example.com
                                Search Admin. User provisioning (registering users) goes here.
    SMARTSEARCH_AUTH_BASE_URL   https://auth.your-company.example.com
                                The identity server's base URL: the part BEFORE /realms/...
    SMARTSEARCH_REALM           your-realm
                                The name of your login realm on that identity server.
                                Together they give the token endpoint the SDK calls:
                                https://auth.your-company.example.com/realms/your-realm/protocol/openid-connect/token
    SMARTSEARCH_CLIENT_ID       svc-your-key-id        Service key ID (starts with svc-)
    SMARTSEARCH_CLIENT_SECRET   (secret)               Service key secret. Never commit it.
"""

from __future__ import annotations

import os
import re
import sys
import time
from typing import Any, Callable

from smartsearch_ai import Scope, ScopeKind, SearchResult, SmartSearchAi, SmartSearchError, Users


class MissingSetting(Exception):
    """A required environment variable is not set."""


def require(name: str) -> str:
    """A required variable; stops the example with a clear message when it is missing."""
    value = os.environ.get(name, "").strip()
    if not value:
        raise MissingSetting(f"Environment variable {name} is not set (see .env.example)")
    return value


def optional(name: str, fallback: str | None) -> str | None:
    """An optional variable, or ``fallback`` when unset."""
    return os.environ.get(name, "").strip() or fallback


def connect() -> SmartSearchAi:
    """Builds the SDK client (:class:`SmartSearchAi`) that authenticates as your service key.

    Building it makes no network call. On the first request the SDK exchanges the key for a
    short-lived access token (OAuth 2.0 client credentials,
    ``POST {auth_url}/realms/{realm}/protocol/openid-connect/token``) and renews it automatically
    before it expires.

    The client is thread-safe: create one per application and share it. Close it on shutdown (the
    examples use ``with``). Misconfigured values (blank, not an http(s) URL) fail here with a
    ``ValueError`` naming the setting.
    """
    return SmartSearchAi(
        api_url=require("SMARTSEARCH_API_BASE_URL"),      # API gateway: project search + Workplace
        admin_url=require("SMARTSEARCH_ADMIN_BASE_URL"),  # Search Admin: user provisioning
        auth_url=require("SMARTSEARCH_AUTH_BASE_URL"),    # identity server base URL, without /realms/...
        realm=require("SMARTSEARCH_REALM"),               # your realm name
        client_id=require("SMARTSEARCH_CLIENT_ID"),
        client_secret=require("SMARTSEARCH_CLIENT_SECRET"),
    )


def project_id() -> str:
    """The project to search (SMARTSEARCH_PROJECT_ID). Your service key must be assigned to it."""
    return require("SMARTSEARCH_PROJECT_ID")


def workspace_id() -> str:
    """The Workplace workspace to search (SMARTSEARCH_WORKSPACE_ID). Your service key must be a member."""
    return require("SMARTSEARCH_WORKSPACE_ID")


def integration_id() -> str:
    """The provisioning integration your service key registers users through (SMARTSEARCH_INTEGRATION_ID)."""
    return require("SMARTSEARCH_INTEGRATION_ID")


def run(main: Callable[[list[str]], None]) -> None:
    """Runs an example with its command-line arguments. On failure prints one ``ERROR`` line and
    exits with status 1.

    Every SDK failure is a :class:`SmartSearchError` with the HTTP status (``status_code``, 0 when
    no response arrived) and the server's error code (``code``). It never contains tokens,
    secrets or request bodies, so it is safe to log.
    """
    try:
        main(sys.argv[1:])
    except SmartSearchError as e:
        if e.code == "TOKEN_ACQUISITION_FAILED":
            # The identity server refused to issue a token (wrong key, disabled grant, expired user token).
            _fail("Could not obtain a token: " + e.message)
        elif e.message == "Provisioning request failed":
            # Provisioning: retryable says whether the same request may succeed later.
            _fail(f"Provisioning request refused: HTTP {e.status_code} {e.code}" + (" (retryable)" if e.retryable else ""))
        elif e.message.startswith("Search request"):
            # Project search: the message carries the status and the server's explanation.
            _fail(e.message)
        else:
            # Workplace: status_code is the HTTP status, code the server's error code.
            _fail(f"Workplace request refused: HTTP {e.status_code} {e.code} - {e.message}")
    except (ValueError, MissingSetting) as e:
        # Invalid input caught by the SDK before sending, or a missing setting.
        _fail(str(e))
    except Exception as e:  # noqa: BLE001 - examples print every failure as one line
        _fail(f"{type(e).__name__}: {e}")


def _fail(message: str) -> None:
    print("ERROR " + message, file=sys.stderr)
    sys.exit(1)


def shorten(text: Any, limit: int) -> str:
    """Collapses whitespace and shortens text to at most ``limit`` characters, for one-line output."""
    s = re.sub(r"\s+", " ", "" if text is None else str(text)).strip()
    return s[:limit] + "..." if len(s) > limit else s


def query_text(args: list[str], fallback: str) -> str:
    """Query text for an example: its command-line arguments joined, or ``fallback`` when there are none."""
    return " ".join(args) if args and args[0].strip() else fallback


# --- project search ----------------------------------------------------------------------------
#
# Shape of SearchResult.result for a search:
#   {
#     "hits": {
#       "hits": [                               one entry per returned document, best first
#         { "_source": { "title": "...", ... },  the fields you asked for with response_fields
#           "_score": 12.3,                      relevance score (absent when the order is not by score)
#           "highlight": { ... } },              only when highlight=True
#         ...
#       ]
#     },
#     "aggregations": { ... }                   only when you asked for facets
#   }
# Count what you received with len(hits). The response does not give a reliable total number of
# matches, so do not build "N results" labels from it.

def hits_of(result: SearchResult) -> list[dict[str, Any]]:
    """The hits of a search result, best first."""
    return result.result.get("hits", {}).get("hits", [])


def print_hits(result: SearchResult, *fields: str) -> None:
    """One line per hit: the first field bare, the others as ``name=value``."""
    hits = hits_of(result)
    warning = f' warning="{shorten(result.warning, 160)}"' if result.warning is not None else ""
    print(f"hits={len(hits)} mode={result.effective_neural_mode}{warning}")
    for rank, hit in enumerate(hits, 1):
        source = hit.get("_source", {})
        line = f"{rank:2d}. "
        for i, field in enumerate(fields):
            if field not in source:
                continue
            if i > 0:
                line += f" | {field}="
            line += shorten(value_as_text(source[field]), 60)
        print(line)


def value_as_text(value: Any) -> str:
    """A field value as text. Lists of {name: ...} objects (such as genres) print as "A, B"."""
    if isinstance(value, list):
        return ", ".join(str(v["name"]) if isinstance(v, dict) and "name" in v else str(v) for v in value)
    return "" if value is None else str(value)


# --- Workplace ---------------------------------------------------------------------------------
#
# A search answers { "documents": [ { "title": ..., "snippet": ..., ... }, ... ] }. A generated
# answer adds "answer" (the text) and "sources" (the documents it cites, same shape). The numbers
# in brackets in an answer, such as [1], refer to those sources.

def print_documents(body: dict[str, Any]) -> None:
    """Prints the documents of a search or retrieval-only query."""
    docs = body.get("documents", [])
    print(f"documents={len(docs)}")
    for rank, doc in enumerate(docs, 1):
        print(f"{rank:2d}. {shorten(doc.get('title'), 90)}")


def print_answer(body: dict[str, Any]) -> None:
    """Prints an answer and the sources it cites."""
    print("answer: " + shorten(body.get("answer"), 300))
    sources = body.get("sources", [])
    print(f"sources={len(sources)}")
    for source in sources:
        print("  - " + shorten(source.get("title"), 90))


# --- user provisioning -------------------------------------------------------------------------

# States after which a provisioning job no longer changes.
FINISHED = {"SUCCEEDED", "PARTIAL", "FAILED", "RECONCILIATION_REQUIRED", "CANCELLED"}


def tenant_scope() -> Scope:
    """The tenant your integration registers users into. A *tenant* is your organisation's account
    in SmartSearch AI; your administrator gives you its ID (SMARTSEARCH_TENANT_ID)."""
    return Scope(kind=ScopeKind.TENANT, id=require("SMARTSEARCH_TENANT_ID"))


def wait_for_job(users: Users, job_id: str, timeout_seconds: float) -> dict[str, Any]:
    """Polls a job once a second until it finishes; raises TimeoutError if the timeout passes.
    GET {admin_url}/search-admin/api/provisioning/v1/jobs/{job_id}"""
    deadline = time.monotonic() + timeout_seconds
    job = users.get_job(job_id).body
    while job.get("state") not in FINISHED and time.monotonic() < deadline:
        time.sleep(1)
        job = users.get_job(job_id).body
    if job.get("state") not in FINISHED:
        raise TimeoutError(f"Provisioning job {job_id} still {job.get('state')} after {timeout_seconds}s; poll this job again")
    return job


def print_items(users: Users, job_id: str) -> None:
    """Prints one line per user in the job: your item key, its state, and the principal it became.
    GET {admin_url}/search-admin/api/provisioning/v1/jobs/{job_id}/items?limit=100"""
    page = users.list_job_items(job_id, limit=100).body
    for item in page.get("values", []):
        print(f"  item {item.get('item_key')}: {item.get('state')}"
              + (f" principal={item['principal_id']}" if item.get("principal_id") is not None else "")
              + (f" error={item['error_code']}" if item.get("error_code") is not None else ""))
