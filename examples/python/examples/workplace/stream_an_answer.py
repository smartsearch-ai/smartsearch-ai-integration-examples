"""Stream an answer: print it as it is written instead of waiting for all of it.

``stream_query`` sends the same request as ``query`` and returns a ``WorkspaceStream`` of
server-sent events. Iterate over it; the loop ends after the final event. The events, in order::

    run.started       the run began                      payload: mode
    sources.selected  the documents were chosen          payload: sources, source_count
    answer.delta      the next piece of text (many)      payload: text
    answer.completed  the text is complete               payload: citation_count
    run.completed     final result                       payload: run_id, mode, status, answer, sources, citations, memory

The server may also send keep-alive events; ignore names you do not handle. After the stream
ends, ``stream.result`` holds the final response. It is authoritative: show the pieces while
they arrive, then replace them with the final answer if you store it.

Errors and timeouts. A failure reported in the stream (``run.failed`` or ``error``) and a wait
longer than the read timeout (default 60 seconds, code ``WORKSPACE_STREAM_TIMEOUT``) both raise
``SmartSearchError`` from the loop. Always close the stream (``with``): that stops reading and
releases the connection, also when you stop early. Read a stream from one thread at a time.

Precondition. Your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an answering
agent. Run: ./run.sh stream_an_answer "What causes high blood pressure?"
"""

import json

from smartsearch_ai import MemoryMode, Mode, SmartSearchError, WorkspaceQueryRequest

from examples._common import connect, query_text, run, workspace_id


def main(args: list[str]) -> None:
    question = query_text(args, "Summarize our travel policy.")
    request = WorkspaceQueryRequest(query=question, mode=Mode.ANSWER, memory_mode=MemoryMode.STANDARD)
    # POST {api_url}/workspace/v1/workspaces/{workspace_id}/query
    #      with "options": {"stream": true} and Accept: text/event-stream (the SDK sets both)
    with connect() as ss, ss.workplace().stream_query(workspace_id(), request) as stream:
        pieces = 0
        try:
            for event in stream:                        # waits for the next event; ends after run.completed
                payload = event.payload
                if event.event == "run.started":
                    print(f"[run started, mode={payload.get('mode')}]")
                elif event.event == "sources.selected":
                    print(f"[{payload.get('source_count')} sources selected]")
                elif event.event == "answer.delta":
                    print(payload.get("text", ""), end="", flush=True)   # print each piece as it arrives
                    pieces += 1
                elif event.event == "answer.completed":
                    print(f"\n[answer completed, citations={payload.get('citation_count')}]")
                elif event.event == "run.completed":
                    print(f"[run completed, status={payload.get('status')}]")
                # other names: keep-alives and future event types
        except SmartSearchError:
            # The run failed or timed out part-way; what was printed so far is incomplete.
            print()
            raise
        # The final response, the same shape as query() returns.
        result = stream.result.body
        print(f"pieces={pieces} final answer length={len(result.get('answer') or '')}"
              f" sources={len(result.get('sources', []))}"
              f" memory={json.dumps(result.get('memory'), separators=(',', ':'))}")


if __name__ == "__main__":
    run(main)
