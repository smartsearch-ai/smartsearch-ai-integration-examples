package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.UserWorkplace;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.MemoryMode;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest.Mode;

/**
 * 05 - Your users sign in to YOUR identity provider; your backend then acts as them in
 * SmartSearch AI (JWT Authorization Grant, RFC 7523). No SmartSearch AI password is involved.
 *
 * <p>Preconditions:
 * <ul>
 *   <li>Your identity provider is registered with SmartSearch AI, and "Act as users" is enabled
 *       for your service key with the JWT authorization grant (see "00 Administrator setup").</li>
 *   <li>The user was registered (example 02) and has workspace access (example 03).</li>
 *   <li>SMARTSEARCH_USER_ASSERTION holds a fresh token your identity provider issued for the user.
 *       Assertions are single-use and short-lived (at most 5 minutes): mint one per sign-in.</li>
 * </ul>
 */
public final class Ex05PartnerIdpLogin {

    public static void main(String[] args) {
        Console.run(() -> {
            String query = Config.queryText(args, "What is our travel policy?");
            String workspaceId = Config.workspaceId();
            try (SmartSearchAi ss = Config.connect();
                 UserWorkplace user = ss.asUser(Config.require("SMARTSEARCH_USER_ASSERTION"))) {
                System.out.println("Signed in as the user until " + user.expiresAt());

                // Search: the documents this user is allowed to see.
                Workplaces.printDocuments(user.search(workspaceId, WorkspaceQueryRequest.builder(query).build()).getBody());

                // Answer: generated only from those documents. STANDARD memory stores nothing about the question.
                var answer = user.query(workspaceId, WorkspaceQueryRequest.builder(query)
                        .mode(Mode.ANSWER).memoryMode(MemoryMode.STANDARD).build()).getBody();
                Workplaces.printAnswer(answer);
            }
        });
    }
}
