package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

import java.util.List;

/**
 * Multi-search: several searches in one request, for example a results page plus "related"
 * rows, or one row per category on a landing page.
 *
 * <p>{@code multiSearch(projectId, queries)} sends the list in one round trip. The answer's
 * {@code result().responses} is an array with one entry per query, in the same order; each entry
 * has the same {@code hits.hits} shape as a single search.
 *
 * <p><b>Limit the number of hits yourself.</b> Multi-search does not apply each query's
 * {@code size}: every entry comes back with more hits than you asked for. Take the first n of
 * each list, as here.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh RunSeveralSearchesAtOnce}
 */
public final class RunSeveralSearchesAtOnce {

    private static final int SHOW = 3;

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            List<String> texts = args.length > 0 ? List.of(args) : List.of("alien", "titanic", "toy story");
            List<SearchQuery> queries = texts.stream()
                    .map(q -> SearchQuery.builder().q(q).responseFields("title").size(SHOW).build())
                    .toList();
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                // POST {apiUrl}/core/projects/{projectId}/mSearch   (body: a JSON array of the queries)
                JsonNode responses = ss.search().multiSearch(SmartSearchConnectionConfig.projectId(), queries).result().path("responses");
                for (int i = 0; i < responses.size(); i++) {
                    JsonNode hits = responses.get(i).path("hits").path("hits");
                    System.out.println("\"" + texts.get(i) + "\": received " + hits.size() + " hits, showing " + Math.min(SHOW, hits.size()) + ":");
                    for (int h = 0; h < Math.min(SHOW, hits.size()); h++) {
                        System.out.println("  - " + hits.get(h).path("_source").path("title").asText());
                    }
                }
            }
        });
    }
}
