package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

import java.util.List;
import java.util.Map;

/**
 * Narrow a Workplace search to one source, and filter on a document field.
 *
 * <p>{@code addSourceId(id)} limits the search to one source of the workspace (call it again to
 * add more). {@code filters(...)} adds conditions on document fields, written as
 * {@code {"all": [clause, ...]}}: every clause must match. A clause is a map with
 * {@code search_type} ({@code term}, {@code terms}, {@code match} or {@code match_phrase}),
 * {@code field} and {@code value}. Fields you can filter on: {@code source_type}, {@code title},
 * {@code content}, {@code snippet}, {@code source_url}, {@code author}, and your own fields under
 * {@code metadata.*}.
 *
 * <p>Access-control fields are set by the server and cannot be filtered on: the SDK rejects them
 * before sending, so a caller can never widen their own access.
 *
 * <p>Precondition: the caller can see SMARTSEARCH_SOURCE_ID in SMARTSEARCH_WORKSPACE_ID.
 * Run: {@code ./run.sh FilterWorkplaceBySourceAndTitle "blood pressure" pressure}
 * (query, then a word the title must contain).
 */
public final class FilterWorkplaceBySourceAndTitle {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String query = args.length > 0 ? args[0] : "travel policy";
            String titleWord = args.length > 1 ? args[1] : "policy";
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                WorkspaceQueryRequest request = WorkspaceQueryRequest.builder(query)
                        .addSourceId(SmartSearchConnectionConfig.require("SMARTSEARCH_SOURCE_ID"))           // only this source
                        .filters(Map.of("all", List.of(                                 // AND of these clauses
                                Map.of("search_type", "match", "field", "title", "value", titleWord))))
                        .build();
                System.out.println("Source + title filter:");
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/search
                WorkplaceResultPrinter.printDocuments(ss.workplace().search(SmartSearchConnectionConfig.workspaceId(), request).getBody());
            }
        });
    }
}
