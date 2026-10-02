"""Keyword, semantic and hybrid search: the same question three ways.

Keyword search (BM25) scores documents by the query words they contain: rare words count more,
repeated words count more, long fields count a little less. It is exact and predictable, and
misses documents that use different words.

Semantic search (vector search) turns the query and every document into lists of numbers
(*embeddings*) that capture meaning, and returns the documents closest in meaning, even with no
words in common. It needs a project with embeddings.

Hybrid search runs both and fuses the two ranked lists, so you get exact matches and matches by
meaning. Tune the fusion with ``tune_hybrid_search``.

``neural_mode`` chooses: ``BM25``, ``A_KNN`` (semantic) or ``A_KNN_AND_BM25`` (hybrid). Without
it the project's default is used. The server can fall back to another mode when the project
cannot run the one you asked for; it then says so in ``warning``, and ``effective_neural_mode``
tells you what actually ran. Always log both.

Precondition: as ``first_search``, on a project with embeddings.
Run: ./run.sh keyword_vs_semantic_vs_hybrid "rebels fight an evil empire in space"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    q = _common.query_text(args, "rebels fight an evil empire in space")
    with _common.connect() as ss:
        for mode in (
            smartsearch_ai.NeuralMode.BM25,
            smartsearch_ai.NeuralMode.A_KNN,
            smartsearch_ai.NeuralMode.A_KNN_AND_BM25,
        ):
            query = smartsearch_ai.SearchQuery(
                q,
                neural_mode=mode,  # which technique to run
                response_fields=["title"],
                size=5,
            )
            # POST {api_url}/core/projects/{project_id}/search
            result = ss.search().search(_common.project_id(), query)
            print(f"requested {mode.value}:")
            _common.print_hits(
                result, "title"
            )  # prints mode=<what actually ran> and any warning


if __name__ == "__main__":
    _common.run(main)
