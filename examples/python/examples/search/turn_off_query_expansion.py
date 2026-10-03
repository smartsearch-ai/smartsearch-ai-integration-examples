"""Turn off query expansion for one request.

*Query expansion* widens a query with related words (synonyms and similar terms) so users find
documents that use different words for the same thing. Whether it is on is a project setting.
``disable_query_expansion=True`` turns it off for this request only, for example when the user
asks for an exact term. There is no per-request way to turn it on.

The example runs the same query both ways and prints the scores, so you can see whether
expansion changes anything on your project.

Precondition: as ``first_search``. Run: ./run.sh turn_off_query_expansion car
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "car")
    with _common.connect() as ss:
        base = dict(
            neural_mode=smartsearch_ai.NeuralMode.BM25,
            response_fields=["title"],
            size=5,
        )
        print("Project default:")
        # POST {api_url}/core/projects/{project_id}/search
        print_with_scores(
            ss.search().search(
                _common.project_id(), smartsearch_ai.SearchQuery(q, **base)
            )
        )
        print("Query expansion off:")
        print_with_scores(
            ss.search().search(
                _common.project_id(),
                smartsearch_ai.SearchQuery(
                    q, **base, disable_query_expansion=True
                ),
            )
        )


def print_with_scores(result: smartsearch_ai.SearchResult) -> None:
    for hit in _common.hits_of(result):
        print(
            f"  {str(hit.get('_source', {}).get('title')):<40} score={hit.get('_score') or 0:.3f}"
        )


if __name__ == "__main__":
    _common.run(main)
