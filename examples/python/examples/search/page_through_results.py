"""Paging: show results one page at a time.

``from_=n`` skips the first n hits and ``size=m`` returns the next m. Page p (from zero) of size
m is ``from_=p * m, size=m``.

Turn reranking off when paging. *Reranking* re-scores the top hits with an AI model (see
``rerank_results``). It works on the first page of candidates and does not apply the ``from_``
offset, so with reranking on every "page" can show the same hits. For paged lists send
``rerank=False``, as here, and keyword ranking for a stable order.

Precondition: as ``first_search``. Run: ./run.sh page_through_results love
"""

from smartsearch_ai import NeuralMode, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run

PAGE_SIZE = 3


def main(args: list[str]) -> None:
    q = query_text(args, "love")
    with connect() as ss:
        for page in range(3):
            query = SearchQuery(
                q,
                response_fields=["title"],
                neural_mode=NeuralMode.BM25,   # keyword ranking: a stable order across pages
                rerank=False,                  # required for paging, see above
                from_=page * PAGE_SIZE,        # zero-based offset; must be >= 0
                size=PAGE_SIZE,                # hits per page; must be > 0
            )
            print(f"Page {page + 1} (from={page * PAGE_SIZE}):")
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")
            # A page with fewer than PAGE_SIZE hits is the last one.


if __name__ == "__main__":
    run(main)
