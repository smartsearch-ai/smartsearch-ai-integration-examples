package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceClientException;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Mode;
import co.smartsearchai.workplace.WorkspaceStream;
import co.smartsearchai.workplace.WorkspaceStreamEvent;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

import java.util.Optional;

/**
 * Stream an answer: print it as it is written instead of waiting for all of it.
 *
 * <p>{@code streamQuery} sends the same request as {@code query} and returns a
 * {@link WorkspaceStream} of server-sent events. Call {@code next()} until it returns empty.
 * The events, in order:
 * <pre>
 * run.started       the run began                      payload: mode
 * sources.selected  the documents were chosen          payload: sources, source_count
 * answer.delta      the next piece of text (many)      payload: text
 * answer.completed  the text is complete               payload: citation_count
 * run.completed     final result                       payload: run_id, mode, status, answer, sources, citations, memory
 * </pre>
 * The server may also send keep-alive events; ignore names you do not handle. After the stream
 * ends, {@code getResult()} holds the final response. It is authoritative: show the pieces while
 * they arrive, then replace them with the final answer if you store it.
 *
 * <p><b>Errors and timeouts.</b> A failure reported in the stream ({@code run.failed} or
 * {@code error}) and a wait longer than the read timeout (default 60 seconds, code
 * {@code WORKSPACE_STREAM_TIMEOUT}) both throw {@link WorkspaceClientException} from
 * {@code next()}. Always close the stream (try-with-resources): that stops reading and releases the
 * connection, also when you stop early. {@code next()} must be called from one thread at a time.
 *
 * <p><b>Precondition.</b> Your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent. Run: {@code ./run.sh StreamAnAnswer "What causes high blood pressure?"}
 */
public final class StreamAnAnswer {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String question = ExampleRunner.queryText(args, "Summarize our travel policy.");
            WorkspaceQueryRequest request = WorkspaceQueryRequest.builder(question)
                    .mode(Mode.ANSWER)
                    .memoryMode(MemoryMode.STANDARD)
                    .build();

            // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/query
            //      with "options": {"stream": true} and Accept: text/event-stream (the SDK sets both)
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect();
                 WorkspaceStream stream = ss.workplace().streamQuery(SmartSearchConnectionConfig.workspaceId(), request)) {

                int pieces = 0;
                try {
                    Optional<WorkspaceStreamEvent> next;
                    while ((next = stream.next()).isPresent()) {       // waits for the next event; empty at the end
                        WorkspaceStreamEvent event = next.get();
                        JsonNode payload = event.getPayload();
                        switch (event.getEvent()) {
                            case "run.started" -> System.out.println("[run started, mode=" + payload.path("mode").asText() + "]");
                            case "sources.selected" -> System.out.println("[" + payload.path("source_count").asText() + " sources selected]");
                            case "answer.delta" -> {
                                System.out.print(payload.path("text").asText());   // print each piece as it arrives
                                System.out.flush();
                                pieces++;
                            }
                            case "answer.completed" -> System.out.println("\n[answer completed, citations=" + payload.path("citation_count").asText() + "]");
                            case "run.completed" -> System.out.println("[run completed, status=" + payload.path("status").asText() + "]");
                            default -> { }                                  // keep-alives and future event types
                        }
                    }
                } catch (WorkspaceClientException e) {
                    // The run failed or timed out part-way; what was printed so far is incomplete.
                    System.out.println();
                    throw e;
                }

                // The final response, the same shape as query() returns.
                JsonNode result = stream.getResult().map(r -> r.getBody()).orElseThrow();
                System.out.println("pieces=" + pieces + " final answer length=" + result.path("answer").asText().length()
                        + " sources=" + result.path("sources").size() + " memory=" + result.path("memory"));
            }
        });
    }
}
