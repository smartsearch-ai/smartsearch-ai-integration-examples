"""Spelling correction: a misspelled query still finds what the user meant.

With ``auto_correct=True`` the server corrects likely typos before searching. On the Movies
sample, "terminater" then finds "The Terminator". Turn it off when users search exact codes or
names that look like typos.

Precondition: as ``first_search``, on a project with spelling correction set up (your
administrator can tell you). Run: ./run.sh correct_spelling terminater
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    misspelled = _common.query_text(args, "terminater")
    with _common.connect() as ss:
        for correct in (False, True):
            query = smartsearch_ai.SearchQuery(
                misspelled,
                auto_correct=correct,  # True: fix likely typos first
                neural_mode=smartsearch_ai.NeuralMode.BM25,  # keyword search shows the effect clearly
                response_fields=["title"],
                size=3,
            )
            print(f'autoCorrect={str(correct).lower()} for "{misspelled}":')
            # POST {api_url}/core/projects/{project_id}/search
            _common.print_hits(
                ss.search().search(_common.project_id(), query), "title"
            )


if __name__ == "__main__":
    _common.run(main)
