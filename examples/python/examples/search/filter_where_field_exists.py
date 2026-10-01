"""Keep only documents that have a value in a field: films with a tagline.

``Filter.exists(field)`` is useful when a result card needs a field to display (an image, a
price, a summary), or to hide incomplete records. To find documents WITHOUT the field, pass the
same filter in ``exclude`` (see ``exclude_results``).

Precondition: as ``first_search``. Run: ./run.sh filter_where_field_exists love
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "love"),
            response_fields=["title", "tagline"],
            filters=[Filter.exists("tagline")],    # tagline has a value
            size=3,
        )
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), "title", "tagline")


if __name__ == "__main__":
    run(main)
