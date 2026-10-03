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

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "wizard school")
    with _common.connect() as ss:
        for rerank in (True, False, None):
            query = smartsearch_ai.SearchQuery(
                q,
                rerank=rerank,  # True / False / None = project default
                response_fields=["title"],
                size=5,
            )
            print(
                f"rerank={'project default' if rerank is None else str(rerank).lower()}:"
            )
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query), "title"
            )


if __name__ == "__main__":
    _common.run(main)
