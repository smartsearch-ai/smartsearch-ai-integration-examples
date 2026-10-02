"""Narrow a Workplace search to one source, and filter on a document field.

``source_ids=[id]`` limits the search to sources of the workspace (list more to add them).
``filters`` adds conditions on document fields, written as ``{"all": [clause, ...]}``: every
clause must match. A clause is a dict with ``search_type`` (``term``, ``terms``, ``match`` or
``match_phrase``), ``field`` and ``value``. Fields you can filter on: ``source_type``, ``title``,
``content``, ``snippet``, ``source_url``, ``author``, and your own fields under ``metadata.*``.

Access-control fields are set by the server and cannot be filtered on: the SDK rejects them
before sending, so a caller can never widen their own access.

Precondition: the caller can see SMARTSEARCH_SOURCE_ID in SMARTSEARCH_WORKSPACE_ID.
Run: ./run.sh filter_workplace_by_source_and_title "blood pressure" pressure
(query, then a word the title must contain).
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    query = args[0] if len(args) > 0 else "travel policy"
    title_word = args[1] if len(args) > 1 else "policy"
    with _common.connect() as ss:
        request = smartsearch_ai.WorkspaceQueryRequest(
            query=query,
            source_ids=[
                _common.require("SMARTSEARCH_SOURCE_ID")
            ],  # only this source
            filters={
                "all": [  # AND of these clauses
                    {
                        "search_type": "match",
                        "field": "title",
                        "value": title_word,
                    }
                ]
            },
        )
        print("Source + title filter:")
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/search
        _common.print_documents(
            ss.workplace().search(_common.workspace_id(), request).body
        )


if __name__ == "__main__":
    _common.run(main)
