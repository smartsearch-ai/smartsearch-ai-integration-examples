"""Control semantic matching: how many close-in-meaning documents to consider and return.

Semantic search looks at a set of candidate documents near the query in meaning and keeps the
closest. ``neural_matches=(top, total)`` sets both numbers: consider ``total`` candidates, keep
the ``top`` best (1 <= top <= total <= 10000). A larger total finds better matches at some cost
in speed.

``top`` caps what semantic search returns even when ``size`` is larger: here ``size=10`` with
``top = 3`` gives at most 3 hits.

Precondition: as ``keyword_vs_semantic_vs_hybrid``.
Run: ./run.sh limit_semantic_matches "a toy cowboy afraid of being replaced"
"""

from smartsearch_ai import NeuralMode, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "a toy cowboy afraid of being replaced")
    with connect() as ss:
        for top in (3, 10):
            query = SearchQuery(
                q,
                neural_mode=NeuralMode.A_KNN,       # semantic only
                neural_matches=(top, 100),          # keep the best `top` of 100 candidates
                response_fields=["title"],
                size=10,
            )
            print(f"neuralMatches(top={top}, total=100), size=10:")
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")


if __name__ == "__main__":
    run(main)
