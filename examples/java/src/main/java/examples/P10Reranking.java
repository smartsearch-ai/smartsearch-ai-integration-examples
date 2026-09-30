package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;

/**
 * P10 - Reranking on, off, and "auto" (the project's default).
 *
 * <p>Precondition: as P1, on a project with a reranker configured. The reranker re-scores the
 * first-stage candidates with a cross-encoder model; {@code rerank(null)} leaves the choice to
 * the project.
 */
public final class P10Reranking {

    public static void main(String[] args) {
        Console.run(() -> {
            String q = Config.queryText(args, "wizard school");
            try (SmartSearchAi ss = Config.connect()) {
                for (Boolean rerank : new Boolean[]{Boolean.TRUE, Boolean.FALSE, null}) {
                    SearchQuery.Builder query = SearchQuery.builder().q(q).rerank(rerank)
                            .responseFields("title").size(5);
                    if (Boolean.TRUE.equals(rerank)) query.rerankFirstStageSize(30); // candidates to re-score
                    System.out.println("rerank=" + (rerank == null ? "auto" : rerank) + ":");
                    Console.printHits(ss.search().search(Config.projectId(), query.build()), "title");
                }
            }
        });
    }
}
