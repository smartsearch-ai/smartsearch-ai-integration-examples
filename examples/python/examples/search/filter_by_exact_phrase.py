"""Filter on an exact phrase: the words together and in order.

``Filter.match_phrase(field, phrase)`` keeps documents whose field contains the phrase word for
word. ``Filter.match`` with the same words would also accept them scattered through the text.
Letter case does not matter.

Precondition: as ``first_search``. Run: ./run.sh filter_by_exact_phrase
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    phrase = _common.query_text(args, "a galaxy far, far away")
    with _common.connect() as ss:
        query = smartsearch_ai.SearchQuery(
            "galaxy",
            response_fields=["title", "tagline"],
            filters=[
                smartsearch_ai.Filter.match_phrase("tagline", phrase)
            ],  # tagline contains the phrase
            size=3,
        )
        print(f'tagline contains "{phrase}":')
        # POST {api_url}/core/projects/{project_id}/search
        _common.print_hits(
            ss.search().search(_common.project_id(), query), "title", "tagline"
        )


if __name__ == "__main__":
    _common.run(main)
