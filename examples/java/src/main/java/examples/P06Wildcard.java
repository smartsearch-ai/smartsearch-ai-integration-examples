package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;

/**
 * P6 - Wildcard search: {@code avat*} matches avatar, avatars, and so on.
 *
 * <p>Precondition: as P1. Wildcards are a keyword feature, so this example runs BM25 only.
 */
public final class P06Wildcard {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder().q(Config.queryText(args, "avat*"))
                        .wildcard(true).neuralMode(NeuralMode.BM25)
                        .responseFields("title", "release_date").size(5).build();
                Console.printHits(ss.search().search(Config.projectId(), query), "title", "release_date");
            }
        });
    }
}
