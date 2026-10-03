package examples;

import co.smartsearchai.auth.CredentialAcquisitionException;
import co.smartsearchai.provisioning.ProvisioningClientException;
import co.smartsearchai.search.SearchException;
import co.smartsearchai.workplace.WorkspaceClientException;

/**
 * Runs an example's code and turns any SDK error into one readable {@code ERROR} line, so each
 * example can focus on the SDK calls it teaches.
 *
 * <p><i>Example plumbing, not part of the SDK.</i> Copy or replace it in your own code.
 */
public final class ExampleRunner {

    private ExampleRunner() {}

    /** Body of an example. */
    @FunctionalInterface
    public interface Example {
        void run() throws Exception;
    }

    /**
     * Runs an example. On failure prints one {@code ERROR} line and exits with status 1.
     *
     * <p>Each SDK area has its own exception type. They carry the HTTP status and the server's
     * error code, and never contain tokens, secrets or request bodies, so they are safe to log.
     */
    public static void run(Example example) {
        try {
            example.run();
        } catch (WorkspaceClientException e) {
            // Workplace: getStatusCode() is the HTTP status, getCode() the server's error code.
            fail(
                "Workplace request refused: HTTP " +
                    e.getStatusCode() +
                    " " +
                    e.getCode() +
                    " - " +
                    e.getMessage()
            );
        } catch (ProvisioningClientException e) {
            // Provisioning: isRetryable() says whether the same request may succeed later.
            fail(
                "Provisioning request refused: HTTP " +
                    e.getStatusCode() +
                    " " +
                    e.getCode() +
                    (e.isRetryable() ? " (retryable)" : "")
            );
        } catch (SearchException e) {
            // Project search: statusCode() is 0 when no response arrived (network, timeout).
            fail(e.getMessage());
        } catch (CredentialAcquisitionException e) {
            // The identity server refused to issue a token (wrong key, disabled grant, expired user token).
            fail("Could not obtain a token: " + e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Invalid input caught by the SDK before sending, or a missing setting.
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void fail(String message) {
        System.err.println("ERROR " + message);
        System.exit(1);
    }

    /** Collapses whitespace and shortens text to at most {@code max} characters, for one-line output. */
    public static String shorten(String text, int max) {
        String s = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    /** Query text for an example: its command-line arguments joined, or {@code fallback} when there are none. */
    public static String queryText(String[] args, String fallback) {
        return args.length > 0 && !args[0].isBlank()
            ? String.join(" ", args)
            : fallback;
    }
}
