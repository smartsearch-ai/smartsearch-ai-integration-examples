package examples;

import co.smartsearchai.SmartSearchAi;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.MemoryMode;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.Mode;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.Options;

/**
 * W2 - Query in two modes: retrieval only (fast, just the evidence), then a generated answer.
 *
 * <p>Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID, which has an
 * answering agent.
 *
 * <p>{@code RETRIEVAL_ONLY} returns the documents an answer would be built from, without calling a
 * language model. {@code ANSWER} adds the answer and the sources it cites.
 */
public final class W2RetrievalThenAnswer {

    public static void main(String[] args) {
        Console.run(() -> {
            String question = Config.queryText(args, "What is our travel policy?");
            String workspaceId = Config.workspaceId();
            try (SmartSearchAi ss = Config.connect()) {
                var retrieval = ss.workplace().query(workspaceId, WorkspaceQueryRequest.builder(question)
                        .mode(Mode.RETRIEVAL_ONLY).build()).getBody();
                System.out.println("Retrieval only: mode=" + retrieval.path("mode").asText()
                        + " sources=" + retrieval.path("sources").size() + " answer=" + (retrieval.hasNonNull("answer") ? "present" : "none"));

                long start = System.nanoTime();
                var answer = ss.workplace().query(workspaceId, WorkspaceQueryRequest.builder(question)
                        .mode(Mode.ANSWER)
                        .memoryMode(MemoryMode.STANDARD)
                        .options(Options.builder().includeSources(true).maxContextDocuments(5))
                        .build()).getBody();
                System.out.println("Answer (" + (System.nanoTime() - start) / 1_000_000 + " ms):");
                Workplaces.printAnswer(answer);
            }
        });
    }
}
