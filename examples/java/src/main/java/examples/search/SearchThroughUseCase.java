package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.SearchQuery;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Use-case search: search through a named, saved search configuration.
 *
 * <p>A <i>use case</i> belongs to a project and holds its own search settings (fields, relevance
 * tuning, search technique) for one screen or purpose, such as "site search" or "support
 * articles". Searching through it applies those settings, so your code does not repeat them and
 * an administrator can tune them without a code change. Send the same {@link SearchQuery}; the
 * only difference is the use-case ID in place of the project ID.
 *
 * <p>Precondition: SMARTSEARCH_USECASE_ID is a use case of a project your service key is assigned
 * to (your administrator gives you the ID). An unknown ID fails with HTTP 404 "Usecase not found".
 *
 * <p>Run: {@code ./run.sh SearchThroughUseCase "star wars"}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect the received hits, actual retrieval mode and warning.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class SearchThroughUseCase {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String usecaseId = SmartSearchConnectionConfig.require(
                "SMARTSEARCH_USECASE_ID"
            );
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                    .q(ExampleRunner.queryText(args, "star wars"))
                    .responseFields("title", "release_date")
                    .size(5)
                    .build();
                // POST {apiUrl}/core/usecases/{usecaseId}/search
                SearchResultPrinter.printHits(
                    ss.search().searchByUsecase(usecaseId, query),
                    "title",
                    "release_date"
                );
            }
        });
    }
}
