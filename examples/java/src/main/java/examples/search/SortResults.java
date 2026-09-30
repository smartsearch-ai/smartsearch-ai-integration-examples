package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import co.smartsearchai.search.SortOrder;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Sorting: order matching hits by a field (newest first, best rated first) instead of relevance.
 *
 * <p>{@code sort(field, SortOrder.DESC)} adds one sort key; call it again for a tie-breaker. The
 * field must be sortable in your project (numbers, dates, keyword fields).
 *
 * <p><b>Turn reranking off when sorting.</b> The reranker reorders hits by relevance and would
 * override your sort, so send {@code rerank(false)} and keyword ranking, as here. A sort on a
 * field that does not exist is not reported as an error: check your field names.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh SortResults love}
 */
public final class SortResults {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "love");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (String field : new String[]{"release_date", "vote_average"}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .responseFields("title", "release_date", "vote_average")
                            .neuralMode(NeuralMode.BM25)
                            .rerank(false)                     // required: reranking would reorder by relevance
                            .sort(field, SortOrder.DESC)       // highest / newest first
                            .size(3)
                            .build();
                    System.out.println("Sorted by " + field + " descending:");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title", "release_date", "vote_average");
                }
            }
        });
    }
}
