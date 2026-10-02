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

import smartsearch_ai


class MissingSetting(Exception):
    """A required environment variable is not set."""


def require(name: str) -> str:
    """Reads one required setting.

    Args:
        name: Environment variable name from .env.example.

    Returns:
        The trimmed setting value.

    Raises:
        MissingSetting: The setting is absent or blank.
    """
    value = os.environ.get(name, "").strip()
    if not value:
        raise MissingSetting(
            f"Environment variable {name} is not set (see .env.example)"
        )
    return value


def optional(name: str, fallback: str | None) -> str | None:
    """Reads a setting or its default.

    Args:
        name: Environment variable name.
        fallback: Value used when the setting is absent or blank.

    Returns:
        The trimmed value or fallback.
    """
    return os.environ.get(name, "").strip() or fallback


def connect() -> smartsearch_ai.SmartSearchAi:
    """Builds the SDK client (:class:`SmartSearchAi`) that authenticates as your service key.

    Building it makes no network call. On the first request the SDK exchanges the key for a
    short-lived access token (OAuth 2.0 client credentials,
    ``POST {auth_url}/realms/{realm}/protocol/openid-connect/token``) and renews it automatically
    before it expires.

    The client is thread-safe: create one per application and share it. Close it on shutdown (the
    examples use ``with``). Misconfigured values (blank, not an http(s) URL) fail here with a
    ``ValueError`` naming the setting.
    """
    return smartsearch_ai.SmartSearchAi(
        api_url=require(
            "SMARTSEARCH_API_BASE_URL"
        ),  # API gateway: project search + Workplace
        admin_url=require(
            "SMARTSEARCH_ADMIN_BASE_URL"
        ),  # Search Admin: user provisioning
        auth_url=require(
            "SMARTSEARCH_AUTH_BASE_URL"
        ),  # identity server base URL, without /realms/...
        realm=require("SMARTSEARCH_REALM"),  # your realm name
        client_id=require("SMARTSEARCH_CLIENT_ID"),
        client_secret=require("SMARTSEARCH_CLIENT_SECRET"),
    )


def project_id() -> str:
    """Reads the assigned project ID used by project-search examples.

    Returns:
        Project ID.

    Raises:
        MissingSetting: SMARTSEARCH_PROJECT_ID is missing.
    """
    return require("SMARTSEARCH_PROJECT_ID")


def workspace_id() -> str:
    """Reads the workspace ID whose membership permits Workplace access.

    Returns:
        Workspace ID.

    Raises:
        MissingSetting: SMARTSEARCH_WORKSPACE_ID is missing.
    """
    return require("SMARTSEARCH_WORKSPACE_ID")


def integration_id() -> str:
    """Reads the saved Register users integration ID.

    Returns:
        Integration ID.

    Raises:
        MissingSetting: SMARTSEARCH_INTEGRATION_ID is missing.
    """
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
    except smartsearch_ai.SmartSearchError as e:
        if e.code == "TOKEN_ACQUISITION_FAILED":
            # The identity server refused to issue a token (wrong key, disabled
            # grant, expired user token).
            _fail("Could not obtain a token: " + e.message)
        elif e.message == "Provisioning request failed":
            # Provisioning: retryable says whether the same request may succeed
            # later.
            _fail(
                f"Provisioning request refused: HTTP {e.status_code} {e.code}"
                + (" (retryable)" if e.retryable else "")
            )
        elif e.message.startswith("Search request"):
            # Project search: the message carries the status and the server's
            # explanation.
            _fail(e.message)
        else:
            # Workplace: status_code is the HTTP status, code the server's error
            # code.
            _fail(
                f"Workplace request refused: HTTP {e.status_code} {e.code} - {e.message}"
            )
    except (ValueError, MissingSetting) as e:
        # Invalid input caught by the SDK before sending, or a missing setting.
        _fail(str(e))
    except (
        Exception
    ) as e:  # noqa: BLE001 - examples print every failure as one line
        _fail(f"{type(e).__name__}: {e}")


def _fail(message: str) -> None:
    print("ERROR " + message, file=sys.stderr)
    sys.exit(1)


def shorten(text: Any, limit: int) -> str:
    """Collapses whitespace and truncates text for one-line output.

    Args:
        text: Value to display.
        limit: Maximum visible text length before the ellipsis.

    Returns:
        Shortened display text.
    """
    s = re.sub(r"\s+", " ", "" if text is None else str(text)).strip()
    return s[:limit] + "..." if len(s) > limit else s


def query_text(args: list[str], fallback: str) -> str:
    """Joins CLI arguments into query text, preserving quoted argument contents.

    Args:
        args: Query word arguments.
        fallback: Query used when no words are supplied.

    Returns:
        Query text.
    """
    return " ".join(args) if args and args[0].strip() else fallback


# --- project search
# ----------------------------------------------------------------------------
#
# Shape of SearchResult.result for a search:
#   {
#     "hits": {
# "hits": [                               one entry per returned document, best
# first
# { "_source": { "title": "...", ... },  the fields you asked for with
# response_fields
# "_score": 12.3,                      relevance score (absent when the order is
# not by score)
#           "highlight": { ... } },              only when highlight=True
#         ...
#       ]
#     },
#     "aggregations": { ... }                   only when you asked for facets
#   }
# Count what you received with len(hits). The response does not give a reliable
# total number of
# matches, so do not build "N results" labels from it.


