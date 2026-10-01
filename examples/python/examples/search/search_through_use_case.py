"""Use-case search: search through a named, saved search configuration.

A *use case* belongs to a project and holds its own search settings (fields, relevance tuning,
search technique) for one screen or purpose, such as "site search" or "support articles".
Searching through it applies those settings, so your code does not repeat them and an
administrator can tune them without a code change. Send the same ``SearchQuery``; the only
difference is the use-case ID in place of the project ID.

Precondition: SMARTSEARCH_USECASE_ID is a use case of a project your service key is assigned to
(your administrator gives you the ID). An unknown ID fails with HTTP 404 "Usecase not found".

Run: ./run.sh search_through_use_case "star wars"
"""

from smartsearch_ai import SearchQuery

from examples._common import connect, print_hits, query_text, require, run


def main(args: list[str]) -> None:
    usecase_id = require("SMARTSEARCH_USECASE_ID")
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "star wars"),
            response_fields=["title", "release_date"],
            size=5,
        )
        # POST {api_url}/core/usecases/{usecase_id}/search
        print_hits(ss.search().search_by_usecase(usecase_id, query), "title", "release_date")


if __name__ == "__main__":
    run(main)
