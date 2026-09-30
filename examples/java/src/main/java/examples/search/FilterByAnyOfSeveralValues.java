package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Filter by any of several values of one field: horror OR animation.
 *
 * <p>{@code Filter.terms(field, v1, v2, ...)} keeps documents whose field equals at least one of
 * the values. It is the natural fit for a multi-select facet ("Genre: [x] Horror [x] Animation").
 * To require a value from each of several fields, add one {@code filter(...)} per field (see
 * {@code CombineFiltersWithAnyOf}).
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByAnyOfSeveralValues love}
 */
public final class FilterByAnyOfSeveralValues {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(ExampleRunner.queryText(args, "love"))
                        .responseFields("title", "genres")
                        .filter(Filter.terms("genres.name", "Horror", "Animation"))   // Horror OR Animation
                        .size(5)
                        .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title", "genres");
            }
        });
    }
}
