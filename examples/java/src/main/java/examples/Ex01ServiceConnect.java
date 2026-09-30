package examples;

import co.smartsearchai.SmartSearchAi;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 01 - Connect with a service key and read what your integration is allowed to do.
 *
 * <p>Precondition: a service key issued by your SmartSearch AI administrator
 * (see "00 Administrator setup" in the README).
 *
 * <p>The SDK exchanges the key for a short-lived access token (OAuth client credentials) and
 * refreshes it for you. Nothing here needs a user.
 */
public final class Ex01ServiceConnect {

    public static void main(String[] args) {
        Console.run(() -> {
            try (SmartSearchAi ss = Config.connect()) {
                // GET /provisioning/v1/capabilities: which provisioning features are enabled for this key.
                JsonNode capabilities = ss.users().capabilities().getBody();
                JsonNode result = capabilities.has("result") ? capabilities.path("result") : capabilities;

                System.out.println("Connected: " + ss);   // prints URLs and realm only, never the secret
                System.out.println("Provisioning capabilities:");
                result.fields().forEachRemaining(field ->
                        System.out.println("  " + field.getKey() + " = " + Console.clip(field.getValue().toString(), 200)));
            }
        });
    }
}
