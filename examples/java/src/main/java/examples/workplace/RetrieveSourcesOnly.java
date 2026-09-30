package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Mode;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Get the evidence for a question without an answer: the documents an answer would be built from.
 *
 * <p>{@code query} in {@code RETRIEVAL_ONLY} mode runs the same document selection as an answer
 * but calls no language model. Use it when you show the sources in your own interface, or pass
 * them to your own language model. It is faster than an answer and has no model cost.
 *
 * <p>Compared with {@code search}: search returns a results list for a query; retrieval-only
 * returns the evidence Workplace would give its answering agent for a question, with the same
 * fields as the sources of an answer.
 *
 * <p><b>Response</b>: {@code run_id}, {@code mode} = "retrieval_only", {@code status},
 * {@code sources} (title, snippet, source_url, source_id, document_id, score, n), an empty
 * {@code citations} list, and no {@code answer}.
 *
 * <p><b>Precondition.</b> Your service key is a member of SMARTSEARCH_WORKSPACE_ID.
 * Run: {@code ./run.sh RetrieveSourcesOnly "What is a normal blood pressure?"}
 */
public final class RetrieveSourcesOnly {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String question = ExampleRunner.queryText(args, "What is our travel policy?");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                WorkspaceQueryRequest request = WorkspaceQueryRequest.builder(question)
                        .mode(Mode.RETRIEVAL_ONLY)                  // sources only, no model call
                        .memoryMode(MemoryMode.STANDARD)            // set explicitly: the default may record memory
                        .build();

                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/query   (mode = retrieval_only)
                // Errors: WorkspaceClientException (HTTP status + server error code).
                JsonNode body = ss.workplace().query(SmartSearchConnectionConfig.workspaceId(), request).getBody();

                System.out.println("mode=" + body.path("mode").asText() + " status=" + body.path("status").asText()
                        + " answer=" + (body.hasNonNull("answer") ? "present" : "none"));
                System.out.println("sources=" + body.path("sources").size());
                for (JsonNode source : body.path("sources")) {
                    System.out.printf("  [%s] %-60s score=%.4f%n", source.path("n").asText(),
                            ExampleRunner.shorten(source.path("title").asText(), 60), source.path("score").asDouble());
                    // snippet (the matching text) and source_url (where to open it) are also here.
                }
            }
        });
    }
}
