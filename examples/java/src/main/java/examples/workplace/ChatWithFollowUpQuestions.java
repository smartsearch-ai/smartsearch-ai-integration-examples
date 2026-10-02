package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Multi-turn chat: follow-up questions that refer to earlier ones ("when was he born?").
 *
 * <p>A <i>chat session</i> holds the conversation. The first {@code chat} call creates one and
 * returns its {@code session_id}; send that ID with {@code sessionId(...)} on every later turn so
 * the agent reads the question in context. Keep one session per conversation in your app. The
 * session is separate from memory: {@code MemoryMode.STANDARD} still keeps the conversation, it
 * only stops long-term memory.
 *
 * <p>Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent. Run: {@code ./run.sh ChatWithFollowUpQuestions "Who was President Kennedy?" "When was he born?"}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect documents, sources or the final answer for this caller.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class ChatWithFollowUpQuestions {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String workspaceId = SmartSearchConnectionConfig.workspaceId();
            String first =
                args.length > 0 ? args[0] : "Who wrote our travel policy?";
            String followUp =
                args.length > 1 ? args[1] : "When was it last updated?";
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                // Turn 1, no session ID: the server starts a session.
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/chat
                JsonNode turn1 = ss
                    .workplace()
                    .chat(
                        workspaceId,
                        WorkspaceQueryRequest.builder(first)
                            .memoryMode(MemoryMode.STANDARD)
                            .build()
                    )
                    .getBody();
                String sessionId = turn1.path("session_id").asText(null);
                System.out.println("Q1: " + first);
                System.out.println(
                    "A1: " +
                        ExampleRunner.shorten(
                            turn1.path("answer").asText(),
                            250
                        )
                );
                if (sessionId == null) {
                    throw new IllegalStateException(
                        "The server did not return a chat session"
                    );
                }
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/chat   (with session_id)
                JsonNode turn2 = ss
                    .workplace()
                    .chat(
                        workspaceId,
                        WorkspaceQueryRequest.builder(followUp)
                            .sessionId(sessionId)
                            .memoryMode(MemoryMode.STANDARD)
                            .build()
                    )
                    .getBody();
                System.out.println("Q2: " + followUp);
                System.out.println(
                    "A2: " +
                        ExampleRunner.shorten(
                            turn2.path("answer").asText(),
                            250
                        )
                );
                System.out.println(
                    "same session: " +
                        sessionId.equals(turn2.path("session_id").asText())
                );
            }
        });
    }
}
