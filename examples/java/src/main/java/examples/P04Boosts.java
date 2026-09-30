package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;

/**
 * P4 - Business boosts: rank hits with a field value higher without filtering others out.
 *
 * <p>Precondition: as P1.
 */
public final class P04Boosts {

    public static void main(String[] args) {
        Console.run(() -> {
            String q = Config.queryText(args, "love");
            try (SmartSearchAi ss = Config.connect()) {
                System.out.println("Without boost:");
                Console.printHits(ss.search().search(Config.projectId(),
                        SearchQuery.builder().q(q).size(5).responseFields("title", "original_language").build()),
                        "title", "original_language");

                System.out.println("Boost original_language=fr (weight 5):");
                Console.printHits(ss.search().search(Config.projectId(),
                        SearchQuery.builder().q(q).size(5).responseFields("title", "original_language")
                                .termBoost("original_language", "fr", 5).build()),
                        "title", "original_language");
            }
        });
    }
}
