package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Paging: show results one page at a time.
 *
 * <p>{@code from(n)} skips the first n hits and {@code size(m)} returns the next m. Page p (from
 * zero) of size m is {@code from(p * m).size(m)}.
 *
 * <p><b>Turn reranking off when paging.</b> <i>Reranking</i> re-scores the top hits with an AI
 * model (see {@code RerankResults}). It works on the first page of candidates and does not apply
 * the {@code from} offset, so with reranking on every "page" can show the same hits. For paged
 * lists send {@code rerank(false)}, as here, and keyword ranking for a stable order.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh PageThroughResults love}
 */
public final class PageThroughResults {

    private static final int PAGE_SIZE = 3;

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "love");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (int page = 0; page < 3; page++) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .responseFields("title")
                            .neuralMode(NeuralMode.BM25)   // keyword ranking: a stable order across pages
                            .rerank(false)                 // required for paging, see above
                            .from(page * PAGE_SIZE)        // zero-based offset; must be >= 0
                            .size(PAGE_SIZE)               // hits per page; must be > 0
                            .build();
                    System.out.println("Page " + (page + 1) + " (from=" + page * PAGE_SIZE + "):");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                    // A page with fewer than PAGE_SIZE hits is the last one.
                }
            }
        });
    }
}
