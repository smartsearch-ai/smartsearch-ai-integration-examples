package examples;

import co.smartsearchai.SmartSearchAi;

/**
 * The connection settings for the examples: reads every URL, key and ID from environment
 * variables, so nothing secret is ever written in source code, and builds the SDK client from
 * them ({@link #connect()}). The variable names match {@code .env.example} at the repository root.
 *
 * <p><i>Example plumbing, not part of the SDK.</i> Copy or replace it in your own code.
 *
 * <p>All values come from your SmartSearch AI administrator:
 *
 * <pre>
 * SMARTSEARCH_API_BASE_URL    https://api.your-company.example.com
 *                             The SmartSearch API gateway. Project search and Workplace calls go here.
 * SMARTSEARCH_ADMIN_BASE_URL  https://admin.your-company.example.com
 *                             Search Admin. User provisioning (registering users) goes here.
 * SMARTSEARCH_AUTH_BASE_URL   https://auth.your-company.example.com
 *                             The identity server's base URL: the part BEFORE /realms/...
 * SMARTSEARCH_REALM           your-realm
 *                             The name of your login realm on that identity server.
 *                             Together they give the token endpoint the SDK calls:
 *                             https://auth.your-company.example.com/realms/your-realm/protocol/openid-connect/token
 * SMARTSEARCH_CLIENT_ID       svc-your-key-id        Service key ID (starts with svc-)
 * SMARTSEARCH_CLIENT_SECRET   (secret)               Service key secret. Never commit it.
 * </pre>
 */
public final class SmartSearchConnectionConfig {

    private SmartSearchConnectionConfig() {}

    /** A required variable; stops the example with a clear message when it is missing. */
    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Environment variable " +
                    name +
                    " is not set (see .env.example)"
            );
        }
        return value.trim();
    }

    /** An optional variable, or {@code fallback} when unset. */
    public static String optional(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /**
     * Connects: builds the SDK client ({@link SmartSearchAi}) that authenticates as your service key.
     *
     * <p>Building it makes no network call. On the first request the SDK exchanges the key for a
     * short-lived access token (OAuth 2.0 client credentials, {@code POST {authUrl}/realms/{realm}/protocol/openid-connect/token})
     * and renews it automatically before it expires.
     *
     * <p>The client is thread-safe: create one per application and share it. Close it on shutdown
     * (the examples use try-with-resources). Misconfigured values (blank, not an http(s) URL) fail
     * here with an {@link IllegalArgumentException} naming the setting.
     */
    public static SmartSearchAi connect() {
        return SmartSearchAi.builder()
            .apiUrl(require("SMARTSEARCH_API_BASE_URL")) // API gateway: project search + Workplace
            .adminUrl(require("SMARTSEARCH_ADMIN_BASE_URL")) // Search Admin: user provisioning
            .authUrl(require("SMARTSEARCH_AUTH_BASE_URL")) // identity server base URL, without /realms/...
            .realm(require("SMARTSEARCH_REALM")) // your realm name
            .serviceKey(
                require("SMARTSEARCH_CLIENT_ID"),
                require("SMARTSEARCH_CLIENT_SECRET")
            )
            .build();
    }

    /** The project to search (SMARTSEARCH_PROJECT_ID). Your service key must be assigned to it. */
    public static String projectId() {
        return require("SMARTSEARCH_PROJECT_ID");
    }

    /** The Workplace workspace to search (SMARTSEARCH_WORKSPACE_ID). Your service key must be a member. */
    public static String workspaceId() {
        return require("SMARTSEARCH_WORKSPACE_ID");
    }

    /** The provisioning integration your service key registers users through (SMARTSEARCH_INTEGRATION_ID). */
    public static String integrationId() {
        return require("SMARTSEARCH_INTEGRATION_ID");
    }
}
