package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;

/**
 * P11 - Precision levels: 1 (broad, more recall) to 11 (strict, every word matters).
 *
 * <p>Precondition: as P1, on a project that uses a precision search template.
 */
public final class P11Precision {

    public static void main(String[] args) {
        Console.run(() -> {
            String q = Config.queryText(args, "the dark knight rises");
            try (SmartSearchAi ss = Config.connect()) {
                for (int precision : new int[]{2, 6, 10}) {
                    System.out.println("precision=" + precision + ":");
                    Console.printHits(ss.search().search(Config.projectId(), SearchQuery.builder().q(q)
                            .precision(precision).neuralMode(NeuralMode.BM25)
                            .responseFields("title").size(3).build()), "title");
                }
            }
        });
    }
}
