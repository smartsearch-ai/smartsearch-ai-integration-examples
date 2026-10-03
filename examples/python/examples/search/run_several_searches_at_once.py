"""Multi-search: several searches in one request, for example a results page plus "related"
rows, or one row per category on a landing page.

``multi_search(project_id, queries)`` sends the list in one round trip. The answer's
``result["responses"]`` is a list with one entry per query, in the same order; each entry has
the same ``hits.hits`` shape as a single search.

Limit the number of hits yourself. Multi-search does not apply each query's ``size``: every
entry comes back with more hits than you asked for. Take the first n of each list, as here.

Precondition: as ``first_search``. Run: ./run.sh run_several_searches_at_once
"""

import smartsearch_ai

from examples import _common

SHOW = 3


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    texts = args or ["alien", "titanic", "toy story"]
    queries = [
        smartsearch_ai.SearchQuery(q, response_fields=["title"], size=SHOW)
        for q in texts
    ]
    with _common.connect() as ss:
        # POST {api_url}/core/projects/{project_id}/mSearch   (body: a JSON
        # array of the queries)
        responses = _common.require_success(
            ss.search().multi_search(_common.project_id(), queries)
        ).result.get("responses", [])
        for text, response in zip(texts, responses):
            hits = response.get("hits", {}).get("hits", [])
            print(
                f'"{text}": received {len(hits)} hits, showing {min(SHOW, len(hits))}:'
            )
            for hit in hits[:SHOW]:
                print("  - " + str(hit.get("_source", {}).get("title")))


if __name__ == "__main__":
    _common.run(main)
