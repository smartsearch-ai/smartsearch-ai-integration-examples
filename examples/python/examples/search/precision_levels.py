"""Precision: how strictly the query words must match.

``precision=level`` runs from 1 (broad: documents matching some of the words qualify, more
results) to 11 (strict: documents must match the query closely, fewer and more exact results).
Use a low level for exploratory search boxes and a high level when users type exact titles or
product names. Values outside 1..11 are rejected by ``SearchQuery`` before sending.

Precondition: as ``first_search``, on a project that uses a precision search template (your
administrator can tell you). Run: ./run.sh precision_levels "dark knight rises"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "dark knight rises")
    with _common.connect() as ss:
        for precision in (1, 6, 11):
            query = smartsearch_ai.SearchQuery(
                q,
                precision=precision,  # 1 = broad ... 11 = strict
                neural_mode=smartsearch_ai.NeuralMode.BM25,  # precision applies to keyword matching
                response_fields=["title"],
                size=3,
            )
            print(f"precision={precision}:")
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query), "title"
            )


if __name__ == "__main__":
    _common.run(main)
