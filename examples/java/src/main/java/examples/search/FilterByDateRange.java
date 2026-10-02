package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Filter by a date range: films released in the year 2000.
 *
 * <p>The range filter takes numbers, and a date field accepts the number formats it was set up
 * with in your project. The Movies sample's {@code release_date} accepts whole years, so
 * {@code gte 2000, lt 2001} means "released in 2000". Ask your administrator which formats your
 * date fields accept; a bound the field cannot read makes the server refuse the request.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByDateRange love}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class FilterByDateRange {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                    .q(ExampleRunner.queryText(args, "love"))
                    .responseFields("title", "release_date")
                    .filter(
                        Filter.range("release_date", 2000, null, null, 2001)
                    ) // 2000 <= year < 2001
                    .size(5)
                    .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(SmartSearchConnectionConfig.projectId(), query),
                    "title",
                    "release_date"
                );
            }
        });
    }
}
