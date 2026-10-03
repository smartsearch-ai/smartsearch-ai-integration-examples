package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchException;
import co.smartsearchai.search.SearchQuery;
import co.smartsearchai.search.SearchResult;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Errors and warnings: the three ways a search can go differently from what you asked.
 *
 * <ol>
 *   <li><b>Invalid request, caught before sending.</b> The builder checks every value and throws
 *       {@link IllegalArgumentException} (precision outside 1..11, negative {@code from}, an empty
 *       query, a wildcard in response fields). Nothing reaches the server. Fix the code.</li>
 *   <li><b>Refused by the server.</b> {@link SearchException}: {@code statusCode()} is the HTTP
 *       status (0 when no response arrived: network or timeout), {@code serverMessage()} and
 *       {@code errorMessage()} explain. A network failure may succeed on retry; a 4xx, or a
 *       refusal whose message names a precondition (such as facets on a project with document
 *       security, see {@code FacetCounts}), needs a different request.</li>
 *   <li><b>Answered, but adjusted.</b> The search succeeds, and {@code warning()} says what the
 *       server changed, for example running a different search technique because the project
 *       cannot run the one requested. {@code effectiveNeuralMode()} shows what ran.</li>
 * </ol>
 * Exception messages never contain tokens, secrets or request bodies, so they are safe to log.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh HandleErrorsAndWarnings}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class HandleErrorsAndWarnings {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            // 1. Caught by the builder: no request is sent.
            try {
                SearchQuery.builder().q("love").precision(12).build();
            } catch (IllegalArgumentException e) {
                System.out.println(
                    "1. rejected before sending: " + e.getMessage()
                );
            }

            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                // 2. Refused by the server: this project ID does not exist.
                //    POST {apiUrl}/core/projects/no-such-project/search
                try {
                    SearchResultPrinter.requireSuccess(
                        ss
                            .search()
                            .search(
                                "no-such-project",
                                SearchQuery.builder().q("love").build()
                            )
                    );
                } catch (IllegalStateException e) {
                    System.out.println(
                        "2. refused by Core: application failure"
                    );
                } catch (SearchException e) {
                    System.out.println(
                        "2. refused: HTTP " +
                            e.statusCode() +
                            ", message=" +
                            e.serverMessage()
                    );
                }

                // 3. Answered with a warning: ask for a search technique the project may not
                //    support and check what actually ran.
                //    POST {apiUrl}/core/projects/{projectId}/search
                SearchResult result = ss
                    .search()
                    .search(
                        SmartSearchConnectionConfig.projectId(),
                        SearchQuery.builder()
                            .q("love")
                            .neuralMode(NeuralMode.EXACT_AND_BM25_FUSED)
                            .responseFields("title")
                            .size(1)
                            .build()
                    );
                SearchResultPrinter.requireSuccess(result);
                System.out.println(
                    "3. requested " +
                        NeuralMode.EXACT_AND_BM25_FUSED +
                        ", ran " +
                        result.effectiveNeuralMode()
                );
                System.out.println(
                    "   warning: " +
                        (result.warning() != null ? result.warning() : "none")
                );
            }
        });
    }
}
