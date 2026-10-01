"""Filter by a date range: films released in the year 2000.

The range filter takes numbers, and a date field accepts the number formats it was set up with
in your project. The Movies sample's ``release_date`` accepts whole years, so
``gte=2000, lt=2001`` means "released in 2000". Ask your administrator which formats your date
fields accept; a bound the field cannot read makes the server refuse the request.

Precondition: as ``first_search``. Run: ./run.sh filter_by_date_range love
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "love"),
            response_fields=["title", "release_date"],
            filters=[Filter.range("release_date", gte=2000, lt=2001)],   # 2000 <= year < 2001
            size=5,
        )
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), "title", "release_date")


if __name__ == "__main__":
    run(main)
