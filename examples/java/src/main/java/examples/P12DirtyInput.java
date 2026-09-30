package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;

/**
 * P12 - Dirty input: trim whitespace and strip special characters from what users type.
 *
 * <p>Precondition: as P1.
 */
public final class P12DirtyInput {

    public static void main(String[] args) {
        Console.run(() -> {
            String typed = "  ***the!!! matrix???  ";
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder().q(typed)
                        .trimQuery(true).removeSpecialChars(true)
                        .responseFields("title", "release_date").size(3).build();
                System.out.println("typed: \"" + typed + "\"");
                Console.printHits(ss.search().search(Config.projectId(), query), "title", "release_date");
            }
        });
    }
}
