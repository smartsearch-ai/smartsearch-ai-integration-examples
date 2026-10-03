package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Keep only documents that have a value in a field: films with a tagline.
 *
 * <p>{@code Filter.exists(field)} is useful when a result card needs a field to display (an
 * image, a price, a summary), or to hide incomplete records. To find documents WITHOUT the
 * field, pass the same filter to {@code exclude(...)} (see {@code ExcludeResults}).
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterWhereFieldExists love}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class FilterWhereFieldExists {

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
                    .responseFields("title", "tagline")
                    .filter(Filter.exists("tagline")) // tagline has a value
                    .size(3)
                    .build();
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
