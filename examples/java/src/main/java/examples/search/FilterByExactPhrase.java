package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Filter on an exact phrase: the words together and in order.
 *
 * <p>{@code Filter.matchPhrase(field, phrase)} keeps documents whose field contains the phrase
 * word for word. {@code Filter.match} with the same words would also accept them scattered
 * through the text. Letter case does not matter.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByExactPhrase}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class FilterByExactPhrase {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String phrase = ExampleRunner.queryText(
                args,
                "a galaxy far, far away"
            );
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                    .q("galaxy")
                    .responseFields("title", "tagline")
                    .filter(Filter.matchPhrase("tagline", phrase)) // tagline contains the phrase
                    .size(3)
                    .build();
                System.out.println("tagline contains \"" + phrase + "\":");
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(SmartSearchConnectionConfig.projectId(), query),
                    "title",
                    "tagline"
                );
            }
        });
    }
}
