package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.NeuralMode;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Searched fields versus returned fields: two different lists.
 *
 * <p>{@code fields(...)} says WHERE the query text is looked for. {@code responseFields(...)}
 * says WHAT each hit sends back. Without {@code fields(...)} the project's configured fields are
 * searched. Searching only the title finds titles containing the word; searching the plot
 * summary finds documents that are about it. Returning few fields keeps responses small.
 *
 * <p>Keyword search is used here ({@code NeuralMode.BM25}) because the searched-fields list
 * applies to keyword matching; semantic search compares meaning instead (see
 * {@code KeywordVsSemanticVsHybrid}).
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh ChooseSearchedAndReturnedFields galaxy}
 */
public final class ChooseSearchedAndReturnedFields {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = ExampleRunner.queryText(args, "galaxy");
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                for (String searched : new String[]{"title", "overview"}) {
                    SearchQuery query = SearchQuery.builder()
                            .q(q)
                            .fields(searched)                 // search only this field (replaces earlier fields(...))
                            .responseFields("title")          // but return only the title
                            .neuralMode(NeuralMode.BM25)
                            .size(3)
                            .build();
                    System.out.println("q=\"" + q + "\" searched in " + searched + ":");
                    // POST {apiUrl}/core/projects/{projectId}/search
                    SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title");
                }
            }
        });
    }
}
