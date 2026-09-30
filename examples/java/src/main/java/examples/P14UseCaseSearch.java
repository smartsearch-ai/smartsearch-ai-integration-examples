package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;

/**
 * P14 - Use-case search: search through a named use case (a saved search configuration with its
 * own template, fields and relevance settings) instead of the project defaults.
 *
 * <p>Precondition: SMARTSEARCH_USECASE_ID is a use case of a project your service key is assigned to.
 */
public final class P14UseCaseSearch {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder().q(Config.queryText(args, "star wars"))
                        .responseFields("title", "release_date").size(5).build();
                Console.printHits(ss.search().searchByUsecase(Config.require("SMARTSEARCH_USECASE_ID"), query),
                        "title", "release_date");
            }
        });
    }
}
