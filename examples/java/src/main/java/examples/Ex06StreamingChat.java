package examples;

import co.smartsearchai.SmartSearchAi;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.MemoryMode;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceStream;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceStreamEvent;

import java.util.Optional;

/**
 * 06 - Streaming chat: print the answer as it is generated (server-sent events).
 *
 * <p>Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, and the workspace
 * has an answering agent. The same call works on a {@code UserWorkplace} (examples 04 and 05).
 *
 * <p>Events arrive in order: {@code run.started} (carries the chat session), {@code sources.selected},
 * many {@code answer.delta}, {@code answer.completed}, then {@code run.completed} with the final
 * result. The final result is authoritative over the deltas.
 */
public final class Ex06StreamingChat {

    public static void main(String[] args) {
        Console.run(() -> {
            String question = Config.queryText(args, "Summarize our travel policy in three bullet points.");
            try (SmartSearchAi ss = Config.connect();
                 WorkspaceStream stream = ss.workplace().streamChat(Config.workspaceId(),
                         WorkspaceQueryRequest.builder(question).memoryMode(MemoryMode.STANDARD).build())) {

                int deltas = 0;
                Optional<WorkspaceStreamEvent> next;
                while ((next = stream.next()).isPresent()) {           // empty once the run completes
                    WorkspaceStreamEvent event = next.get();
                    switch (event.getEvent()) {
                        case "sources.selected" -> System.out.println("[sources selected]");
                        case "answer.delta" -> {
                            System.out.print(event.getPayload().path("text").asText());
                            System.out.flush();
                            deltas++;
                        }
                        default -> { }                                  // run.started, answer.completed, heartbeats
                    }
                }
                System.out.println();
                System.out.println("[done] deltas=" + deltas + " session=" + (stream.getSessionId() != null ? "yes" : "no")
                        + " final sources=" + stream.getResult().map(r -> r.getBody().path("sources").size()).orElse(0));
            }
        });
    }
}
