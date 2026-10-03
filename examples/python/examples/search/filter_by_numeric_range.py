"""Filter by a numeric range: rating at least 7.5, or between 5 and 6.

``Filter.range(field, gte=, lte=, gt=, lt=)`` takes up to four bounds; give at least one.
gte = "at least", lte = "at most", gt = "more than", lt = "less than". Whole numbers stay whole
numbers on the wire; decimals are sent as decimals. Use it for prices, ratings, sizes, durations.

Precondition: as ``first_search``. Run: ./run.sh filter_by_numeric_range love
"""

import smartsearch_ai

from examples import _common

FIELDS = ["title", "vote_average"]


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "love")
    with _common.connect() as ss:
        well_rated = smartsearch_ai.SearchQuery(
            q,
            response_fields=FIELDS,
            size=3,
            filters=[smartsearch_ai.Filter.range("vote_average", gte=7.5)],
        )  # vote_average >= 7.5
        print("vote_average >= 7.5:")
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), well_rated), *FIELDS
        )

        between = smartsearch_ai.SearchQuery(
            q,
            response_fields=FIELDS,
            size=3,
            filters=[
                smartsearch_ai.Filter.range("vote_average", gt=5.0, lt=6.0)
            ],
        )  # 5.0 < vote_average < 6.0
        print("5.0 < vote_average < 6.0:")
        _common.print_hits(
            ss.search().search(_common.project_id(), between), *FIELDS
        )


if __name__ == "__main__":
    _common.run(main)
