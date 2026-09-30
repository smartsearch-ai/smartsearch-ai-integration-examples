package examples;

import co.smartsearchai.SmartSearchAi;

/**
 * Reads connection settings and IDs from environment variables, never from source code.
 * The variable names match {@code .env.example} at the repository root.
 */
public final class Config {

    private Config() {
    }

    /** A required variable; exits with a clear message when it is missing. */
    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is not set (see .env.example)");
        }
        return value.trim();
    }

    /** An optional variable, or {@code fallback} when unset. */
    public static String optional(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /** A client authenticated with your service key. Close it when you are done (try-with-resources). */
    public static SmartSearchAi connect() {
        return SmartSearchAi.builder()
                .apiUrl(require("SMARTSEARCH_API_BASE_URL"))      // project search + Workplace
                .adminUrl(require("SMARTSEARCH_ADMIN_BASE_URL"))  // user provisioning
                .authUrl(require("SMARTSEARCH_AUTH_BASE_URL"))
                .realm(require("SMARTSEARCH_REALM"))
                .serviceKey(require("SMARTSEARCH_CLIENT_ID"), require("SMARTSEARCH_CLIENT_SECRET"))
                .build();
    }

    public static String projectId() {
        return require("SMARTSEARCH_PROJECT_ID");
    }

    public static String workspaceId() {
        return require("SMARTSEARCH_WORKSPACE_ID");
    }

    public static String integrationId() {
        return require("SMARTSEARCH_INTEGRATION_ID");
    }

    /** Query text: the first program argument, or the example's default. */
    public static String queryText(String[] args, String fallback) {
        return args.length > 0 && !args[0].isBlank() ? String.join(" ", args) : fallback;
    }
}
