"""Answer from part of a workspace only: one source, and documents whose title contains a word.

A query takes the same scoping as a search (``filter_workplace_by_source_and_title``):
``source_ids=[id]`` limits it to a source, and ``filters={"all": [clause, ...]}`` adds
conditions every document must meet. A clause is ``search_type`` (``term``, ``terms``,
``match`` or ``match_phrase``), ``field`` and ``value``, on ``source_type``, ``title``,
``content``, ``snippet``, ``source_url``, ``author`` or your own ``metadata.*`` fields. The
answer is then written only from documents that pass. Access-control fields cannot be filtered
on; the SDK rejects them.

Each source in the response carries its ``source_id``, so you can confirm the scoping.

Precondition. The caller can see SMARTSEARCH_SOURCE_ID in SMARTSEARCH_WORKSPACE_ID, which has an
answering agent.
Run: ./run.sh query_with_filters "What is a normal blood pressure?" pressure
(question, then a word the titles must contain).
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    question = args[0] if len(args) > 0 else "What is our travel policy?"
    title_word = args[1] if len(args) > 1 else "policy"
    source_id = _common.require("SMARTSEARCH_SOURCE_ID")
    with _common.connect() as ss:
        request = smartsearch_ai.WorkspaceQueryRequest(
            query=question,
            mode=smartsearch_ai.Mode.ANSWER,
            memory_mode=smartsearch_ai.MemoryMode.STANDARD,
            source_ids=[source_id],  # only this source
            filters={
                "all": [  # every clause must match
                    {
                        "search_type": "match",
                        "field": "title",
                        "value": title_word,
                    }
                ]
            },
        )
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/query   (with
        # source_ids and filters)
        # Errors: ValueError before sending for an unsupported clause or field;
        # SmartSearchError
        # when the server refuses.
        body = ss.workplace().query(_common.workspace_id(), request).body
        print("answer: " + _common.shorten(body.get("answer"), 250))
        print(f"sources={len(body.get('sources', []))}")
        for source in body.get("sources", []):
            print(
                f"  [{source.get('n')}] {_common.shorten(source.get('title'), 70)}"
                f" (from the chosen source: {str(source.get('source_id') == source_id).lower()})"
            )


if __name__ == "__main__":
    _common.run(main)
