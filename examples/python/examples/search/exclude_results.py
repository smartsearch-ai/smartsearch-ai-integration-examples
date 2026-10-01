"""Exclude documents: no dramas, no romances, nothing unreleased.

``exclude=[filter, ...]`` removes every document that matches any of the filters. It accepts
every filter type. With ``Filter.terms``, a document is removed if it has ANY of the values, so
``Filter.terms("genres.name", "Drama", "Romance")`` drops dramas and romances alike.

Precondition: as ``first_search``. Run: ./run.sh exclude_results love
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "love"),
            response_fields=["title", "genres", "status"],
            exclude=[
                Filter.terms("genres.name", "Drama", "Romance"),   # neither genre
                Filter.term("status", "Rumored"),                  # and not unreleased
            ],
            size=5,
        )
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), "title", "genres", "status")


if __name__ == "__main__":
    run(main)
