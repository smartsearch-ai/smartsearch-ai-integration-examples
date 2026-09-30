package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * P3 - Facets (terms and cardinality aggregations) and drill-down.
 *
 * <p>Precondition: as P1, on a project WITHOUT document security. Projects with document
 * security refuse aggregations (HTTP 503 "Canonical retrieval denied"), because counts could
 * reveal documents the caller cannot see.
 */
public final class P03Facets {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                String q = Config.queryText(args, "war");
                SearchQuery faceted = SearchQuery.builder().q(q).size(1).responseFields("title")
                        .termsAgg("languages", "original_language", 5)          // top 5 buckets
                        .cardinalityAgg("distinct_languages", "original_language")
                        .build();
                JsonNode aggs = ss.search().search(Config.projectId(), faceted).result().path("aggregations");

                System.out.println("distinct languages: " + aggs.path("distinct_languages").path("value").asText("?"));
                JsonNode buckets = aggs.path("languages").path("buckets");
                buckets.forEach(b -> System.out.println("  " + b.path("key").asText() + " (" + b.path("doc_count").asText() + ")"));
                if (buckets.isEmpty()) return;

                // Drill down: the user clicks the first facet value.
                String language = buckets.get(0).path("key").asText();
                System.out.println("Drill-down original_language=" + language + ":");
                Console.printHits(ss.search().search(Config.projectId(), SearchQuery.builder().q(q).size(3)
                        .responseFields("title", "original_language")
                        .filter(Filter.term("original_language", language)).build()), "title", "original_language");
            }
        });
    }
}
