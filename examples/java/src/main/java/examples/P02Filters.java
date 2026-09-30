package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.search.Filter;
import co.smartsearchai.search.SearchQuery;

/**
 * P2 - Filters: required (all), any-of, excluded (not), range, exists and phrase match.
 *
 * <p>Precondition: as P1. Filters restrict results; they do not change scores.
 */
public final class P02Filters {

    private static final String[] FIELDS = {"title", "original_language", "vote_average", "status"};

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                SearchQuery query = SearchQuery.builder()
                        .q(Config.queryText(args, "space"))
                        .responseFields(FIELDS)
                        .filter(Filter.term("original_language", "en"))          // must match
                        .filter(Filter.range("vote_average", 7.0, null, null, null)) // vote_average >= 7.0
                        .anyOf(Filter.terms("genres", "Science Fiction", "Adventure"), Filter.exists("tagline"))
                        .exclude(Filter.term("status", "Rumored"))                 // must not match
                        .size(5)
                        .build();
                System.out.println("Filtered search:");
                Console.printHits(ss.search().search(Config.projectId(), query), FIELDS);

                // Phrase match: the words must appear together, in order.
                SearchQuery phrase = SearchQuery.builder().q("galaxy").responseFields("title", "tagline").size(3)
                        .filter(Filter.matchPhrase("tagline", "a galaxy far, far away"))
                        .build();
                System.out.println("Phrase filter on tagline:");
                Console.printHits(ss.search().search(Config.projectId(), phrase), "title", "tagline");
            }
        });
    }
}
