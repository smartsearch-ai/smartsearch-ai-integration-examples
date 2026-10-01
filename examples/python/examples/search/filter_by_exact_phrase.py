"""Filter on an exact phrase: the words together and in order.

``Filter.match_phrase(field, phrase)`` keeps documents whose field contains the phrase word for
word. ``Filter.match`` with the same words would also accept them scattered through the text.
Letter case does not matter.

Precondition: as ``first_search``. Run: ./run.sh filter_by_exact_phrase
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    phrase = query_text(args, "a galaxy far, far away")
    with connect() as ss:
        query = SearchQuery(
            "galaxy",
            response_fields=["title", "tagline"],
            filters=[Filter.match_phrase("tagline", phrase)],   # tagline contains the phrase
            size=3,
        )
        print(f'tagline contains "{phrase}":')
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), "title", "tagline")


if __name__ == "__main__":
    run(main)
