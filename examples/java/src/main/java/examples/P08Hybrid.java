package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Combination;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.Normalization;
import co.smartsearchai.search.SearchQuery;
import co.smartsearchai.search.SearchResult;

/**
 * P8 - Hybrid search: keyword (BM25) and vector results fused with reciprocal rank fusion.
 *
 * <p>Precondition: as P1, on a project with embeddings. Without them the server falls back to
 * keyword search: the response then reports a different {@code effectiveNeuralMode()} and
 * explains why in {@code warning()}. Always check both.
 *
 * <p>Fusion pairings follow the server: RANK + RRF, or a score normalization (MIN_MAX, L2,
 * Z_SCORE, DISTRIBUTION_BASED) with a mean combination. Other pairings fail before sending.
 */
public final class P08Hybrid {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(Config.queryText(args, "rebels fight an evil empire in space"))
                        .neuralMode(NeuralMode.A_KNN_AND_BM25)
                        .normalization(Normalization.RANK, Combination.RRF)
                        .rankWindow(50)          // candidates per side before fusion
                        .rankConstant(60)        // RRF constant k
                        .responseFields("title", "release_date").size(5)
                        .build();
                SearchResult page = ss.search().search(Config.projectId(), query);
                if (!"A_KNN_AND_BM25".equals(page.effectiveNeuralMode())) {
                    System.out.println("Fell back to " + page.effectiveNeuralMode() + ": " + page.warning());
                }
                Console.printHits(page, "title", "release_date");
            }
        });
    }
}
