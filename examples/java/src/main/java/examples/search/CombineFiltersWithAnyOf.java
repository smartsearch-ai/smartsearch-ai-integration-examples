package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Combine filters: AND across {@code filter(...)} calls, OR inside {@code anyOf(...)}, NOT with
 * {@code exclude(...)}.
 *
 * <p>This request reads: English AND rated at least 7 AND (science fiction or adventure OR has a
 * tagline) AND NOT rumored. The three builder methods map to the three parts of SSPL filters:
 * <ul>
 *   <li>{@code filter(f)}: every one must match ({@code filters.all});</li>
 *   <li>{@code anyOf(f1, f2, ...)}: at least one of them must match (an {@code any} group);</li>
 *   <li>{@code exclude(f)}: none may match ({@code filters.not}).</li>
 * </ul>
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh CombineFiltersWithAnyOf space}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class CombineFiltersWithAnyOf {

    private static final String[] FIELDS = {
        "title",
        "original_language",
        "vote_average",
        "genres",
    };

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                    .q(ExampleRunner.queryText(args, "space"))
                    .responseFields(FIELDS)
                    .filter(Filter.term("original_language", "en")) // AND English
                    .filter(Filter.range("vote_average", 7.0, null, null, null)) // AND rated >= 7
                    .anyOf(
                        Filter.terms(
                            "genres.name",
                            "Science Fiction",
                            "Adventure"
                        ),
                        Filter.exists("tagline")
                    ) // AND (genre OR tagline)
                    .exclude(Filter.term("status", "Rumored")) // AND NOT rumored
                    .size(5)
                    .build();
                System.out.println("request: " + query.toJson()); // see how the three parts are written
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(SmartSearchConnectionConfig.projectId(), query),
                    FIELDS
                );
            }
        });
    }
}
