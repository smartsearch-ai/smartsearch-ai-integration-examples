package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceStream;
import co.smartsearchai.workplace.WorkspaceStreamEvent;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

import java.util.Optional;

/**
 * Streaming chat: show the answer word by word as it is written, instead of waiting for all of it.
 *
 * <p>{@code streamChat} opens a stream of server-sent events. Read them with {@code next()} until
 * it returns empty. Events arrive in this order:
 * <ul>
 *   <li>{@code run.started}: the run began (it carries the chat session);</li>
 *   <li>{@code sources.selected}: the documents the answer will use were chosen;</li>
 *   <li>{@code answer.delta}, many times: the next piece of answer text, in {@code payload.text};</li>
 *   <li>{@code answer.completed}, then {@code run.completed}: done.</li>
 * </ul>
 * After the stream ends, {@code getResult()} holds the complete final response (answer and
 * sources); it is authoritative over the pieces. Always close the stream (try-with-resources):
 * closing it early stops reading and releases the connection. A wait longer than the read
 * timeout fails with {@code WorkspaceClientException} code {@code WORKSPACE_STREAM_TIMEOUT}.
 *
 * <p>Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent. The same call works as a user ({@code UserWorkplace.streamChat}).
 * Run: {@code ./run.sh StreamChatAnswer "What causes high blood pressure? Answer briefly."}
 */
public final class StreamChatAnswer {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String question = ExampleRunner.queryText(args, "Summarize our travel policy in three bullet points.");
            // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/chat
            //      with "options": {"stream": true} and Accept: text/event-stream (the SDK sets both)
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect();
                 WorkspaceStream stream = ss.workplace().streamChat(SmartSearchConnectionConfig.workspaceId(),
                         WorkspaceQueryRequest.builder(question).memoryMode(MemoryMode.STANDARD).build())) {

                int deltas = 0;
                Optional<WorkspaceStreamEvent> next;
                while ((next = stream.next()).isPresent()) {          // blocks until the next event; empty at the end
                    WorkspaceStreamEvent event = next.get();
                    switch (event.getEvent()) {                        // the event name
                        case "sources.selected" -> System.out.println("[sources selected]");
                        case "answer.delta" -> {
                            System.out.print(event.getPayload().path("text").asText());   // the next piece of text
                            System.out.flush();
                            deltas++;
                        }
                        default -> { }                                 // run.started, answer.completed, keep-alives
                    }
                }
                System.out.println();
                System.out.println("[done] deltas=" + deltas
                        + " session=" + (stream.getSessionId() != null ? "yes" : "no")                  // reuse it for follow-ups
                        + " final sources=" + stream.getResult().map(r -> r.getBody().path("sources").size()).orElse(0));
            }
        });
    }
}
