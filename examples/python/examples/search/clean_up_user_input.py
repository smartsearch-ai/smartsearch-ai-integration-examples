"""Clean up what users type: surrounding spaces and stray symbols.

``trim_query=True`` asks the server to remove leading and trailing whitespace;
``remove_special_chars=True`` asks it to remove special characters (symbols such as ``* ! ?``)
before searching. Turn both on for search boxes that take free text from users. Leave special
characters in when users search for codes that contain them (for example "C++" or "AT&T").
``SearchQuery`` itself rejects an empty or all-blank query with a ``ValueError``.

This example sends the same messy text both ways so you can compare the results on your own data.

Precondition: as ``first_search``. Run: ./run.sh clean_up_user_input
"""

from smartsearch_ai import NeuralMode, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    typed = query_text(args, "  ***the!!! matrix???  ")
    with connect() as ss:
        for clean in (False, True):
            query = SearchQuery(
                typed,
                trim_query=clean,               # remove surrounding whitespace
                remove_special_chars=clean,     # remove special characters
                neural_mode=NeuralMode.BM25,
                response_fields=["title"],
                size=3,
            )
            print(f'clean-up={str(clean).lower()} for "{typed}":')
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")


if __name__ == "__main__":
    run(main)
