package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Mode;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Memory: STANDARD (no long-term memory) versus AGENTIC (long-term memory for the caller).
 *
 * <p>Workplace can keep a long-term memory of what an identity asked, and use it in later
 * answers, across sessions. Memory belongs to the <b>calling identity</b>: your service key when
 * you call as your service, the user when you act as a user.
 * <ul>
 *   <li>{@code MemoryMode.STANDARD}: nothing is stored or recalled. The response reports
 *       {@code "write_status": "not_applicable"}. Use it when you call as your service on behalf
 *       of many people (they would otherwise share one memory), and for one-off questions.</li>
 *   <li>{@code MemoryMode.AGENTIC}: the question is recorded in the caller's memory; the response
 *       reports {@code "write_status": "queued"}, meaning the memory write was accepted and is
 *       processed in the background. Use it when you act as an individual user who benefits from
 *       personal context.</li>
 * </ul>
 * Without {@code memoryMode(...)} the server's default applies, and it can be AGENTIC: set the
 * mode explicitly on every request. Memory is separate from a chat session (which only holds one
 * conversation, see {@code ChatWithFollowUpQuestions}).
 *
 * <p><b>Precondition.</b> Your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent. Note that the AGENTIC call below records the question in your service key's
 * memory. Run: {@code ./run.sh AnswerWithMemory "What is a normal blood pressure?"}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect documents, sources or the final answer for this caller.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class AnswerWithMemory {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String question = ExampleRunner.queryText(
                args,
                "What is our travel policy?"
            );
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (MemoryMode memory : new MemoryMode[] {
                    MemoryMode.STANDARD,
                    MemoryMode.AGENTIC,
                }) {
                    WorkspaceQueryRequest request =
                        WorkspaceQueryRequest.builder(question)
                            .mode(Mode.ANSWER)
                            .memoryMode(memory) // STANDARD or AGENTIC
                            .build();
                    // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/query
                    JsonNode body = ss
                        .workplace()
                        .query(
                            SmartSearchConnectionConfig.workspaceId(),
                            request
                        )
                        .getBody();
                    // memory = { "mode": what the server used, "write_status": what it did with memory }
                    System.out.println(
                        memory +
                            ": memory=" +
                            body.path("memory") +
                            " answer=" +
                            ExampleRunner.shorten(
                                body.path("answer").asText(),
                                100
                            )
                    );
                }
            }
        });
    }
}
