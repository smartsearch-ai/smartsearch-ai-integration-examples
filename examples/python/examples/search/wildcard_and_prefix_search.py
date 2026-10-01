"""Wildcard and prefix search: ``avat*`` finds avatar, avatars, and so on.

With ``wildcard=True`` a ``*`` in the query stands for any characters. It suits part numbers,
codes and search-as-you-type prefixes. Wildcards are a keyword feature: use keyword ranking
(``NeuralMode.BM25``), as here.

Precondition: as ``first_search``. Run: ./run.sh wildcard_and_prefix_search "avat*"
"""

from smartsearch_ai import NeuralMode, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    pattern = query_text(args, "avat*")
    with connect() as ss:
        for wildcard in (True, False):
            query = SearchQuery(
                pattern,
                wildcard=wildcard,              # True: '*' matches any characters
                neural_mode=NeuralMode.BM25,
                response_fields=["title"],
                size=5,
            )
            print(f'wildcard={str(wildcard).lower()} for "{pattern}":')
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")


if __name__ == "__main__":
    run(main)
