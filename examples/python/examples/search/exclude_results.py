"""Exclude documents: no dramas, no romances, nothing unreleased.

``exclude=[filter, ...]`` removes every document that matches any of the filters. It accepts
every filter type. With ``Filter.terms``, a document is removed if it has ANY of the values, so
``Filter.terms("genres.name", "Drama", "Romance")`` drops dramas and romances alike.

Precondition: as ``first_search``. Run: ./run.sh exclude_results love
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
            response_fields=["title", "genres", "status"],
            exclude=[
                smartsearch_ai.Filter.terms(
                    "genres.name", "Drama", "Romance"
                ),  # neither genre
                smartsearch_ai.Filter.term(
                    "status", "Rumored"
                ),  # and not unreleased
            ],
            size=5,
        )
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), query),
            "title",
            "genres",
            "status",
        )


if __name__ == "__main__":
    _common.run(main)
