package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Clean up what users type: surrounding spaces and stray symbols.
 *
 * <p>{@code trimQuery(true)} asks the server to remove leading and trailing whitespace;
 * {@code removeSpecialChars(true)} asks it to remove special characters (symbols such as {@code * ! ?})
 * before searching. Turn both on for search
 * boxes that take free text from users. Leave special characters in when users search for
 * codes that contain them (for example "C++" or "AT&amp;T"). The builder itself rejects an empty
 * or all-blank query with an {@link IllegalArgumentException}.
 *
 * <p>This example sends the same messy text both ways so you can compare the results on your
 * own data.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh CleanUpUserInput}
 */
public final class CleanUpUserInput {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String typed = ExampleRunner.queryText(args, "  ***the!!! matrix???  ");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (boolean clean : new boolean[]{false, true}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(typed)
                            .trimQuery(clean)                   // remove surrounding whitespace
                            .removeSpecialChars(clean)          // remove special characters
                            .neuralMode(NeuralMode.BM25)
                            .responseFields("title")
                            .size(3)
                            .build();
                    System.out.println("clean-up=" + clean + " for \"" + typed + "\":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }
            }
        });
    }
}
