"""Streaming chat: show the answer word by word as it is written, instead of waiting for all of it.

``stream_chat`` opens a stream of server-sent events. Iterate over it; events arrive in this order:

- ``run.started``: the run began (it carries the chat session);
- ``sources.selected``: the documents the answer will use were chosen;
- ``answer.delta``, many times: the next piece of answer text, in ``payload["text"]``;
- ``answer.completed``, then ``run.completed``: done.

After the stream ends, ``stream.result`` holds the complete final response (answer and sources);
it is authoritative over the pieces. Always close the stream (``with``): closing it early stops
reading and releases the connection. A wait longer than the read timeout fails with a
``RequestTimeoutError``, code ``WORKSPACE_STREAM_TIMEOUT``.

Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an answering
agent. The same call works as a user (``UserWorkplace.stream_chat``).
Run: ./run.sh stream_chat_answer "What causes high blood pressure? Answer briefly."
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    question = _common.query_text(
        args, "Summarize our travel policy in three bullet points."
    )
    # POST {api_url}/workspace/v1/workspaces/{workspace_id}/chat
    # with "options": {"stream": true} and Accept: text/event-stream (the SDK
    # sets both)
    with _common.connect() as ss, ss.workplace().stream_chat(
        _common.workspace_id(),
        smartsearch_ai.WorkspaceQueryRequest(
            query=question, memory_mode=smartsearch_ai.MemoryMode.STANDARD
        ),
    ) as stream:
        deltas = 0
        for (
            event
        ) in stream:  # blocks until the next event; ends after run.completed
            if event.event == "sources.selected":  # the event name
                print("[sources selected]")
            elif event.event == "answer.delta":
                print(
                    event.payload.get("text", ""), end="", flush=True
                )  # the next piece of text
                deltas += 1
            # others: run.started, answer.completed, run.completed, keep-alives
        print()
        if stream.result is None:
            raise RuntimeError("No final stream completion")
        print("Final status: " + str(stream.result.body.get("status")))
        _common.print_answer(stream.result.body)
        print(
            f"[done] deltas={deltas}"
            f" session={'yes' if stream.session_id is not None else 'no'}"  # reuse it for follow-ups
            f" final sources={len(stream.result.body.get('sources', [])) if stream.result else 0}"
        )


if __name__ == "__main__":
    _common.run(main)
