package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Wildcard and prefix search: {@code avat*} finds avatar, avatars, and so on.
 *
 * <p>With {@code wildcard(true)} a {@code *} in the query stands for any characters. It suits
 * part numbers, codes and search-as-you-type prefixes. Wildcards are a keyword feature: use
 * keyword ranking ({@code NeuralMode.BM25}), as here.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh WildcardAndPrefixSearch "avat*"}
 */
public final class WildcardAndPrefixSearch {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String pattern = ExampleRunner.queryText(args, "avat*");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (boolean wildcard : new boolean[]{true, false}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(pattern)
                            .wildcard(wildcard)                 // true: '*' matches any characters
                            .neuralMode(NeuralMode.BM25)
                            .responseFields("title")
                            .size(5)
                            .build();
                    System.out.println("wildcard=" + wildcard + " for \"" + pattern + "\":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }
            }
        });
    }
}
