package examples;

import co.smartsearchai.search.SearchException;
import co.smartsearchai.search.SearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.weblinktechs.smartsearch.client4j.auth.CredentialAcquisitionException;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningClientException;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceClientException;

/** Small output helpers shared by the examples. */
public final class Console {

    private Console() {
    }

    /** Body of an example. */
    @FunctionalInterface
    public interface Example {
        void run() throws Exception;
    }

    /**
     * Runs an example and turns SDK errors into one readable line. SDK exceptions carry the HTTP
     * status and the server's error code, never tokens or request bodies.
     */
    public static void run(Example example) {
        try {
            example.run();
        } catch (WorkspaceClientException e) {
            fail("Workplace request refused: HTTP " + e.getStatusCode() + " " + e.getCode() + " - " + e.getMessage());
        } catch (ProvisioningClientException e) {
            fail("Provisioning request refused: HTTP " + e.getStatusCode() + " " + e.getCode()
                    + (e.getRequestId() != null ? " (request " + e.getRequestId() + ")" : ""));
        } catch (SearchException e) {
            fail(e.getMessage());
        } catch (CredentialAcquisitionException e) {
            fail("Could not obtain a token: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void fail(String message) {
        System.err.println("ERROR " + message);
        System.exit(1);
    }

    /** Collapses whitespace and shortens text for one-line output. */
    public static String clip(String text, int max) {
        String s = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    /** Prints the hits of a project search: how many, the mode the server ran, any warning, and one line per hit. */
    public static void printHits(SearchResult page, String... fields) {
        JsonNode hits = page.result().path("hits");
        System.out.println("hits=" + hits.path("hits").size()
                + " mode=" + page.effectiveNeuralMode()
                + (page.warning() != null ? " warning=\"" + clip(page.warning(), 120) + "\"" : ""));
        int rank = 1;
        for (JsonNode hit : hits.path("hits")) {
            JsonNode source = hit.path("_source");
            StringBuilder line = new StringBuilder(String.format("%2d. ", rank++));
            for (int i = 0; i < fields.length; i++) {
                if (!source.has(fields[i])) continue;
                line.append(i == 0 ? "" : " | ").append(i == 0 ? "" : fields[i] + "=")
                        .append(clip(source.path(fields[i]).asText(), 60));
            }
            System.out.println(line);
        }
    }
}
