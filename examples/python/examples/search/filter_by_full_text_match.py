"""Filter on words inside a text field: love stories whose plot mentions Paris.

``Filter.match(field, text)`` is a full-text condition. Unlike ``Filter.term``, which compares
the whole value exactly, match reads the text the way search does: it finds the word anywhere in
the field, whatever its letter case. Use term for codes and categories, match for free text.

Precondition: as ``first_search``. Run: ./run.sh filter_by_full_text_match love paris
(first word = query, second = word the plot must mention).
"""

from smartsearch_ai import Filter, SearchQuery

from examples._common import connect, print_hits, project_id, run


def main(args: list[str]) -> None:
    q = args[0] if len(args) > 0 else "love"
    word = args[1] if len(args) > 1 else "paris"
    with connect() as ss:
        query = SearchQuery(
            q,
            response_fields=["title", "overview"],
            filters=[Filter.match("overview", word)],   # overview mentions the word
            size=3,
        )
        print(f'q="{q}", overview matches "{word}":')
        # POST {api_url}/core/projects/{project_id}/search
        print_hits(ss.search().search(project_id(), query), "title", "overview")


if __name__ == "__main__":
    run(main)
