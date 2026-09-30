package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Filter by a numeric range: rating at least 7.5, or between 5 and 6.
 *
 * <p>{@code Filter.range(field, gte, lte, gt, lt)} takes four bounds; pass {@code null} for the
 * ones you do not need, and at least one bound. gte = "at least", lte = "at most", gt = "more
 * than", lt = "less than". Whole numbers stay whole numbers on the wire; decimals are sent as
 * decimals. Use it for prices, ratings, sizes, durations.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByNumericRange love}
 */
public final class FilterByNumericRange {

    private static final String[] FIELDS = {"title", "vote_average"};

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "love");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery wellRated = SearchQuery.builder().q(q).responseFields(FIELDS).size(3)
                        .filter(Filter.range("vote_average", 7.5, null, null, null))   // vote_average >= 7.5
                        .build();
                System.out.println("vote_average >= 7.5:");
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), wellRated), FIELDS);

                SearchQuery between = SearchQuery.builder().q(q).responseFields(FIELDS).size(3)
                        .filter(Filter.range("vote_average", null, null, 5.0, 6.0))    // 5.0 < vote_average < 6.0
                        .build();
                System.out.println("5.0 < vote_average < 6.0:");
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), between), FIELDS);
            }
        });
    }
}
