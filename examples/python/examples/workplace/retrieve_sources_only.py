"""Get the evidence for a question without an answer: the documents an answer would be built from.

``query`` in ``RETRIEVAL_ONLY`` mode runs the same document selection as an answer but calls no
language model. Use it when you show the sources in your own interface, or pass them to your own
language model. It is faster than an answer and has no model cost.

Compared with ``search``: search returns a results list for a query; retrieval-only returns the
evidence Workplace would give its answering agent for a question, with the same fields as the
sources of an answer.

Response: ``run_id``, ``mode`` = "retrieval_only", ``status``, ``sources`` (title, snippet,
source_url, source_id, document_id, score, n), an empty ``citations`` list, and no ``answer``.

Precondition. Your service key is a member of SMARTSEARCH_WORKSPACE_ID.
Run: ./run.sh retrieve_sources_only "What is a normal blood pressure?"
"""

from smartsearch_ai import MemoryMode, Mode, WorkspaceQueryRequest

from examples._common import connect, query_text, run, shorten, workspace_id


def main(args: list[str]) -> None:
    question = query_text(args, "What is our travel policy?")
    with connect() as ss:
        request = WorkspaceQueryRequest(
            query=question,
            mode=Mode.RETRIEVAL_ONLY,                   # sources only, no model call
            memory_mode=MemoryMode.STANDARD,            # set explicitly: the default may record memory
        )
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/query   (mode = retrieval_only)
        # Errors: SmartSearchError (HTTP status + server error code).
        body = ss.workplace().query(workspace_id(), request).body
        print(f"mode={body.get('mode')} status={body.get('status')}"
              f" answer={'present' if body.get('answer') is not None else 'none'}")
        print(f"sources={len(body.get('sources', []))}")
        for source in body.get("sources", []):
            print(f"  [{source.get('n')}] {shorten(source.get('title'), 60):<60} score={source.get('score') or 0:.4f}")
            # snippet (the matching text) and source_url (where to open it) are also here.


if __name__ == "__main__":
    run(main)
