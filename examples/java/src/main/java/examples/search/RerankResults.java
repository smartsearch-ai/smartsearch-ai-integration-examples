package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Reranking on, off, and the project default.
 *
 * <p><i>Reranking</i> is a second pass: the search first finds candidates quickly, then an AI
 * model (a cross-encoder, which reads the query and each document together) re-scores the top
 * candidates and reorders them by how well they answer the query. It usually improves the first
 * results, at the cost of some time.
 *
 * <p>{@code rerank(true)} forces it on, {@code rerank(false)} forces it off, and
 * {@code rerank(null)} (or not calling it) uses the project's setting.
 *
 * <p>Turn reranking off for sorted lists and paged lists: it reorders by relevance, which
 * overrides a sort, and it does not apply the {@code from} offset (see {@code SortResults} and
 * {@code PageThroughResults}).
 *
 * <p>Precondition: as {@code FirstSearch}, on a project with a reranker set up.
 * Run: {@code ./run.sh RerankResults "wizard school"}
 */
public final class RerankResults {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "wizard school");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (Boolean rerank : new Boolean[]{Boolean.TRUE, Boolean.FALSE, null}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .rerank(rerank)                     // true / false / null = project default
                            .responseFields("title")
                            .size(5)
                            .build();
                    System.out.println("rerank=" + (rerank == null ? "project default" : rerank) + ":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }
            }
        });
    }
}
