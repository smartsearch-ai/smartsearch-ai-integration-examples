package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Facet counts and distinct counts, then drill down.
 *
 * <p><i>Facets</i> are the "Language: English (120), French (14)" lists beside search results.
 * {@code termsAgg(name, field, size)} asks for the {@code size} most common values of a field
 * among the matching documents, each with a count. {@code cardinalityAgg(name, field)} asks how
 * many distinct values there are (an approximate count). The {@code name} you choose is the key
 * under which the answer comes back in {@code result().aggregations}.
 *
 * <p><b>Precondition: a project WITHOUT document security.</b> <i>Document security</i> means
 * each document carries access rules and every caller sees only the documents they are allowed to
 * see. On such projects the server refuses aggregations (HTTP 503 "Canonical retrieval denied"),
 * because counts could reveal documents the caller cannot see. Ask your administrator whether
 * your project uses document security.
 *
 * <p>Run: {@code ./run.sh FacetCounts war}
 */
public final class FacetCounts {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "war");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery faceted = SearchQuery.builder()
                        .q(q)
                        .responseFields("title")
                        .size(1)                                                      // hits are not the point here
                        .termsAgg("languages", "original_language", 5)                // top 5 values with counts
                        .cardinalityAgg("distinct_languages", "original_language")   // how many different values
                        .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                JsonNode aggs = ss.search().search(SmartSearchConnectionConfig.projectId(), faceted).result().path("aggregations");

                // aggregations = { "languages": { "buckets": [ {"key": "en", "doc_count": 120}, ... ] },
                //                  "distinct_languages": { "value": 17 } }
                System.out.println("distinct languages: " + aggs.path("distinct_languages").path("value").asText("?"));
                JsonNode buckets = aggs.path("languages").path("buckets");
                buckets.forEach(b -> System.out.println("  " + b.path("key").asText() + " (" + b.path("doc_count").asText() + ")"));
                if (buckets.isEmpty()) return;

                // Drill down: the user clicks the first facet value, which becomes a filter.
                String language = buckets.get(0).path("key").asText();
                System.out.println("Drill down to original_language=" + language + ":");
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), SearchQuery.builder().q(q).size(3)
                        .responseFields("title", "original_language")
                        .filter(Filter.term("original_language", language)).build()), "title", "original_language");
            }
        });
    }
}
