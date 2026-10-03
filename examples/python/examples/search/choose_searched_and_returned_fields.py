"""Searched fields versus returned fields: two different lists.

``fields`` says WHERE the query text is looked for. ``response_fields`` says WHAT each hit sends
back. Without ``fields`` the project's configured fields are searched. Searching only the title
finds titles containing the word; searching the plot summary finds documents that are about it.
Returning few fields keeps responses small.

Keyword search is used here (``NeuralMode.BM25``) because the searched-fields list applies to
keyword matching; semantic search compares meaning instead (see ``keyword_vs_semantic_vs_hybrid``).

Precondition: as ``first_search``. Run: ./run.sh choose_searched_and_returned_fields galaxy
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "galaxy")
    with _common.connect() as ss:
        for searched in ("title", "overview"):
            query = smartsearch_ai.SearchQuery(
                q,
                fields=[searched],  # search only this field
                response_fields=["title"],  # but return only the title
                neural_mode=smartsearch_ai.NeuralMode.BM25,
                size=3,
            )
            print(f'q="{q}" searched in {searched}:')
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query), "title"
            )


if __name__ == "__main__":
    _common.run(main)
