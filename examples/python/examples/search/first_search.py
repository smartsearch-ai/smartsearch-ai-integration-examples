"""Your first project search, and how to read what comes back.

Concepts. A *project* is one searchable collection of your documents (a product catalogue, a
knowledge base) with its own relevance settings. You search it with *SSPL*, the SmartSearch
search request language: a small JSON request with the query text, the fields to search and
return, filters, sorting, paging and search options. You never write the JSON by hand:
``SearchQuery`` writes it for you and rejects invalid values (``ValueError``) before anything is
sent. The server then picks the search technique (keyword, semantic or both, see
``keyword_vs_semantic_vs_hybrid``) from the project's settings and your options.

Preconditions. SMARTSEARCH_PROJECT_ID is a project your service key is assigned to. The search
examples use the Movies sample data set (fields ``title``, ``overview``, ``tagline``,
``genres``, ``release_date``, ``vote_average``, ``original_language``, ``runtime``,
``status``); change the field names for yours.

Run: ./run.sh first_search "star wars"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    with _common.connect() as ss:
        # Build the request. q is what the user typed; response_fields names the
        # fields each hit
        # should carry (name each one: wildcards are rejected); size is how many
        # hits to return
        # (default 10).
        query = smartsearch_ai.SearchQuery(
            _common.query_text(args, "star wars"),
            response_fields=["title", "release_date", "vote_average"],
            size=5,
        )
        # to_json() shows the exact body the SDK will send. Useful while
        # learning and in logs.
        print("request: " + query.to_json())

        # POST {api_url}/core/projects/{project_id}/search
        # Authenticates with the service key's token. Raises SmartSearchError
        # when the server
        # refuses the request (for example HTTP 404 for an unknown project ID)
        # or when no response
        # arrives (status_code == 0). See handle_errors_and_warnings.
        result = ss.search().search(_common.project_id(), query)

        _common.require_success(result)
        # Envelope fields on every response:
        #   code                   1 = success
        #   message                short status text, "Success" on success
        # search_id              identifies this search; quote it when reporting
        # a problem
        #   effective_neural_mode  the search technique the server actually ran
        # warning                set when the server changed something you asked
        # for
        print(
            f"code={result.code} message={result.message}"
            f" searchId={'present' if result.search_id is not None else 'none'}"
            f" mode={result.effective_neural_mode} warning={result.warning}"
        )

        # result.result holds the hits (see _common.py for the shape). Each
        # hit's _source carries
        # only the response_fields you asked for.
        for hit in _common.hits_of(result):
            doc = hit.get("_source", {})
            print(
                f"  {doc.get('title')} ({doc.get('release_date')}, rated {doc.get('vote_average')})"
            )


if __name__ == "__main__":
    _common.run(main)
