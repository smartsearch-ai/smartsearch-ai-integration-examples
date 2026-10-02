"""Keep only documents that have a value in a field: films with a tagline.

``Filter.exists(field)`` is useful when a result card needs a field to display (an image, a
price, a summary), or to hide incomplete records. To find documents WITHOUT the field, pass the
same filter in ``exclude`` (see ``exclude_results``).

Precondition: as ``first_search``. Run: ./run.sh filter_where_field_exists love
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
            response_fields=["title", "tagline"],
            filters=[
                smartsearch_ai.Filter.exists("tagline")
            ],  # tagline has a value
            size=3,
        )
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), query), "title", "tagline"
        )


if __name__ == "__main__":
    _common.run(main)
