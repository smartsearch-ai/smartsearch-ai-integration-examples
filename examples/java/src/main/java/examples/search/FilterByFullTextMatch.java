package examples.search;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Filter on words inside a text field: love stories whose plot mentions Paris.
 *
 * <p>{@code Filter.match(field, text)} is a full-text condition. Unlike {@code Filter.term},
 * which compares the whole value exactly, match reads the text the way search does: it finds the
 * word anywhere in the field, whatever its letter case. Use term for codes and categories,
 * match for free text.
 *
 * <p>Precondition: as {@code FirstSearch}. Run: {@code ./run.sh FilterByFullTextMatch love paris}
 * (first word = query, second = word the plot must mention).
 */
public final class FilterByFullTextMatch {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String q = args.length > 0 ? args[0] : "love";
            String word = args.length > 1 ? args[1] : "paris";
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(q)
                        .responseFields("title", "overview")
                        .filter(Filter.match("overview", word))   // overview mentions the word
                        .size(3)
                        .build();
                System.out.println("q=\"" + q + "\", overview matches \"" + word + "\":");
                // POST {apiUrl}/core/projects/{projectId}/search
                SearchResultPrinter.printHits(ss.search().search(SmartSearchConnectionConfig.projectId(), query), "title", "overview");
            }
        });
    }
}
