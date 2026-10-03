package examples.gettingstarted;

import co.smartsearchai.SmartSearchAi;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Connect with your service key and read its provisioning capabilities.
 *
 * <p><b>Concepts.</b> A <i>service key</i> is the identity of your application (not of a person)
 * in SmartSearch AI: an ID starting with {@code svc-} plus a secret, issued by your administrator.
 * Your backend keeps it in an environment variable or secret store and never ships it to a
 * browser or mobile app. The SDK trades it for a short-lived access token (OAuth 2.0 client
 * credentials) and renews the token for you; your code never handles the token.
 *
 * <p>Every call in these examples authenticates as this key, except where an example acts as one
 * of your users (see the Workplace examples).
 *
 * <p><b>Preconditions.</b> The connection settings from {@code .env.example} (see {@link SmartSearchConnectionConfig}).
 * This example asks Search Admin which user-provisioning features are enabled for the key, which
 * checks authentication and the Admin capabilities call only. Test project/workspace access separately. It needs no project or workspace.
 *
 * <p>Run: {@code ./run.sh ConnectAndCheckAccess}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect Admin provisioning capabilities; this does not prove search access.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class ConnectAndCheckAccess {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            // SmartSearchConnectionConfig.connect() only validates the settings; the first call below fetches a token.
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                // toString() shows the URLs and realm, never the secret.
                System.out.println("Connected: " + ss);

                // GET {adminUrl}/search-admin/api/provisioning/v1/capabilities
                // First the SDK gets a token: POST {authUrl}/realms/{realm}/protocol/openid-connect/token.
                // Errors: CredentialAcquisitionException when the identity server refuses the key
                // (wrong ID or secret, wrong realm); ProvisioningClientException, with the HTTP
                // status and the server's error code, when Search Admin refuses the key.
                JsonNode body = ss.users().capabilities().getBody();

                // The answer lists the provisioning job kinds this key may submit and its limits,
                // for example kinds = ["UPSERT_USERS", "ONBOARD_USERS", ...] and max_items.
                JsonNode capabilities = body.has("result")
                    ? body.path("result")
                    : body;
                System.out.println("Provisioning capabilities:");
                capabilities
                    .properties()
                    .forEach(field ->
                        System.out.println(
                            "  " +
                                field.getKey() +
                                " = " +
                                ExampleRunner.shorten(
                                    field.getValue().toString(),
                                    200
                                )
                        )
                    );
            }
        });
    }
}
