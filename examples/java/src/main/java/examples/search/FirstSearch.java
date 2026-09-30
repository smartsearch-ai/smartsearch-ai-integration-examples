package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import co.smartsearchai.search.SearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Your first project search, and how to read what comes back.
 *
 * <p><b>Concepts.</b> A <i>project</i> is one searchable collection of your documents (a product
 * catalogue, a knowledge base) with its own relevance settings. You search it with <i>SSPL</i>,
 * the SmartSearch search request language: a small JSON request with the query text, the fields
 * to search and return, filters, sorting, paging and search options. You never write the JSON by
 * hand: {@link SearchQuery.Builder} writes it for you and rejects invalid values before anything
 * is sent. The server then picks the search technique (keyword, semantic or both, see
 * {@code KeywordVsSemanticVsHybrid}) from the project's settings and your options.
 *
 * <p><b>Preconditions.</b> SMARTSEARCH_PROJECT_ID is a project your service key is assigned to.
 * The search examples use the Movies sample data set (fields {@code title}, {@code overview},
 * {@code tagline}, {@code genres}, {@code release_date}, {@code vote_average},
 * {@code original_language}, {@code runtime}, {@code status}); change the field names for yours.
 *
 * <p>Run: {@code ./run.sh FirstSearch "star wars"}
 */
public final class FirstSearch {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                // Build the request. q is what the user typed; responseFields names the fields
                // each hit should carry (name each one: wildcards are rejected); size is how many
                // hits to return (default 10).
                SearchQuery query = SearchQuery.builder()
                        .q(ExampleRunner.queryText(args, "star wars"))
                        .responseFields("title", "release_date", "vote_average")
                        .size(5)
                        .build();
                // toJson() shows the exact body the SDK will send. Useful while learning and in logs.
                System.out.println("request: " + query.toJson());

                // POST {apiUrl}/core/projects/{projectId}/search
                // Authenticates with the service key's token. Throws SearchException when the
                // server refuses the request (for example HTTP 404 for an unknown project ID) or
                // when no response arrives (statusCode() == 0). See HandleErrorsAndWarnings.
                SearchResult result = ss.search().search(SmartSearchConnectionConfig.projectId(), query);

                // Envelope fields on every response:
                //   code()                 1 = success
                //   message()              short status text, "Success" on success
                //   searchId()             identifies this search; quote it when reporting a problem
                //   effectiveNeuralMode()  the search technique the server actually ran
                //   warning()              set when the server changed something you asked for
                System.out.println("code=" + result.code() + " message=" + result.message()
                        + " searchId=" + (result.searchId() != null ? "present" : "none")
                        + " mode=" + result.effectiveNeuralMode()
                        + " warning=" + result.warning());

                // result() holds the hits (see SearchResultPrinter for the shape). Each hit's _source carries
                // only the responseFields you asked for.
                JsonNode hits = result.result().path("hits").path("hits");
                for (JsonNode hit : hits) {
                    JsonNode doc = hit.path("_source");
                    System.out.println("  " + doc.path("title").asText()
                            + " (" + doc.path("release_date").asText() + ", rated " + doc.path("vote_average").asText() + ")");
                }
            }
        });
    }
}
