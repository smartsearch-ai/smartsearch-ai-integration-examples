package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Spelling correction: a misspelled query still finds what the user meant.
 *
 * <p>With {@code autoCorrect(true)} the server corrects likely typos before searching. On the
 * Movies sample, "terminater" then finds "The Terminator". Turn it off when users search
 * exact codes or names that look like typos.
 *
 * <p>Precondition: as {@code FirstSearch}, on a project with spelling correction set up (your
 * administrator can tell you). Run: {@code ./run.sh CorrectSpelling terminater}
 */
public final class CorrectSpelling {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String misspelled = ExampleRunner.queryText(args, "terminater");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (boolean correct : new boolean[]{false, true}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(misspelled)
                            .autoCorrect(correct)               // true: fix likely typos first
                            .neuralMode(NeuralMode.BM25)        // keyword search shows the effect clearly
                            .responseFields("title")
                            .size(3)
                            .build();
                    System.out.println("autoCorrect=" + correct + " for \"" + misspelled + "\":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }
            }
        });
    }
}
