"""Combine filters: AND across ``filters``, OR inside ``AnyOf(...)``, NOT with ``exclude``.

This request reads: English AND rated at least 7 AND (science fiction or adventure OR has a
tagline) AND NOT rumored. The three parts map to the three parts of SSPL filters:

- each entry in ``filters``: every one must match (``filters.all``);
- ``AnyOf(f1, f2, ...)`` inside ``filters``: at least one of them must match (an ``any`` group);
- each entry in ``exclude``: none may match (``filters.not``).

Precondition: as ``first_search``. Run: ./run.sh combine_filters_with_any_of space
"""

from smartsearch_ai import AnyOf, Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run

FIELDS = ["title", "original_language", "vote_average", "genres"]


def main(args: list[str]) -> None:
    with connect() as ss:
        query = SearchQuery(
            query_text(args, "space"),
            response_fields=FIELDS,
            filters=[
                Filter.term("original_language", "en"),                    # AND English
                Filter.range("vote_average", gte=7.0),                     # AND rated >= 7
                AnyOf(Filter.terms("genres.name", "Science Fiction", "Adventure"),
                      Filter.exists("tagline")),                           # AND (genre OR tagline)
            ],
            exclude=[Filter.term("status", "Rumored")],                    # AND NOT rumored
            size=5,
        )
        print("request: " + query.to_json())   # see how the three parts are written
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), *FIELDS)


if __name__ == "__main__":
    run(main)
