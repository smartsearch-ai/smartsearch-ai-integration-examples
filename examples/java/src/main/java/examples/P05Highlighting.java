package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * P5 - Highlighting: return the matching fragments with the query terms marked.
 *
 * <p>Precondition: as P1.
 */
public final class P05Highlighting {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder().q(Config.queryText(args, "princess"))
                        .responseFields("title").highlight(true).size(3).build();
                JsonNode hits = ss.search().search(Config.projectId(), query).result().path("hits").path("hits");
                for (JsonNode hit : hits) {
                    System.out.println("- " + hit.path("_source").path("title").asText());
                    // highlight = { field: [fragment, ...] }; matched terms are wrapped in <em> tags.
                    java.util.Set<String> fragments = new java.util.LinkedHashSet<>();
                    hit.path("highlight").forEach(field -> field.forEach(f -> {
                        if (f.asText().contains("<em>")) fragments.add(Console.clip(f.asText(), 120));
                    }));
                    fragments.stream().limit(3).forEach(f -> System.out.println("    " + f));
                }
            }
        });
    }
}
