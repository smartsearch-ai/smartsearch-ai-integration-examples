package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import co.smartsearchai.search.SortOrder;

/**
 * P1 - Keyword search with paging, searched fields, returned fields and sorting.
 *
 * <p>A field sort is exact with keyword (BM25) ranking. A reranker re-scores the top hits by
 * relevance, which would override the sort order and the page offset, so this example turns it
 * off for the request (see P10). Hybrid and semantic modes rank by relevance first.
 *
 * <p>Precondition: SMARTSEARCH_PROJECT_ID is a project your service key is assigned to. The
 * project examples use the Movies sample dataset (fields title, overview, tagline, genres,
 * release_date, vote_average, original_language, status); change the field names for your data.
 */
public final class P01BasicSearch {

    public static void main(String[] args) {
        Console.run(() -> {
            String q = Config.queryText(args, "star wars");
            try (SmartSearchAi ss = Config.connect()) {
                for (int page = 0; page < 2; page++) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .fields("title", "overview")                         // where q is matched
                            .responseFields("title", "release_date", "vote_average") // what each hit returns
                            .neuralMode(NeuralMode.BM25)                          // keyword ranking: sorts apply across pages
                            .rerank(false)                                        // reranking reorders by relevance: off to sort or page
                            .sort("vote_average", SortOrder.DESC)
                            .from(page * 3).size(3)                               // page = 3 hits
                            .build();
                    System.out.println("Page " + (page + 1) + ":");
                    Console.printHits(ss.search().search(Config.projectId(), query), "title", "release_date", "vote_average");
                }
            }
        });
    }
}
