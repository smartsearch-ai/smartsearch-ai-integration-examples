"""Multi-search: several searches in one request, for example a results page plus "related"
rows, or one row per category on a landing page.

``multi_search(project_id, queries)`` sends the list in one round trip. The answer's
``result["responses"]`` is a list with one entry per query, in the same order; each entry has
the same ``hits.hits`` shape as a single search.

Limit the number of hits yourself. Multi-search does not apply each query's ``size``: every
entry comes back with more hits than you asked for. Take the first n of each list, as here.

Precondition: as ``first_search``. Run: ./run.sh run_several_searches_at_once
"""

from smartsearch_ai import SearchQuery

from examples._common import connect, project_id, run

SHOW = 3


def main(args: list[str]) -> None:
    texts = args or ["alien", "titanic", "toy story"]
    queries = [SearchQuery(q, response_fields=["title"], size=SHOW) for q in texts]
    with connect() as ss:
        # POST {api_url}/core/projects/{project_id}/mSearch   (body: a JSON array of the queries)
        responses = ss.search().multi_search(project_id(), queries).result.get("responses", [])
        for text, response in zip(texts, responses):
            hits = response.get("hits", {}).get("hits", [])
            print(f'"{text}": received {len(hits)} hits, showing {min(SHOW, len(hits))}:')
            for hit in hits[:SHOW]:
                print("  - " + str(hit.get("_source", {}).get("title")))


if __name__ == "__main__":
    run(main)
