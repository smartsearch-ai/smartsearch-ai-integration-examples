package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Control semantic matching: how many close-in-meaning documents to consider and return.
 *
 * <p>Semantic search looks at a set of candidate documents near the query in meaning and keeps
 * the closest. {@code neuralMatches(top, total)} sets both numbers: consider {@code total}
 * candidates, keep the {@code top} best (1 &lt;= top &lt;= total &lt;= 10000). A larger total
 * finds better matches at some cost in speed.
 *
 * <p>{@code top} caps what semantic search returns even when {@code size} is larger: here
 * {@code size(10)} with {@code top = 3} gives at most 3 hits.
 *
 * <p>Precondition: as {@code KeywordVsSemanticVsHybrid}.
 * Run: {@code ./run.sh LimitSemanticMatches "a toy cowboy afraid of being replaced"}
 */
public final class LimitSemanticMatches {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "a toy cowboy afraid of being replaced");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (int top : new int[]{3, 10}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .neuralMode(NeuralMode.A_KNN)       // semantic only
                            .neuralMatches(top, 100)            // keep the best `top` of 100 candidates
                            .responseFields("title")
                            .size(10)
                            .build();
                    System.out.println("neuralMatches(top=" + top + ", total=100), size=10:");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }
            }
        });
    }
}
