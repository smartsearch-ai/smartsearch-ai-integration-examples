package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Mode;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

import java.util.List;
import java.util.Map;

/**
 * Answer from part of a workspace only: one source, and documents whose title contains a word.
 *
 * <p>A query takes the same scoping as a search ({@code FilterWorkplaceBySourceAndTitle}):
 * {@code addSourceId(id)} limits it to a source, and {@code filters({"all": [clause, ...]})}
 * adds conditions every document must meet. A clause is {@code search_type} ({@code term},
 * {@code terms}, {@code match} or {@code match_phrase}), {@code field} and {@code value}, on
 * {@code source_type}, {@code title}, {@code content}, {@code snippet}, {@code source_url},
 * {@code author} or your own {@code metadata.*} fields. The answer is then written only from
 * documents that pass. Access-control fields cannot be filtered on; the SDK rejects them.
 *
 * <p>Each source in the response carries its {@code source_id}, so you can confirm the scoping.
 *
 * <p><b>Precondition.</b> The caller can see SMARTSEARCH_SOURCE_ID in SMARTSEARCH_WORKSPACE_ID,
 * which has an answering agent.
 * Run: {@code ./run.sh QueryWithFilters "What is a normal blood pressure?" pressure}
 * (question, then a word the titles must contain).
 */
public final class QueryWithFilters {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String question = args.length > 0 ? args[0] : "What is our travel policy?";
            String titleWord = args.length > 1 ? args[1] : "policy";
            String sourceId = SmartSearchConnectionConfig.require("SMARTSEARCH_SOURCE_ID");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                WorkspaceQueryRequest request = WorkspaceQueryRequest.builder(question)
                        .mode(Mode.ANSWER)
                        .memoryMode(MemoryMode.STANDARD)
                        .addSourceId(sourceId)                                          // only this source
                        .filters(Map.of("all", List.of(                                 // every clause must match
                                Map.of("search_type", "match", "field", "title", "value", titleWord))))
                        .build();

                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/query   (with source_ids and filters)
                // Errors: IllegalArgumentException before sending for an unsupported clause or field;
                // WorkspaceClientException when the server refuses.
                JsonNode body = ss.workplace().query(SmartSearchConnectionConfig.workspaceId(), request).getBody();

                System.out.println("answer: " + ExampleRunner.shorten(body.path("answer").asText(), 250));
                System.out.println("sources=" + body.path("sources").size());
                for (JsonNode source : body.path("sources")) {
                    System.out.println("  [" + source.path("n").asText() + "] " + ExampleRunner.shorten(source.path("title").asText(), 70)
                            + " (from the chosen source: " + sourceId.equals(source.path("source_id").asText()) + ")");
                }
            }
        });
    }
}
