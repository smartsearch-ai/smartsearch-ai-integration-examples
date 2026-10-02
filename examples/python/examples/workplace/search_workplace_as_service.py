"""Search Workplace as your service (no end user involved): the documents that match a query.

Concepts. *Workplace* is SmartSearch AI's search and answer engine for company knowledge. A
*workspace* groups *sources* (connected document collections such as a file share or a wiki) and
an answering agent that writes answers from them. Three calls:

- search (this example): the matching documents, no language model. Use it for a results list.
- query: one question, one answer written from the documents, with its sources
  (``ask_a_question``), or the sources only (``retrieve_sources_only``).
- chat: like query, in a conversation where follow-up questions build on earlier turns
  (``chat_with_follow_up_questions``).

Whose access? Called as your service, results reflect the service key's own access. To respect
each person's permissions, act as that user instead (``search_workplace_as_user``,
``sign_in_with_your_identity_provider``).

Precondition. Your service key is a member of SMARTSEARCH_WORKSPACE_ID.
Run: ./run.sh search_workplace_as_service "blood pressure"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    text = _common.query_text(args, "travel policy")
    with _common.connect() as ss:
        # query sets the query text; nothing else is required.
        request = smartsearch_ai.WorkspaceQueryRequest(query=text)
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/search
        # Errors: SmartSearchError with the HTTP status (status_code) and the
        # server's error code
        # (code), for example when the key is not a member.
        result = ss.workplace().search(_common.workspace_id(), request)
        # result.body = { "documents": [ { "title", "snippet", "source_url", ...
        # }, ... ] }
        # result.request_id identifies the call; quote it when reporting a
        # problem.
        print(
            f"HTTP {result.status_code} requestId={'present' if result.request_id is not None else 'none'}"
        )
        _common.print_documents(result.body)


if __name__ == "__main__":
    _common.run(main)
