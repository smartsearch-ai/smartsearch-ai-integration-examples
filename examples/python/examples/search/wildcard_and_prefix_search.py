"""Wildcard and prefix search: ``avat*`` finds avatar, avatars, and so on.

With ``wildcard=True`` a ``*`` in the query stands for any characters. It suits part numbers,
codes and search-as-you-type prefixes. Wildcards are a keyword feature: use keyword ranking
(``NeuralMode.BM25``), as here.

Precondition: as ``first_search``. Run: ./run.sh wildcard_and_prefix_search "avat*"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    pattern = _common.query_text(args, "avat*")
    with _common.connect() as ss:
        for wildcard in (True, False):
            query = smartsearch_ai.SearchQuery(
                pattern,
                wildcard=wildcard,  # True: '*' matches any characters
                neural_mode=smartsearch_ai.NeuralMode.BM25,
                response_fields=["title"],
                size=5,
            )
            print(f'wildcard={str(wildcard).lower()} for "{pattern}":')
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query), "title"
            )


if __name__ == "__main__":
    _common.run(main)
