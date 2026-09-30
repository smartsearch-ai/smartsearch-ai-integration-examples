package examples;

import com.fasterxml.jackson.databind.JsonNode;

/** Output helpers shared by the Workplace examples. */
public final class Workplaces {

    private Workplaces() {
    }

    /** Prints the documents of a Workplace search or retrieval-only query. */
    public static void printDocuments(JsonNode body) {
        JsonNode docs = body.path("documents");
        System.out.println("documents=" + docs.size());
        int rank = 1;
        for (JsonNode doc : docs) {
            System.out.printf("%2d. %s%n", rank++, Console.clip(doc.path("title").asText(), 90));
        }
    }

    /** Prints an answer and the sources it cites. */
    public static void printAnswer(JsonNode body) {
        System.out.println("answer: " + Console.clip(body.path("answer").asText(), 300));
        JsonNode sources = body.path("sources");
        System.out.println("sources=" + sources.size());
        for (JsonNode source : sources) {
            System.out.println("  - " + Console.clip(source.path("title").asText(), 90));
        }
    }
}
