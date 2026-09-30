package examples;

import co.smartsearchai.SmartSearchAi;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;

import java.util.List;
import java.util.Map;

/**
 * W1 - Narrow a Workplace search to one source and filter on a document field.
 *
 * <p>Arguments (optional): the query, then a word the title must contain.
 *
 * <p>Precondition: your service key (or user) can see SMARTSEARCH_SOURCE_ID in
 * SMARTSEARCH_WORKSPACE_ID.
 *
 * <p>Workplace filters are a list of clauses that must all match ({@code all}). Clause types:
 * term, terms, match and match_phrase on source_type, title, content, snippet, source_url, author
 * and {@code metadata.*}. Access-control fields are set by the server and are rejected if you
 * send them.
 */
public final class W1SourceAndFieldFilters {

    public static void main(String[] args) {
        Console.run(() -> {
            String workspaceId = Config.workspaceId();
            String sourceId = Config.require("SMARTSEARCH_SOURCE_ID");
            String query = args.length > 0 ? args[0] : "travel policy";
            String titleWord = args.length > 1 ? args[1] : "policy";
            try (SmartSearchAi ss = Config.connect()) {
                // Only this source, and the title must contain the word.
                var scoped = WorkspaceQueryRequest.builder(query)
                        .addSourceId(sourceId)
                        .filters(Map.of("all", List.of(
                                Map.of("search_type", "match", "field", "title", "value", titleWord))))
                        .build();
                System.out.println("Source + title filter:");
                Workplaces.printDocuments(ss.workplace().search(workspaceId, scoped).getBody());
            }
        });
    }
}
