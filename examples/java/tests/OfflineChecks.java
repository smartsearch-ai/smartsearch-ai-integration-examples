package examples.search;

import co.smartsearchai.search.SearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;

/** Offline regression for HTTP-success envelopes that report application failure. */
public final class OfflineChecks {

    /** @param args unused */
    public static void main(String[] args) throws Exception {
        Constructor<SearchResult> constructor =
            SearchResult.class.getDeclaredConstructor(JsonNode.class);
        constructor.setAccessible(true);
        ObjectMapper json = new ObjectMapper();
        SearchResult failure = constructor.newInstance(
            json.readTree(
                "{\"code\":0,\"message\":\"do-not-log\",\"result\":{\"hits\":{\"hits\":[]}}}"
            )
        );
        try {
            SearchResultPrinter.printHits(failure, "title");
            throw new AssertionError(
                "Application failure displayed as empty success"
            );
        } catch (IllegalStateException expected) {
            if (expected.getMessage().contains("do-not-log")) {
                throw new AssertionError("Raw failure message exposed");
            }
        }
        SearchResult success = constructor.newInstance(
            json.readTree("{\"code\":1,\"result\":{\"hits\":{\"hits\":[]}}}")
        );
        if (SearchResultPrinter.requireSuccess(success) != success) {
            throw new AssertionError("Success envelope changed");
        }
        System.out.println(
            "Application failure and success envelope checks passed"
        );
    }
}
