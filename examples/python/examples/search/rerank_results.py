"""Reranking on, off, and the project default.

*Reranking* is a second pass: the search first finds candidates quickly, then an AI model (a
cross-encoder, which reads the query and each document together) re-scores the top candidates
and reorders them by how well they answer the query. It usually improves the first results, at
the cost of some time.

``rerank=True`` forces it on, ``rerank=False`` forces it off, and ``rerank=None`` (the default)
uses the project's setting.

Turn reranking off for sorted lists and paged lists: it reorders by relevance, which overrides a
sort, and it does not apply the ``from_`` offset (see ``sort_results`` and
``page_through_results``).

Precondition: as ``first_search``, on a project with a reranker set up.
Run: ./run.sh rerank_results "wizard school"
"""

from smartsearch_ai import SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "wizard school")
    with connect() as ss:
        for rerank in (True, False, None):
            query = SearchQuery(
                q,
                rerank=rerank,                  # True / False / None = project default
                response_fields=["title"],
                size=5,
            )
            print(f"rerank={'project default' if rerank is None else str(rerank).lower()}:")
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")


if __name__ == "__main__":
    run(main)
