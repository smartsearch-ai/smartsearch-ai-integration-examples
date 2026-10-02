package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Filter by any of several values of one field: horror OR animation.
 *
 * <p>{@code Filter.terms(field, v1, v2, ...)} keeps documents whose field equals at least one of
 * the values. It is the natural fit for a multi-select facet ("Genre: [x] Horror [x] Animation").
 * To require a value from each of several fields, add one {@code filter(...)} per field (see
 * {@code CombineFiltersWithAnyOf}).
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByAnyOfSeveralValues love}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class FilterByAnyOfSeveralValues {

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
                    .responseFields("title", "genres")
                    .filter(Filter.terms("genres.name", "Horror", "Animation")) // Horror OR Animation
                    .size(5)
                    .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(
                    ss
                        .search()
                        .search(SmartSearchConnectionConfig.projectId(), query),
                    "title",
                    "genres"
                );
            }
        });
    }
}
