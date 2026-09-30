package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceResult;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Search Workplace as your service (no end user involved): the documents that match a query.
 *
 * <p><b>Concepts.</b> <i>Workplace</i> is SmartSearch AI's search and answer engine for company
 * knowledge. A <i>workspace</i> groups <i>sources</i> (connected document collections such as a
 * file share or a wiki) and an answering agent that writes answers from them. Three calls:
 * <ul>
 *   <li><b>search</b> (this example): the matching documents, no language model. Use it for a
 *       results list.</li>
 *   <li><b>query</b>: one question, one answer written from the documents, with its sources
 *       ({@code AskAQuestion}), or the sources only ({@code RetrieveSourcesOnly}).</li>
 *   <li><b>chat</b>: like query, in a conversation where follow-up questions build on earlier
 *       turns ({@code ChatWithFollowUpQuestions}).</li>
 * </ul>
 *
 * <p><b>Whose access?</b> Called as your service, results reflect the service key's own access.
 * To respect each person's permissions, act as that user instead ({@code SearchWorkplaceAsUser},
 * {@code SignInWithYourIdentityProvider}).
 *
 * <p><b>Precondition.</b> Your service key is a member of SMARTSEARCH_WORKSPACE_ID.
 * Run: {@code ./run.sh SearchWorkplaceAsService "blood pressure"}
 */
public final class SearchWorkplaceAsService {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String text = ExampleRunner.queryText(args, "travel policy");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                // builder(text) sets the query text; nothing else is required.
                WorkspaceQueryRequest request = WorkspaceQueryRequest.builder(text).build();

                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/search
                // Errors: WorkspaceClientException with the HTTP status (getStatusCode()) and the
                // server's error code (getCode()), for example when the key is not a member.
                WorkspaceResult result = ss.workplace().search(SmartSearchConnectionConfig.workspaceId(), request);

                // getBody() = { "documents": [ { "title", "snippet", "source_url", ... }, ... ] }
                // getRequestId() identifies the call; quote it when reporting a problem.
                System.out.println("HTTP " + result.getStatusCode() + " requestId=" + (result.getRequestId() != null ? "present" : "none"));
                WorkplaceResultPrinter.printDocuments(result.getBody());
            }
        });
    }
}
