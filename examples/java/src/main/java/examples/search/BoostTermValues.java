package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Boost: rank documents with a chosen value higher, without removing the others.
 *
 * <p>A filter removes documents; a boost only moves matching ones up. Typical uses are business
 * rules such as "prefer in-stock items" or "prefer the user's language".
 * {@code termBoost(field, value, weight)} adds {@code weight} worth of score to documents whose
 * field equals the value; a larger weight pushes them further up. You can add several boosts.
 *
 * <p><b>Boosts shape keyword scores.</b> They act on keyword (BM25) ranking. The reranker
 * re-scores the top hits by relevance alone and can undo a boost, so when a business rule must
 * decide the order use keyword ranking and {@code rerank(false)}, as here.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh BoostTermValues love}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class BoostTermValues {

    private static final String[] FIELDS = { "title", "original_language" };

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "love");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery.Builder base = SearchQuery.builder()
                    .q(q)
                    .responseFields(FIELDS)
                    .size(5)
                    .neuralMode(NeuralMode.BM25)
                    .rerank(false); // keep the boosted order

                System.out.println("Without a boost:");
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(
                            SmartSearchConnectionConfig.projectId(),
                            base.build()
                        ),
                    FIELDS
                );

                // A builder can be reused: build() takes a snapshot, so adding the boost now
                // does not change the request built above.
                System.out.println("Boost original_language = fr, weight 5:");
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(
                            SmartSearchConnectionConfig.projectId(),
                            base.termBoost("original_language", "fr", 5).build()
                        ),
                    FIELDS
                );
            }
        });
    }
}
