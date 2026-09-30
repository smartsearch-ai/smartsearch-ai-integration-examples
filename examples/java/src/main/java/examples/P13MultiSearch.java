package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * P13 - Multi-search: several queries in one round trip (for example a results page plus
 * "related" rows).
 *
 * <p>Precondition: as P1.
 */
public final class P13MultiSearch {

    public static void main(String[] args) {
        Console.run(() -> {
            List<String> queries = List.of("alien", "titanic", "toy story");
            try (SmartSearchAi ss = Config.connect()) {
                JsonNode responses = ss.search().multiSearch(Config.projectId(), queries.stream()
                        .map(q -> SearchQuery.builder().q(q).responseFields("title").size(2).build())
                        .toList()).result().path("responses");
                for (int i = 0; i < responses.size(); i++) {
                    JsonNode hits = responses.get(i).path("hits").path("hits");
                    System.out.println("\"" + queries.get(i) + "\": " + hits.size() + " hits, top 3:");
                    for (int h = 0; h < Math.min(3, hits.size()); h++) {
                        System.out.println("  - " + hits.get(h).path("_source").path("title").asText());
                    }
                }
            }
        });
    }
}
