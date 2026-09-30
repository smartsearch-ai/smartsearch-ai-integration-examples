package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Tune reranking: how many candidates the model re-scores, and a minimum score to keep a hit.
 *
 * <p>{@code rerankFirstStageSize(n)} (greater than 0) is how many first-pass candidates the
 * reranker reads. More candidates give the model more chances to find the best answers, and take
 * longer. {@code rerankMinScore(s)} drops hits the model scores below {@code s}, which trims
 * weak answers from the end of the list. Whether any hit falls below a given value depends on the
 * model and your data, so pick the threshold by testing on real queries.
 *
 * <p>Precondition: as {@code RerankResults}. Run: {@code ./run.sh TuneReranking "wizard school"}
 */
public final class TuneReranking {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "wizard school");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (int firstStage : new int[]{10, 50}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .rerank(true)
                            .rerankFirstStageSize(firstStage)   // candidates the model re-scores
                            .responseFields("title")
                            .size(5)
                            .build();
                    System.out.println("rerankFirstStageSize=" + firstStage + ":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }

                SearchQuery withMinimum = SearchQuery.builder()
                        .q(q)
                        .rerank(true)
                        .rerankFirstStageSize(30)
                        .rerankMinScore(0.5)                    // drop hits the model scores below 0.5
                        .responseFields("title")
                        .size(10)
                        .build();
                System.out.println("rerankMinScore=0.5, size=10:");
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), withMinimum), "title");
            }
        });
    }
}
