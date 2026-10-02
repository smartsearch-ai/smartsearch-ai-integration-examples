"""Memory: STANDARD (no long-term memory) versus AGENTIC (long-term memory for the caller).

Workplace can keep a long-term memory of what an identity asked, and use it in later answers,
across sessions. Memory belongs to the calling identity: your service key when you call as your
service, the user when you act as a user.

- ``MemoryMode.STANDARD``: nothing is stored or recalled. The response reports
  ``"write_status": "not_applicable"``. Use it when you call as your service on behalf of many
  people (they would otherwise share one memory), and for one-off questions.
- ``MemoryMode.AGENTIC``: the question is recorded in the caller's memory; the response reports
  ``"write_status": "queued"``, meaning the memory write was accepted and is processed in the
  background. Use it when you act as an individual user who benefits from personal context.

Without ``memory_mode`` the server's default applies, and it can be AGENTIC: set the mode
explicitly on every request. Memory is separate from a chat session (which only holds one
conversation, see ``chat_with_follow_up_questions``).

Precondition. Your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an answering
agent. Note that the AGENTIC call below records the question in your service key's memory.
Run: ./run.sh answer_with_memory "What is a normal blood pressure?"
"""

import json

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    question = _common.query_text(args, "What is our travel policy?")
    with _common.connect() as ss:
        for memory in (
            smartsearch_ai.MemoryMode.STANDARD,
            smartsearch_ai.MemoryMode.AGENTIC,
        ):
            request = smartsearch_ai.WorkspaceQueryRequest(
                query=question,
                mode=smartsearch_ai.Mode.ANSWER,
                memory_mode=memory,  # STANDARD or AGENTIC
            )
            # POST {api_url}/workspace/v1/workspaces/{workspace_id}/query
            body = ss.workplace().query(_common.workspace_id(), request).body
            # memory = { "mode": what the server used, "write_status": what it
            # did with memory }
            print(
                f"{memory.name}: memory={json.dumps(body.get('memory'), separators=(',', ':'))}"
                f" answer={_common.shorten(body.get('answer'), 100)}"
            )


if __name__ == "__main__":
    _common.run(main)
