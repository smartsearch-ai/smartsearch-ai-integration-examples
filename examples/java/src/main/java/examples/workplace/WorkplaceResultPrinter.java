package examples.workplace;

import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;

/**
 * Prints Workplace responses: the documents found, and an answer with its sources.
 *
 * <p><i>Example plumbing, not part of the SDK.</i> Copy or replace it in your own code.
 *
 * <p>A search answers {@code { "documents": [ { "title": ..., "snippet": ..., ... }, ... ] }}.
 * A generated answer adds {@code "answer"} (the text) and {@code "sources"} (the documents it
 * cites, same shape). The numbers in brackets in an answer, such as [1], refer to those sources.
 */
public final class WorkplaceResultPrinter {

    private WorkplaceResultPrinter() {
    }

    /** Prints the documents of a search or retrieval-only query. */
    public static void printDocuments(JsonNode body) {
        JsonNode docs = body.path("documents");
        System.out.println("documents=" + docs.size());
        int rank = 1;
        for (JsonNode doc : docs) {
            System.out.printf("%2d. %s%n", rank++, ExampleRunner.shorten(doc.path("title").asText(), 90));
        }
    }

    /** Prints an answer and the sources it cites. */
    public static void printAnswer(JsonNode body) {
        System.out.println("answer: " + ExampleRunner.shorten(body.path("answer").asText(), 300));
        JsonNode sources = body.path("sources");
        System.out.println("sources=" + sources.size());
        for (JsonNode source : sources) {
            System.out.println("  - " + ExampleRunner.shorten(source.path("title").asText(), 90));
        }
    }
}
