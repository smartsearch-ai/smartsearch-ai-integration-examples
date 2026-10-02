"""Ask a question and get an answer written from your documents, with the sources it used.

``query`` in ``ANSWER`` mode: Workplace finds the best documents in the workspace, the
workspace's answering agent reads them and writes an answer, and the response lists the sources
and the citations. Use it for a "question box" that answers instead of listing results. For a
conversation with follow-up questions use chat instead (``chat_with_follow_up_questions``).

Request fields set here:

- ``query``: the question, in natural language;
- ``mode=Mode.ANSWER``: write an answer (``RETRIEVAL_ONLY`` returns sources only);
- ``memory_mode=MemoryMode.STANDARD``: store and recall no long-term memory (see
  ``answer_with_memory``). Set it explicitly: the server's default may use memory;
- ``QueryOptions``: ``include_sources=True`` returns the documents used;
  ``max_context_documents=n`` caps how many documents the agent reads (fewer = faster).

Response (``result.body``)::

    run_id     identifies this answer run; quote it when reporting a problem
    mode       "answer"
    status     "COMPLETED" when the answer was produced
    answer     the answer text; [1], [2] refer to the numbered sources
    sources    the documents used: title, snippet, source_url, source_id, document_id, score, n (its number)
    citations  the sources the answer actually cites
    memory     what happened to memory: {"mode": "standard", "write_status": "not_applicable"}

Precondition. Your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an answering
agent. Answers take seconds; the SDK's default read timeout is 60 seconds.
Run: ./run.sh ask_a_question "What is a normal blood pressure?"
"""

import json
import time

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    question = _common.query_text(args, "What is our travel policy?")
    with _common.connect() as ss:
        request = smartsearch_ai.WorkspaceQueryRequest(
            query=question,
            mode=smartsearch_ai.Mode.ANSWER,  # write an answer
            memory_mode=smartsearch_ai.MemoryMode.STANDARD,  # no long-term memory
            options=smartsearch_ai.QueryOptions(
                include_sources=True,  # return the documents used
                max_context_documents=5,
            ),  # the agent reads at most 5
        )
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/query
        # Errors: SmartSearchError (HTTP status + server error code), for
        # example when the key is
        # not a member or the workspace has no answering agent;
        # RequestTimeoutError when the
        # answer takes longer than the read timeout.
        start = time.monotonic()
        result = ss.workplace().query(_common.workspace_id(), request)
        elapsed_ms = int((time.monotonic() - start) * 1000)
        body = result.body
        print(
            f"HTTP {result.status_code} in {elapsed_ms} ms"
            f", run_id={'present' if body.get('run_id') is not None else 'none'}"
            f", mode={body.get('mode')}, status={body.get('status')}"
        )
        print("answer: " + _common.shorten(body.get("answer"), 300))
        # Show sources by their number n, so [1], [2] in the answer can be
        # matched to them.
        print(
            f"sources={len(body.get('sources', []))} citations={len(body.get('citations', []))}"
        )
        for source in body.get("sources", []):
            print(
                f"  [{source.get('n')}] {_common.shorten(source.get('title'), 80)}"
            )
        print("memory=" + json.dumps(body.get("memory"), separators=(",", ":")))


if __name__ == "__main__":
    _common.run(main)
