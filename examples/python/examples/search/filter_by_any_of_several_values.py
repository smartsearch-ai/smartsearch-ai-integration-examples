"""Filter by any of several values of one field: horror OR animation.

``Filter.terms(field, v1, v2, ...)`` keeps documents whose field equals at least one of the
values. It is the natural fit for a multi-select facet ("Genre: [x] Horror [x] Animation"). To
require a value from each of several fields, put one filter per field in ``filters`` (see
``combine_filters_with_any_of``).

Precondition: as ``first_search``. Run: ./run.sh filter_by_any_of_several_values love
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "love"),
            response_fields=["title", "genres"],
            filters=[Filter.terms("genres.name", "Horror", "Animation")],   # Horror OR Animation
            size=5,
        )
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), "title", "genres")


if __name__ == "__main__":
    run(main)
