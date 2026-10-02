package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.UserWorkplace;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;

/**
 * Search Workplace as one of your users, starting from a token the user already has
 * (OAuth 2.0 Token Exchange, RFC 8693).
 *
 * <p><b>Why act as the user?</b> Each user should see only the documents they are allowed to see.
 * Acting as the user makes SmartSearch AI apply that user's workspace memberships and document
 * permissions, and gives the user their own memory. The exchanged token may only search, query
 * and chat.
 *
 * <p><b>How.</b> Your backend sends the user's SmartSearch AI access token together with your
 * service key to the identity server, and gets back a token that acts as the user on behalf of
 * your service. Use this when your users already sign in to SmartSearch AI (for example through
 * single sign-on). If they sign in to your own identity provider instead, see
 * {@code SignInWithYourIdentityProvider}.
 *
 * <p><b>Preconditions.</b>
 * <ul>
 *   <li>"Act as users" is enabled for your service key with <b>token exchange</b> allowed
 *       (see "Administrator setup" in the README).</li>
 *   <li>SMARTSEARCH_USER_ACCESS_TOKEN holds the user's current access token. It is short-lived:
 *       set it just before running, never store it in a file.</li>
 * </ul>
 * Without token exchange enabled for the key the identity server refuses, and the example stops
 * with "Could not obtain a token".
 *
 * <p>Run: {@code ./run.sh SearchWorkplaceAsUser "What is our travel policy?"}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect documents, sources or the final answer for this caller.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class SearchWorkplaceAsUser {

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String query = ExampleRunner.queryText(
                args,
                "What is our travel policy?"
            );
            // asUserFromToken: POST {authUrl}/realms/{realm}/protocol/openid-connect/token
            //   grant_type = urn:ietf:params:oauth:grant-type:token-exchange, subject_token = the user's token.
            //   Throws CredentialAcquisitionException when refused (grant not enabled, token expired).
            // The UserWorkplace holds the exchanged token; close it when done (try-with-resources).
            try (
                SmartSearchAi ss = SmartSearchConnectionConfig.connect();
                UserWorkplace user = ss.asUserFromToken(
                    SmartSearchConnectionConfig.require(
                        "SMARTSEARCH_USER_ACCESS_TOKEN"
                    )
                )
            ) {
                // The token is not renewed: get a new one before this time.
                System.out.println(
                    "Acting as the user until " + user.expiresAt()
                );

                // Same call as the service search, now limited to what this user may see.
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/search   (as the user)
                WorkplaceResultPrinter.printDocuments(
                    user
                        .search(
                            SmartSearchConnectionConfig.workspaceId(),
                            WorkspaceQueryRequest.builder(query).build()
                        )
                        .getBody()
                );
            }
        });
    }
}
