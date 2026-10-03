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

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "a toy cowboy afraid of being replaced")
    with _common.connect() as ss:
        for top in (3, 10):
            query = smartsearch_ai.SearchQuery(
                q,
                neural_mode=smartsearch_ai.NeuralMode.A_KNN,  # semantic only
                neural_matches=(
                    top,
                    100,
                ),  # keep the best `top` of 100 candidates
                response_fields=["title"],
                size=10,
            )
            print(f"neuralMatches(top={top}, total=100), size=10:")
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query), "title"
            )


if __name__ == "__main__":
    _common.run(main)
