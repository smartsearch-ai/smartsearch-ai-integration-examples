package examples;

import co.smartsearchai.SmartSearchAi;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.MemoryMode;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.Mode;

/**
 * 07 - Search Workplace as your service, with no end user involved.
 *
 * <p>Precondition: your service key is a member of SMARTSEARCH_WORKSPACE_ID.
 *
 * <p>Results reflect the service key's own access, not any user's. Memory belongs to the
 * authenticated identity, so every call made with one service key shares one memory. When you
 * answer on behalf of many people, send {@code MemoryMode.STANDARD} (nothing stored or recalled),
 * or act as each user instead (examples 04 and 05).
 */
public final class Ex07ServiceSearch {

    public static void main(String[] args) {
        Console.run(() -> {
            String query = Config.queryText(args, "What is our travel policy?");
            String workspaceId = Config.workspaceId();
            try (SmartSearchAi ss = Config.connect()) {
                Workplaces.printDocuments(ss.workplace().search(workspaceId, WorkspaceQueryRequest.builder(query).build()).getBody());

                var answer = ss.workplace().query(workspaceId, WorkspaceQueryRequest.builder(query)
                        .mode(Mode.ANSWER).memoryMode(MemoryMode.STANDARD).build()).getBody();
                Workplaces.printAnswer(answer);
                System.out.println("memory=" + answer.path("memory"));
            }
        });
    }
}
