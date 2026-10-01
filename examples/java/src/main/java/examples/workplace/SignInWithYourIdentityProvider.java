package examples.workplace;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.UserWorkplace;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import co.smartsearchai.workplace.WorkspaceQueryRequest.MemoryMode;
import co.smartsearchai.workplace.WorkspaceQueryRequest.Mode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

/**
 * Your users sign in to YOUR identity provider; your backend then searches and answers as them
 * (JWT Authorization Grant, RFC 7523). No SmartSearch AI password is involved.
 *
 * <p><b>How it fits together.</b>
 * <ol>
 *   <li>Once: register the users ({@code RegisterUsers}), linking each one to their account in
 *       your identity provider, and give them access ({@code RegisterUsersWithWorkspaceAccess}).</li>
 *   <li>Each sign-in: your identity provider, or your backend, issues a signed token (an
 *       <i>assertion</i>) that says who the user is. {@code CreateUserAssertion} shows how to
 *       build and sign one, and the public key set to publish.</li>
 *   <li>Your backend passes that assertion to {@code asUser(...)}. The identity server checks it
 *       against your registered identity provider and returns a token that acts as the linked
 *       SmartSearch AI user on behalf of your service.</li>
 * </ol>
 *
 * <p><b>Preconditions.</b>
 * <ul>
 *   <li>Your identity provider is registered with SmartSearch AI, and "Act as users" is enabled
 *       for your service key with the JWT authorization grant (see "Administrator setup").</li>
 *   <li>The user is registered and has workspace access.</li>
 *   <li>SMARTSEARCH_USER_ASSERTION holds a FRESH assertion for the user. Assertions are single-use
 *       and short-lived (at most 5 minutes): get a new one for every {@code asUser} call.</li>
 * </ul>
 *
 * <p>Run: {@code ./run.sh SignInWithYourIdentityProvider "What is our travel policy?"}
 */
public final class SignInWithYourIdentityProvider {

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String query = ExampleRunner.queryText(args, "What is our travel policy?");
            String workspaceId = SmartSearchConnectionConfig.workspaceId();
            // asUser: POST {authUrl}/realms/{realm}/protocol/openid-connect/token
            //   grant_type = urn:ietf:params:oauth:grant-type:jwt-bearer, assertion = the user's token.
            //   Throws CredentialAcquisitionException when the identity server refuses, for example
            //   for an expired or already-used assertion.
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect();
                 UserWorkplace user = ss.asUser(SmartSearchConnectionConfig.require("SMARTSEARCH_USER_ASSERTION"))) {
                System.out.println("Signed in as the user until " + user.expiresAt());

                // Search: only the documents this user is allowed to see.
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/search   (as the user)
                WorkplaceResultPrinter.printDocuments(user.search(workspaceId, WorkspaceQueryRequest.builder(query).build()).getBody());

                // Answer: written only from those documents.
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/query   (as the user)
                var answer = user.query(workspaceId, WorkspaceQueryRequest.builder(query)
                        .mode(Mode.ANSWER)
                        .memoryMode(MemoryMode.STANDARD)        // AGENTIC would use this user's own long-term memory
                        .build()).getBody();
                WorkplaceResultPrinter.printAnswer(answer);
            }
        });
    }
}
