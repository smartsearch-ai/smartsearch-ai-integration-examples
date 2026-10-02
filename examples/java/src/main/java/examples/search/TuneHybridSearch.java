package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Combination;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.Normalization;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Tune hybrid search: how the keyword list and the semantic list are fused.
 *
 * <p>Hybrid search produces two ranked lists and merges them. There are two ways to merge,
 * chosen with {@code normalization(normalization, combination)}:
 * <ul>
 *   <li><b>By rank</b>: {@code (RANK, RRF)}, reciprocal rank fusion. Each document scores
 *       1 / (k + its position) in each list, and the scores add up. Robust, needs no score
 *       tuning. {@code rankConstant(k)} (at least 1) sets k: a larger k flattens the difference
 *       between top and lower positions.</li>
 *   <li><b>By score</b>: a normalization ({@code MIN_MAX}, {@code L2}, {@code Z_SCORE},
 *       {@code DISTRIBUTION_BASED}) that brings both lists' scores to a common scale, and a mean
 *       ({@code ARITHMETIC_MEAN}, {@code GEOMETRIC_MEAN}, {@code HARMONIC_MEAN}) that combines them.</li>
 * </ul>
 * RRF works only with RANK, and RANK only with RRF; the builder rejects other pairings with an
 * {@link IllegalArgumentException} before sending.
 *
 * <p>{@code rankWindow(n)} (at least 10) is how many candidates each list contributes before
 * fusion: larger finds more, costs more.
 *
 * <p>Precondition: as {@code KeywordVsSemanticVsHybrid}. Run: {@code ./run.sh TuneHybridSearch}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class TuneHybridSearch {

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
                SearchQuery byRank = SearchQuery.builder()
                    .q(q)
                    .neuralMode(NeuralMode.A_KNN_AND_BM25)
                    .normalization(Normalization.RANK, Combination.RRF) // fuse by position
                    .rankWindow(50) // 50 candidates per list
                    .rankConstant(60) // RRF constant k
                    .responseFields("title")
                    .size(5)
                    .build();
                System.out.println("Fuse by rank (RANK + RRF):");
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(
                            SmartSearchConnectionConfig.projectId(),
                            byRank
                        ),
                    "title"
                );

                SearchQuery byScore = SearchQuery.builder()
                    .q(q)
                    .neuralMode(NeuralMode.A_KNN_AND_BM25)
                    .normalization(
                        Normalization.MIN_MAX,
                        Combination.ARITHMETIC_MEAN
                    ) // fuse by score
                    .responseFields("title")
                    .size(5)
                    .build();
                System.out.println(
                    "Fuse by score (MIN_MAX + ARITHMETIC_MEAN):"
                );
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(
                            SmartSearchConnectionConfig.projectId(),
                            byScore
                        ),
                    "title"
                );
            }
        });
    }
}
