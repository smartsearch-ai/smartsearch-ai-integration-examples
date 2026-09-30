package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Highlighting: show the user WHY a result matched, with the query words marked.
 *
 * <p>With {@code highlight(true)} each hit gets a {@code highlight} object: for each matching
 * field, a list of short text fragments in which the matched words are wrapped in
 * {@code <em>...</em>}. The keys are internal names for the analysed versions of your fields;
 * you do not need them, only the fragments. The same field can appear under more than one key,
 * so de-duplicate the fragments.
 *
 * <p>The fragments are HTML-escaped text plus {@code <em>} tags. Render them as HTML only after
 * checking that {@code <em>} is the only markup, or replace the tags with your own styling.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh HighlightMatches princess}
 */
public final class HighlightMatches {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(ExampleRunner.queryText(args, "princess"))
                        .responseFields("title")
                        .highlight(true)                    // add matching fragments to each hit
                        .size(3)
                        .build();
                // POST {apiUrl}/core/projects/{projectId}/search
                JsonNode hits = ss.search().search(SmartSearchConnectionConfig.projectId(), query).result().path("hits").path("hits");
                for (JsonNode hit : hits) {
                    System.out.println("- " + hit.path("_source").path("title").asText());
                    // highlight = { "<field key>": ["fragment with <em>word</em>", ...], ... }
                    Set<String> fragments = new LinkedHashSet<>();
                    hit.path("highlight").forEach(field -> field.forEach(fragment -> {
                        if (fragment.asText().contains("<em>")) fragments.add(ExampleRunner.shorten(fragment.asText(), 120));
                    }));
                    fragments.stream().limit(3).forEach(f -> System.out.println("    " + f));
                }
            }
        });
    }
}
