"""Boost: rank documents with a chosen value higher, without removing the others.

A filter removes documents; a boost only moves matching ones up. Typical uses are business rules
such as "prefer in-stock items" or "prefer the user's language". ``TermBoost(field, value,
weight)`` adds ``weight`` worth of score to documents whose field equals the value; a larger
weight pushes them further up. You can add several boosts.

Boosts shape keyword scores. They act on keyword (BM25) ranking. The reranker re-scores the top
hits by relevance alone and can undo a boost, so when a business rule must decide the order use
keyword ranking and ``rerank=False``, as here.

Precondition: as ``first_search``. Run: ./run.sh boost_term_values love
"""

from smartsearch_ai import NeuralMode, SearchQuery, TermBoost

from examples._common import connect, print_hits, project_id, query_text, run

FIELDS = ["title", "original_language"]


def main(args: list[str]) -> None:
    q = query_text(args, "love")
    with connect() as ss:
        # A SearchQuery is immutable, so keep the shared options in a dict and build a new
        # query for each variant.
        base = dict(response_fields=FIELDS, size=5, neural_mode=NeuralMode.BM25,
                    rerank=False)                                          # keep the boosted order
        print("Without a boost:")
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), SearchQuery(q, **base)), *FIELDS)
        print("Boost original_language = fr, weight 5:")
        print_hits(ss.search().search(project_id(), SearchQuery(
            q, **base, boosts=[TermBoost("original_language", "fr", 5)])), *FIELDS)


if __name__ == "__main__":
    run(main)
