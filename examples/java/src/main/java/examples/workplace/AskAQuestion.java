package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Mode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Options;
import co.smartsearchai.workplace.WorkspaceResult;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Ask a question and get an answer written from your documents, with the sources it used.
 *
 * <p>{@code query} in {@code ANSWER} mode: Workplace finds the best documents in the workspace,
 * the workspace's answering agent reads them and writes an answer, and the response lists the
 * sources and the citations. Use it for a "question box" that answers instead of listing results.
 * For a conversation with follow-up questions use chat instead ({@code ChatWithFollowUpQuestions}).
 *
 * <p><b>Request fields</b> set here:
 * <ul>
 *   <li>{@code builder(question)}: the question, in natural language;</li>
 *   <li>{@code mode(Mode.ANSWER)}: write an answer ({@code RETRIEVAL_ONLY} returns sources only);</li>
 *   <li>{@code memoryMode(MemoryMode.STANDARD)}: store and recall no long-term memory (see
 *       {@code AnswerWithMemory}). Set it explicitly: the server's default may use memory;</li>
 *   <li>{@code Options}: {@code includeSources(true)} returns the documents used;
 *       {@code maxContextDocuments(n)} caps how many documents the agent reads (fewer = faster).</li>
 * </ul>
 *
 * <p><b>Response</b> ({@code getBody()}):
 * <pre>
 * run_id     identifies this answer run; quote it when reporting a problem
 * mode       "answer"
 * status     "COMPLETED" when the answer was produced
 * answer     the answer text; [1], [2] refer to the numbered sources
 * sources    the documents used: title, snippet, source_url, source_id, document_id, score, n (its number)
 * citations  the sources the answer actually cites
 * memory     what happened to memory: {"mode": "standard", "write_status": "not_applicable"}
 * </pre>
 *
 * <p><b>Precondition.</b> Your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent. Answers take seconds; the SDK's default read timeout is 60 seconds.
 * Run: {@code ./run.sh AskAQuestion "What is a normal blood pressure?"}
 */
public final class AskAQuestion {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String question = ExampleRunner.queryText(args, "What is our travel policy?");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                WorkspaceQueryRequest request = WorkspaceQueryRequest.builder(question)
                        .mode(Mode.ANSWER)                          // write an answer
                        .memoryMode(MemoryMode.STANDARD)            // no long-term memory
                        .options(Options.builder()
                                .includeSources(true)               // return the documents used
                                .maxContextDocuments(5))            // the agent reads at most 5
                        .build();

                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/query
                // Errors: WorkspaceClientException (HTTP status + server error code), for example when
                // the key is not a member or the workspace has no answering agent; a timeout when
                // the answer takes longer than the read timeout.
                long start = System.nanoTime();
                WorkspaceResult result = ss.workplace().query(SmartSearchConnectionConfig.workspaceId(), request);
                JsonNode body = result.getBody();

                System.out.println("HTTP " + result.getStatusCode() + " in " + (System.nanoTime() - start) / 1_000_000 + " ms"
                        + ", run_id=" + (body.hasNonNull("run_id") ? "present" : "none")
                        + ", mode=" + body.path("mode").asText() + ", status=" + body.path("status").asText());
                System.out.println("answer: " + ExampleRunner.shorten(body.path("answer").asText(), 300));

                // Show sources by their number n, so [1], [2] in the answer can be matched to them.
                System.out.println("sources=" + body.path("sources").size() + " citations=" + body.path("citations").size());
                for (JsonNode source : body.path("sources")) {
                    System.out.println("  [" + source.path("n").asText() + "] " + ExampleRunner.shorten(source.path("title").asText(), 80));
                }
                System.out.println("memory=" + body.path("memory"));
            }
        });
    }
}