def require_success(
    result: smartsearch_ai.SearchResult,
) -> smartsearch_ai.SearchResult:
    """Requires application success independently of the HTTP status.

    Args:
        result: Core response envelope.

    Returns:
        The same envelope when its code equals one.

    Raises:
        ValueError: Core reported an application failure.
    """
    if result.code != 1:
        raise ValueError(
            f"Core search reported application failure (code={result.code})"
        )
    return result


def hits_of(result: smartsearch_ai.SearchResult) -> list[dict[str, Any]]:
    """Reads hits only after Core application success is confirmed.

    Args:
        result: Core response envelope.

    Returns:
        Received hits; this is not the total match count.

    Raises:
        ValueError: Core reported an application failure.
    """
    return require_success(result).result.get("hits", {}).get("hits", [])


def print_hits(result: smartsearch_ai.SearchResult, *fields: str) -> None:
    """Prints received hits, actual retrieval mode and warning.

    Args:
        result: Core response envelope.
        fields: Business fields to display from each hit.

    Raises:
        ValueError: Core reported an application failure.
    """
    hits = hits_of(result)
    warning = (
        f' warning="{shorten(result.warning, 160)}"'
        if result.warning is not None
        else ""
    )
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
    """Formats a field value, including lists of named objects.

    Args:
        value: Response field value.

    Returns:
        Display text.
    """
    if isinstance(value, list):
        return ", ".join(
            str(v["name"]) if isinstance(v, dict) and "name" in v else str(v)
            for v in value
        )
    return "" if value is None else str(value)


# --- Workplace ----------------------------------------------------------------
# -----------------
#
# A search answers { "documents": [ { "title": ..., "snippet": ..., ... }, ... ]
# }. A generated
# answer adds "answer" (the text) and "sources" (the documents it cites, same
# shape). The numbers
# in brackets in an answer, such as [1], refer to those sources.


def print_documents(body: dict[str, Any]) -> None:
    """Prints search documents; retrieval-only query evidence lives in sources.

    Args:
        body: Direct Workplace search response.
    """
    docs = body.get("documents", [])
    print(f"documents={len(docs)}")
    for rank, doc in enumerate(docs, 1):
        print(f"{rank:2d}. {shorten(doc.get('title'), 90)}")


def print_answer(body: dict[str, Any]) -> None:
    """Prints the final answer and source titles.

    Args:
        body: Direct Workplace response or completed stream payload.
    """
    print("answer: " + shorten(body.get("answer"), 300))
    sources = body.get("sources", [])
    print(f"sources={len(sources)}")
    for source in sources:
        print("  - " + shorten(source.get("title"), 90))


# --- user provisioning
# -------------------------------------------------------------------------

# States after which a provisioning job no longer changes.
FINISHED = {
    "SUCCEEDED",
    "PARTIAL",
    "FAILED",
    "RECONCILIATION_REQUIRED",
    "CANCELLED",
}


def tenant_scope() -> smartsearch_ai.Scope:
    """The tenant your integration registers users into. A *tenant* is your organisation's account
    in SmartSearch AI; your administrator gives you its ID (SMARTSEARCH_TENANT_ID).
    """
    return smartsearch_ai.Scope(
        kind=smartsearch_ai.ScopeKind.TENANT,
        id=require("SMARTSEARCH_TENANT_ID"),
    )


def wait_for_job(
    users: smartsearch_ai.Users, job_id: str, timeout_seconds: float
) -> dict[str, Any]:
    """Polls until a terminal registration state; a timeout is not completion.

    Args:
        users: Public provisioning client.
        job_id: Submitted job ID.
        timeout_seconds: Polling window in seconds.

    Returns:
        Terminal job response; inspect per-item failures separately.

    Raises:
        TimeoutError: The job remains nonterminal at the deadline.
    """
    deadline = time.monotonic() + timeout_seconds
    job = users.get_job(job_id).body
    while job.get("state") not in FINISHED and time.monotonic() < deadline:
        time.sleep(1)
        job = users.get_job(job_id).body
    if job.get("state") not in FINISHED:
        raise TimeoutError(
            f"Provisioning job {job_id} still {job.get('state')} after {timeout_seconds}s; poll this job again"
        )
    return job


def print_items(users: smartsearch_ai.Users, job_id: str) -> None:
    """Prints the first 100 registration item outcomes.

    Args:
        users: Public provisioning client.
        job_id: Job ID returned by submission.
    """
    page = users.list_job_items(job_id, limit=100).body
    for item in page.get("values", []):
        print(
            f"  item {item.get('item_key')}: {item.get('state')}"
            + (
                f" principal={item['principal_id']}"
                if item.get("principal_id") is not None
                else ""
            )
            + (
                f" error={item['error_code']}"
                if item.get("error_code") is not None
                else ""
            )
        )
