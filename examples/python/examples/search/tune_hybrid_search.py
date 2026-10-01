"""Tune hybrid search: how the keyword list and the semantic list are fused.

Hybrid search produces two ranked lists and merges them. There are two ways to merge, chosen
with ``normalization`` and ``combination`` (give both):

- By rank: ``RANK`` + ``RRF``, reciprocal rank fusion. Each document scores 1 / (k + its
  position) in each list, and the scores add up. Robust, needs no score tuning.
  ``rank_constant=k`` (at least 1) sets k: a larger k flattens the difference between top and
  lower positions.
- By score: a normalization (``MIN_MAX``, ``L2``, ``Z_SCORE``, ``DISTRIBUTION_BASED``) that
  brings both lists' scores to a common scale, and a mean (``ARITHMETIC_MEAN``,
  ``GEOMETRIC_MEAN``, ``HARMONIC_MEAN``) that combines them.

RRF works only with RANK, and RANK only with RRF; ``SearchQuery`` rejects other pairings with a
``ValueError`` before sending.

``rank_window=n`` (at least 10) is how many candidates each list contributes before fusion:
larger finds more, costs more.

Precondition: as ``keyword_vs_semantic_vs_hybrid``. Run: ./run.sh tune_hybrid_search
"""

from smartsearch_ai import Combination, NeuralMode, Normalization, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "rebels fight an evil empire in space")
    with connect() as ss:
        by_rank = SearchQuery(
            q,
            neural_mode=NeuralMode.A_KNN_AND_BM25,
            normalization=Normalization.RANK, combination=Combination.RRF,   # fuse by position
            rank_window=50,                                                  # 50 candidates per list
            rank_constant=60,                                                # RRF constant k
            response_fields=["title"], size=5,
        )
        print("Fuse by rank (RANK + RRF):")
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), by_rank), "title")

        by_score = SearchQuery(
            q,
            neural_mode=NeuralMode.A_KNN_AND_BM25,
            normalization=Normalization.MIN_MAX, combination=Combination.ARITHMETIC_MEAN,   # fuse by score
            response_fields=["title"], size=5,
        )
        print("Fuse by score (MIN_MAX + ARITHMETIC_MEAN):")
        print_hits(ss.search().search(project_id(), by_score), "title")


if __name__ == "__main__":
    run(main)
