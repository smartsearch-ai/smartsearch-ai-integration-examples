package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;

/**
 * P9 - Pure semantic (vector) search: finds items by meaning, even with no shared words.
 *
 * <p>Precondition: as P1, on a project with embeddings.
 */
public final class P09Semantic {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(Config.queryText(args, "a toy cowboy afraid of being replaced"))
                        .neuralMode(NeuralMode.A_KNN)
                        .neuralMatches(10, 100)   // return the top 10 of 100 nearest candidates
                        .responseFields("title", "release_date").size(5)
                        .build();
                Console.printHits(ss.search().search(Config.projectId(), query), "title", "release_date");
            }
        });
    }
}
