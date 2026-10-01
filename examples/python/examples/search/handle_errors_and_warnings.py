"""Errors and warnings: the three ways a search can go differently from what you asked.

1. Invalid request, caught before sending. ``SearchQuery`` checks every value and raises
   ``ValueError`` (precision outside 1..11, negative ``from_``, an empty query, a wildcard in
   response fields). Nothing reaches the server. Fix the code.
2. Refused by the server. ``SmartSearchError``: ``status_code`` is the HTTP status (0 when no
   response arrived: network or timeout), ``server_message`` and ``error_message`` explain.
   Subclasses narrow the cause (``NotFoundError`` for 404, ``RequestTimeoutError``,
   ``TransportError``). A network failure may succeed on retry; a 4xx, or a refusal whose
   message names a precondition (such as facets on a project with document security, see
   ``facet_counts``), needs a different request.
3. Answered, but adjusted. The search succeeds, and ``warning`` says what the server changed,
   for example running a different search technique because the project cannot run the one
   requested. ``effective_neural_mode`` shows what ran.

Exception messages never contain tokens, secrets or request bodies, so they are safe to log.

Precondition: as ``first_search``. Run: ./run.sh handle_errors_and_warnings
"""

from smartsearch_ai import NeuralMode, SearchQuery, SmartSearchError

from examples._common import connect, project_id, run


def main(args: list[str]) -> None:
    # 1. Caught by SearchQuery: no request is sent.
    try:
        SearchQuery("love", precision=12)
    except ValueError as e:
        print(f"1. rejected before sending: {e}")
    with connect() as ss:
        # 2. Refused by the server: this project ID does not exist.
        #    POST {api_url}/core/projects/no-such-project/search
        try:
            ss.search().search("no-such-project", SearchQuery("love"))
        except SmartSearchError as e:
            print(f"2. refused: HTTP {e.status_code}, message={e.server_message}")
        # 3. Answered with a warning: ask for a search technique the project may not support and
        #    check what actually ran.
        #    POST {api_url}/core/projects/{project_id}/search
        result = ss.search().search(project_id(), SearchQuery(
            "love", neural_mode=NeuralMode.EXACT_AND_BM25_FUSED, response_fields=["title"], size=1))
        print(f"3. requested {NeuralMode.EXACT_AND_BM25_FUSED.value}, ran {result.effective_neural_mode}")
        print(f"   warning: {result.warning if result.warning is not None else 'none'}")


if __name__ == "__main__":
    run(main)
