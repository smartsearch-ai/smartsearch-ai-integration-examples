package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;

/**
 * P7 - Spelling correction: a misspelled query still finds the right items.
 *
 * <p>Precondition: as P1, with spelling correction enabled for the project.
 */
public final class P07SpellingCorrection {

    public static void main(String[] args) {
        Console.run(() -> {
            String misspelled = Config.queryText(args, "terminater");
            try (SmartSearchAi ss = Config.connect()) {
                for (boolean correct : new boolean[]{false, true}) {
                    System.out.println("autoCorrect=" + correct + " for \"" + misspelled + "\":");
                    Console.printHits(ss.search().search(Config.projectId(), SearchQuery.builder().q(misspelled)
                            .autoCorrect(correct).neuralMode(NeuralMode.BM25)
                            .responseFields("title").size(3).build()), "title");
                }
            }
        });
    }
}
