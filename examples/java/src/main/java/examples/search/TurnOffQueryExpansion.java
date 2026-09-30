package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Turn off query expansion for one request.
 *
 * <p><i>Query expansion</i> widens a query with related words (synonyms and similar terms) so
 * users find documents that use different words for the same thing. Whether it is on is a
 * project setting. {@code disableQueryExpansion()} turns it off for this request only, for
 * example when the user asks for an exact term. There is no per-request way to turn it on.
 *
 * <p>The example runs the same query both ways and prints the scores, so you can see whether
 * expansion changes anything on your project.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh TurnOffQueryExpansion car}
 */
public final class TurnOffQueryExpansion {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "car");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery.Builder base = SearchQuery.builder().q(q).neuralMode(NeuralMode.BM25)
                        .responseFields("title").size(5);

                System.out.println("Project default:");
                // POST {apiUrl}/core/projects/{projectId}/search
                printWithScores(ss.search().search(SmartSearchConnectionConfig.projectId(), base.build()).result());

                System.out.println("Query expansion off:");
                printWithScores(ss.search().search(SmartSearchConnectionConfig.projectId(), base.disableQueryExpansion().build()).result());
            }
        });
    }

    private static void printWithScores(JsonNode result) {
        for (JsonNode hit : result.path("hits").path("hits")) {
            System.out.printf("  %-40s score=%.3f%n", hit.path("_source").path("title").asText(), hit.path("_score").asDouble());
        }
    }
}
