"""Multi-turn chat: follow-up questions that refer to earlier ones ("when was he born?").

A *chat session* holds the conversation. The first ``chat`` call creates one and returns its
``session_id``; send that ID with ``session_id=...`` on every later turn so the agent reads the
question in context. Keep one session per conversation in your app. The session is separate from
memory: ``MemoryMode.STANDARD`` still keeps the conversation, it only stops long-term memory.

Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an answering
agent. Run: ./run.sh chat_with_follow_up_questions "Who was President Kennedy?" "When was he born?"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    workspace = _common.workspace_id()
    first = args[0] if len(args) > 0 else "Who wrote our travel policy?"
    follow_up = args[1] if len(args) > 1 else "When was it last updated?"
    with _common.connect() as ss:
        # Turn 1, no session ID: the server starts a session.
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/chat
        turn1 = (
            ss.workplace()
            .chat(
                workspace,
                smartsearch_ai.WorkspaceQueryRequest(
                    query=first, memory_mode=smartsearch_ai.MemoryMode.STANDARD
                ),
            )
            .body
        )
        session_id = turn1.get("session_id")
        print("Q1: " + first)
        print("A1: " + _common.shorten(turn1.get("answer"), 250))
        if session_id is None:
            raise RuntimeError("The server did not return a chat session")

        # Turn 2: the same session, so "he" / "it" refers to turn 1.
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/chat   (with
        # session_id)
        turn2 = (
            ss.workplace()
            .chat(
                workspace,
                smartsearch_ai.WorkspaceQueryRequest(
                    query=follow_up,
                    session_id=session_id,
                    memory_mode=smartsearch_ai.MemoryMode.STANDARD,
                ),
            )
            .body
        )
        print("Q2: " + follow_up)
        print("A2: " + _common.shorten(turn2.get("answer"), 250))
        print(
            f"same session: {str(session_id == turn2.get('session_id')).lower()}"
        )


if __name__ == "__main__":
    _common.run(main)
