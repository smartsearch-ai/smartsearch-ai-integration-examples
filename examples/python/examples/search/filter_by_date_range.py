"""Filter by a date range: films released in the year 2000.

The range filter takes numbers, and a date field accepts the number formats it was set up with
in your project. The Movies sample's ``release_date`` accepts whole years, so
``gte=2000, lt=2001`` means "released in 2000". Ask your administrator which formats your date
fields accept; a bound the field cannot read makes the server refuse the request.

Precondition: as ``first_search``. Run: ./run.sh filter_by_date_range love
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    with _common.connect() as ss:
        query = smartsearch_ai.SearchQuery(
            _common.query_text(args, "love"),
            response_fields=["title", "release_date"],
            filters=[
                smartsearch_ai.Filter.range("release_date", gte=2000, lt=2001)
            ],  # 2000 <= year < 2001
            size=5,
        )
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), query),
            "title",
            "release_date",
        )


if __name__ == "__main__":
    _common.run(main)
