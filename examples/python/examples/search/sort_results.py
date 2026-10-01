"""Sorting: order matching hits by a field (newest first, best rated first) instead of relevance.

``sort=[Sort(field, SortOrder.DESC)]`` adds one sort key; add more for tie-breakers. The field
must be sortable in your project (numbers, dates, keyword fields).

Turn reranking off when sorting. The reranker reorders hits by relevance and would override your
sort, so send ``rerank=False`` and keyword ranking, as here. A sort on a field that does not
exist is not reported as an error: check your field names.

Precondition: as ``first_search``. Run: ./run.sh sort_results love
"""

from smartsearch_ai import NeuralMode, SearchQuery, Sort, SortOrder

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "love")
    with connect() as ss:
        for field in ("release_date", "vote_average"):
            query = SearchQuery(
                q,
                response_fields=["title", "release_date", "vote_average"],
                neural_mode=NeuralMode.BM25,
                rerank=False,                          # required: reranking would reorder by relevance
                sort=[Sort(field, SortOrder.DESC)],    # highest / newest first
                size=3,
            )
            print(f"Sorted by {field} descending:")
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title", "release_date", "vote_average")


if __name__ == "__main__":
    run(main)
