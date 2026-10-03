package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Precision: how strictly the query words must match.
 *
 * <p>{@code precision(level)} runs from 1 (broad: documents matching some of the words qualify,
 * more results) to 11 (strict: documents must match the query closely, fewer and more exact
 * results). Use a low level for exploratory search boxes and a high level when users type exact
 * titles or product names. Values outside 1..11 are rejected by the builder before sending.
 *
 * <p>Precondition: as {@code FirstSearch}, on a project that uses a precision search template
 * (your administrator can tell you). Run: {@code ./run.sh PrecisionLevels "dark knight rises"}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class PrecisionLevels {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "dark knight rises");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (int precision : new int[] { 1, 6, 11 }) {
                    SearchQuery query = SearchQuery.builder()
                        .q(q)
                        .precision(precision) // 1 = broad ... 11 = strict
                        .neuralMode(NeuralMode.BM25) // precision applies to keyword matching
                        .responseFields("title")
                        .size(3)
                        .build();
                    System.out.println("precision=" + precision + ":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(
                        ss
                            .search()
                            .search(
                                SmartSearchConnectionConfig.projectId(),
                                query
                            ),
                        "title"
                    );
                }
            }
        });
    }
}
