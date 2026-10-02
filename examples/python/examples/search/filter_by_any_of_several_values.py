"""Filter by any of several values of one field: horror OR animation.

``Filter.terms(field, v1, v2, ...)`` keeps documents whose field equals at least one of the
values. It is the natural fit for a multi-select facet ("Genre: [x] Horror [x] Animation"). To
require a value from each of several fields, put one filter per field in ``filters`` (see
``combine_filters_with_any_of``).

Precondition: as ``first_search``. Run: ./run.sh filter_by_any_of_several_values love
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
            response_fields=["title", "genres"],
            filters=[
                smartsearch_ai.Filter.terms(
                    "genres.name", "Horror", "Animation"
                )
            ],  # Horror OR Animation
            size=5,
        )
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), query), "title", "genres"
        )


if __name__ == "__main__":
    _common.run(main)
