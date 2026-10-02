"""Sorting: order matching hits by a field (newest first, best rated first) instead of relevance.

``sort=[Sort(field, SortOrder.DESC)]`` adds one sort key; add more for tie-breakers. The field
must be sortable in your project (numbers, dates, keyword fields).

Turn reranking off when sorting. The reranker reorders hits by relevance and would override your
sort, so send ``rerank=False`` and keyword ranking, as here. A sort on a field that does not
exist is not reported as an error: check your field names.

Precondition: as ``first_search``. Run: ./run.sh sort_results love
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "love")
    with _common.connect() as ss:
        for field in ("release_date", "vote_average"):
            query = smartsearch_ai.SearchQuery(
                q,
                response_fields=["title", "release_date", "vote_average"],
                neural_mode=smartsearch_ai.NeuralMode.BM25,
                rerank=False,  # required: reranking would reorder by relevance
                sort=[
                    smartsearch_ai.Sort(field, smartsearch_ai.SortOrder.DESC)
                ],  # highest / newest first
                size=3,
            )
            print(f"Sorted by {field} descending:")
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query),
                "title",
                "release_date",
                "vote_average",
            )


if __name__ == "__main__":
    _common.run(main)
