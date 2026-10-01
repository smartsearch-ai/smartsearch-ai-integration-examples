"""Spelling correction: a misspelled query still finds what the user meant.

With ``auto_correct=True`` the server corrects likely typos before searching. On the Movies
sample, "terminater" then finds "The Terminator". Turn it off when users search exact codes or
names that look like typos.

Precondition: as ``first_search``, on a project with spelling correction set up (your
administrator can tell you). Run: ./run.sh correct_spelling terminater
"""

from smartsearch_ai import NeuralMode, SearchQuery

from examples._common import connect, print_hits, project_id, query_text, run


def main(args: list[str]) -> None:
    misspelled = query_text(args, "terminater")
    with connect() as ss:
        for correct in (False, True):
            query = SearchQuery(
                misspelled,
                auto_correct=correct,           # True: fix likely typos first
                neural_mode=NeuralMode.BM25,    # keyword search shows the effect clearly
                response_fields=["title"],
                size=3,
            )
            print(f'autoCorrect={str(correct).lower()} for "{misspelled}":')
            # POST {api_url}/core/projects/{project_id}/search
            print_hits(ss.search().search(project_id(), query), "title")


if __name__ == "__main__":
    run(main)
