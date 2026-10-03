package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import co.smartsearchai.search.SearchResult;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Keyword, semantic and hybrid search: the same question three ways.
 *
 * <p><b>Keyword search (BM25)</b> scores documents by the query words they contain: rare words
 * count more, repeated words count more, long fields count a little less. It is exact and
 * predictable, and misses documents that use different words.
 *
 * <p><b>Semantic search (vector search)</b> turns the query and every document into lists of
 * numbers (<i>embeddings</i>) that capture meaning, and returns the documents closest in meaning,
 * even with no words in common. It needs a project with embeddings.
 *
 * <p><b>Hybrid search</b> runs both and fuses the two ranked lists, so you get exact matches and
 * matches by meaning. Tune the fusion with {@code TuneHybridSearch}.
 *
 * <p>{@code neuralMode(...)} chooses: {@code BM25}, {@code A_KNN} (semantic) or
 * {@code A_KNN_AND_BM25} (hybrid). Without it the project's default is used. The server can fall
 * back to another mode when the project cannot run the one you asked for; it then says so in
 * {@code warning()}, and {@code effectiveNeuralMode()} tells you what actually ran. Always log both.
 *
 * <p>Precondition: as {@code FirstSearch}, on a project with embeddings.
 * Run: {@code ./run.sh KeywordVsSemanticVsHybrid "rebels fight an evil empire in space"}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class KeywordVsSemanticVsHybrid {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(
                args,
                "rebels fight an evil empire in space"
            );
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (NeuralMode mode : new NeuralMode[] {
                    NeuralMode.BM25,
                    NeuralMode.A_KNN,
                    NeuralMode.A_KNN_AND_BM25,
                }) {
                    SearchQuery query = SearchQuery.builder()
                        .q(q)
                        .neuralMode(mode) // which technique to run
                        .responseFields("title")
                        .size(5)
                        .build();
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResult result = ss
                        .search()
                        .search(SmartSearchConnectionConfig.projectId(), query);
                    SearchResultPrinter.requireSuccess(result);
                    System.out.println("requested " + mode + ":");
                    SearchResultPrinter.printHits(result, "title"); // prints mode=<what actually ran> and any warning
                }
            }
        });
    }
}
