package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Keep only documents that have a value in a field: films with a tagline.
 *
 * <p>{@code Filter.exists(field)} is useful when a result card needs a field to display (an
 * image, a price, a summary), or to hide incomplete records. To find documents WITHOUT the
 * field, pass the same filter to {@code exclude(...)} (see {@code ExcludeResults}).
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterWhereFieldExists love}
 */
public final class FilterWhereFieldExists {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(ExampleRunner.queryText(args, "love"))
                        .responseFields("title", "tagline")
                        .filter(Filter.exists("tagline"))    // tagline has a value
                        .size(3)
                        .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title", "tagline");
            }
        });
    }
}
