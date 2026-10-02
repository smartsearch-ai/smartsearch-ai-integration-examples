"""Combine filters: AND across ``filters``, OR inside ``AnyOf(...)``, NOT with ``exclude``.

This request reads: English AND rated at least 7 AND (science fiction or adventure OR has a
tagline) AND NOT rumored. The three parts map to the three parts of SSPL filters:

- each entry in ``filters``: every one must match (``filters.all``);
- ``AnyOf(f1, f2, ...)`` inside ``filters``: at least one of them must match (an ``any`` group);
- each entry in ``exclude``: none may match (``filters.not``).

Precondition: as ``first_search``. Run: ./run.sh combine_filters_with_any_of space
"""

import smartsearch_ai

from examples import _common

FIELDS = ["title", "original_language", "vote_average", "genres"]


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    with _common.connect() as ss:
        query = smartsearch_ai.SearchQuery(
            _common.query_text(args, "space"),
            response_fields=FIELDS,
            filters=[
                smartsearch_ai.Filter.term(
                    "original_language", "en"
                ),  # AND English
                smartsearch_ai.Filter.range(
                    "vote_average", gte=7.0
                ),  # AND rated >= 7
                smartsearch_ai.AnyOf(
                    smartsearch_ai.Filter.terms(
                        "genres.name", "Science Fiction", "Adventure"
                    ),
                    smartsearch_ai.Filter.exists("tagline"),
                ),  # AND (genre OR tagline)
            ],
            exclude=[
                smartsearch_ai.Filter.term("status", "Rumored")
            ],  # AND NOT rumored
            size=5,
        )
        print(
            "request: " + query.to_json()
        )  # see how the three parts are written
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), query), *FIELDS
        )


if __name__ == "__main__":
    _common.run(main)
