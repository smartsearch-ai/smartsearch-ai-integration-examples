package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Exclude documents: no dramas, no romances, nothing unreleased.
 *
 * <p>{@code exclude(filter)} removes every document that matches the filter. It accepts every
 * filter type. With {@code Filter.terms}, a document is removed if it has ANY of the values, so
 * {@code exclude(terms("genres.name", "Drama", "Romance"))} drops dramas and romances alike.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh ExcludeResults love}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class ExcludeResults {

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
                    .responseFields("title", "genres", "status")
                    .exclude(Filter.terms("genres.name", "Drama", "Romance")) // neither genre
                    .exclude(Filter.term("status", "Rumored")) // and not unreleased
                    .size(5)
                    .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(SmartSearchConnectionConfig.projectId(), query),
                    "title",
                    "genres",
                    "status"
                );
            }
        });
    }
}
