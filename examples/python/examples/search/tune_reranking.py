"""Tune reranking: how many candidates the model re-scores, and a minimum score to keep a hit.

``rerank_first_stage_size=n`` (greater than 0) is how many first-pass candidates the reranker
reads. More candidates give the model more chances to find the best answers, and take longer.
``rerank_min_score=s`` drops hits the model scores below ``s``, which trims weak answers from
the end of the list. Whether any hit falls below a given value depends on the model and your
data, so pick the threshold by testing on real queries.

Precondition: as ``rerank_results``. Run: ./run.sh tune_reranking "wizard school"
"""

from smartsearch_ai import SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "wizard school")
    with connect() as ss:
        for first_stage in (10, 50):
            query = SearchQuery(
                q,
                rerank=True,
                rerank_first_stage_size=first_stage,    # candidates the model re-scores
                response_fields=["title"],
                size=5,
            )
            print(f"rerankFirstStageSize={first_stage}:")
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")

        with_minimum = SearchQuery(
            q,
            rerank=True,
            rerank_first_stage_size=30,
            rerank_min_score=0.5,                       # drop hits the model scores below 0.5
            response_fields=["title"],
            size=10,
        )
        print("rerankMinScore=0.5, size=10:")
        print_hits(ss.search().search(project_id(), with_minimum), "title")


if __name__ == "__main__":
    run(main)
