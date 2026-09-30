package examples;

import co.smartsearchai.SmartSearchAi;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.MemoryMode;

/**
 * W3 - Multi-turn chat: the server creates a session on the first turn; send its ID back so a
 * follow-up question ("and when was he born?") is understood in context.
 *
 * <p>Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent.
 */
public final class W3ChatFollowUp {

    public static void main(String[] args) {
        Console.run(() -> {
            String workspaceId = Config.workspaceId();
            String first = args.length > 0 ? args[0] : "Who wrote our travel policy?";
            String followUp = args.length > 1 ? args[1] : "When was it last updated?";
            try (SmartSearchAi ss = Config.connect()) {
                var turn1 = ss.workplace().chat(workspaceId, WorkspaceQueryRequest.builder(first)
                        .memoryMode(MemoryMode.STANDARD).build()).getBody();
                String sessionId = turn1.path("session_id").asText(null);
                System.out.println("Q1: " + first);
                System.out.println("A1: " + Console.clip(turn1.path("answer").asText(), 250));
                if (sessionId == null) throw new IllegalStateException("The server did not return a chat session");

                var turn2 = ss.workplace().chat(workspaceId, WorkspaceQueryRequest.builder(followUp)
                        .sessionId(sessionId).memoryMode(MemoryMode.STANDARD).build()).getBody();
                System.out.println("Q2: " + followUp);
                System.out.println("A2: " + Console.clip(turn2.path("answer").asText(), 250));
                System.out.println("same session: " + sessionId.equals(turn2.path("session_id").asText()));
            }
        });
    }
}
