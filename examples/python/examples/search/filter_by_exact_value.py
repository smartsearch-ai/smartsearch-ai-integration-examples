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

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "love")
    with _common.connect() as ss:
        french = smartsearch_ai.SearchQuery(
            q,
            response_fields=["title", "original_language"],
            filters=[
                smartsearch_ai.Filter.term("original_language", "fr")
            ],  # must equal "fr"
            size=3,
        )
        print("original_language = fr:")
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), french),
            "title",
            "original_language",
        )

        comedies = smartsearch_ai.SearchQuery(
            q,
            response_fields=["title", "genres"],
            filters=[
                smartsearch_ai.Filter.term("genres.name", "Comedy")
            ],  # a property inside a list of objects
            size=3,
        )
        print("genres.name = Comedy:")
        _common.print_hits(
            ss.search().search(_common.project_id(), comedies),
            "title",
            "genres",
        )


if __name__ == "__main__":
    _common.run(main)
