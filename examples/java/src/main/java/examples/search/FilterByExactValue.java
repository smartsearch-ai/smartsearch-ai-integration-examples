package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Filter by an exact value: only French films, only comedies.
 *
 * <p><b>Filters versus the query.</b> The query ({@code q}) decides how well a document matches
 * and so its rank. A filter is a yes/no condition: documents that fail it are removed, and the
 * filter does not change the scores of the rest. Use filters for choices the user makes in the
 * interface (a language picker, a category menu).
 *
 * <p>{@code Filter.term(field, value)} keeps documents whose field equals the value exactly
 * (case and spelling matter). For a list of objects, filter on the object's property: in the
 * Movies data {@code genres} is a list like {@code [{"id":35,"name":"Comedy"}]}, so the field is
 * {@code genres.name}. Filtering on {@code genres} itself returns no hits and no error.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByExactValue love}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class FilterByExactValue {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "love");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery french = SearchQuery.builder()
                    .q(q)
                    .responseFields("title", "original_language")
                    .filter(Filter.term("original_language", "fr")) // must equal "fr"
                    .size(3)
                    .build();
                System.out.println("original_language = fr:");
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(
                            SmartSearchConnectionConfig.projectId(),
                            french
                        ),
                    "title",
                    "original_language"
                );

                SearchQuery comedies = SearchQuery.builder()
                    .q(q)
                    .responseFields("title", "genres")
                    .filter(Filter.term("genres.name", "Comedy")) // a property inside a list of objects
                    .size(3)
                    .build();
                System.out.println("genres.name = Comedy:");
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(
                            SmartSearchConnectionConfig.projectId(),
                            comedies
                        ),
                    "title",
                    "genres"
                );
            }
        });
    }
}
