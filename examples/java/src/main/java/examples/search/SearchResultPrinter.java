package examples.search;

import co.smartsearchai.search.SearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;

/**
 * Prints the hits of a project search, one line per hit.
 *
 * <p><i>Example plumbing, not part of the SDK.</i> Copy or replace it in your own code.
 *
 * <p>Shape of {@link SearchResult#result()} for a search:
 * <pre>
 * {
 *   "hits": {
 *     "hits": [                               one entry per returned document, best first
 *       { "_source": { "title": "...", ... },  the fields you asked for with responseFields(...)
 *         "_score": 12.3,                      relevance score (absent when the order is not by score)
 *         "highlight": { ... } },              only when highlight(true)
 *       ...
 *     ]
 *   },
 *   "aggregations": { ... }                   only when you asked for facets
 * }
 * </pre>
 * Count what you received with {@code hits.hits.size()}. The response does not give a reliable
 * total number of matches, so do not build "N results" labels from it.
 */
final class SearchResultPrinter {

    private SearchResultPrinter() {}

    /**
     * Requires application success independently of the HTTP status.
     *
     * @param result Core envelope returned by a search
     * @return the same envelope when code is 1
     * @throws IllegalStateException when Core reports an application failure
     */
    static SearchResult requireSuccess(SearchResult result) {
        if (result.code() != 1) {
            throw new IllegalStateException(
                "Core search reported application failure (code=" +
                    result.code() +
                    ")"
            );
        }
        return result;
    }

    /** One line per hit: the first field bare, the others as {@code name=value}. */
    static void printHits(SearchResult result, String... fields) {
        JsonNode hits = requireSuccess(result)
            .result()
            .path("hits")
            .path("hits");
        System.out.println(
            "hits=" +
                hits.size() +
                " mode=" +
                result.effectiveNeuralMode() +
                (result.warning() != null
                    ? " warning=\"" +
                      ExampleRunner.shorten(result.warning(), 160) +
                      "\""
                    : "")
        );
        int rank = 1;
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            StringBuilder line = new StringBuilder(
                String.format("%2d. ", rank++)
            );
            for (int i = 0; i < fields.length; i++) {
                if (!source.has(fields[i])) {
                    continue;
                }
                if (i > 0) {
                    line.append(" | ").append(fields[i]).append('=');
                }
                line.append(
                    ExampleRunner.shorten(
                        valueAsText(source.path(fields[i])),
                        60
                    )
                );
            }
            System.out.println(line);
        }
    }

    /** A field value as text. Lists of {name: ...} objects (such as genres) print as "A, B". */
    static String valueAsText(JsonNode value) {
        if (!value.isArray()) {
            return value.asText();
        }
        StringBuilder joined = new StringBuilder();
        for (JsonNode item : value) {
            if (joined.length() > 0) {
                joined.append(", ");
            }
            joined.append(
                item.has("name") ? item.path("name").asText() : item.asText()
            );
        }
        return joined.toString();
    }
}
