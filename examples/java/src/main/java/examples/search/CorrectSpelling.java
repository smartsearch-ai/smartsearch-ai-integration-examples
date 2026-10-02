package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Spelling correction: a misspelled query still finds what the user meant.
 *
 * <p>With {@code autoCorrect(true)} the server corrects likely typos before searching. On the
 * Movies sample, "terminater" then finds "The Terminator". Turn it off when users search
 * exact codes or names that look like typos.
 *
 * <p>Precondition: as {@code FirstSearch}, on a project with spelling correction set up (your
 * administrator can tell you). Run: {@code ./run.sh CorrectSpelling terminater}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class CorrectSpelling {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String misspelled = ExampleRunner.queryText(args, "terminater");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (boolean correct : new boolean[] { false, true }) {
                    SearchQuery query = SearchQuery.builder()
                        .q(misspelled)
                        .autoCorrect(correct) // true: fix likely typos first
                        .neuralMode(NeuralMode.BM25) // keyword search shows the effect clearly
                        .responseFields("title")
                        .size(3)
                        .build();
                    System.out.println(
                        "autoCorrect=" +
                            correct +
                            " for \"" +
                            misspelled +
                            "\":"
                    );
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
