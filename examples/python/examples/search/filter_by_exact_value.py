"""Filter by an exact value: only French films, only comedies.

Filters versus the query. The query (``q``) decides how well a document matches and so its rank.
A filter is a yes/no condition: documents that fail it are removed, and the filter does not
change the scores of the rest. Use filters for choices the user makes in the interface (a
language picker, a category menu).

``Filter.term(field, value)`` keeps documents whose field equals the value exactly (case and
spelling matter). For a list of objects, filter on the object's property: in the Movies data
``genres`` is a list like ``[{"id":35,"name":"Comedy"}]``, so the field is ``genres.name``.
Filtering on ``genres`` itself returns no hits and no error.

Precondition: as ``first_search``. Run: ./run.sh filter_by_exact_value love
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    q = query_text(args, "love")
    with connect() as ss:
        french = SearchQuery(
            q,
            response_fields=["title", "original_language"],
            filters=[Filter.term("original_language", "fr")],   # must equal "fr"
            size=3,
        )
        print("original_language = fr:")
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), french), "title", "original_language")

        comedies = SearchQuery(
            q,
            response_fields=["title", "genres"],
            filters=[Filter.term("genres.name", "Comedy")],     # a property inside a list of objects
            size=3,
        )
        print("genres.name = Comedy:")
        print_hits(ss.search().search(project_id(), comedies), "title", "genres")


if __name__ == "__main__":
    run(main)
