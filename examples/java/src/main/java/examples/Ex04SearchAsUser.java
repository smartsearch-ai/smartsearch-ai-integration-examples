package examples;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.UserWorkplace;
import com.weblinktechs.smartsearch.client4j.workspace.WorkspaceQueryRequest;

/**
 * 04 - Search Workplace as one of your users, starting from that user's SmartSearch AI access
 * token (OAuth 2.0 Token Exchange, RFC 8693).
 *
 * <p>Preconditions:
 * <ul>
 *   <li>"Act as users" is enabled for your service key with the <b>token exchange</b> grant
 *       (see "00 Administrator setup" in the README).</li>
 *   <li>SMARTSEARCH_USER_ACCESS_TOKEN holds an access token the user obtained by signing in to
 *       SmartSearch AI (for example through your single sign-on). Tokens are short-lived.</li>
 * </ul>
 *
 * <p>The exchanged token acts as the user: results respect that user's workspace memberships and
 * document permissions, and the user may only search, query and chat.
 */
public final class Ex04SearchAsUser {

    public static void main(String[] args) {
        Console.run(() -> {
            String query = Config.queryText(args, "What is our travel policy?");
            try (SmartSearchAi ss = Config.connect();
                 UserWorkplace user = ss.asUserFromToken(Config.require("SMARTSEARCH_USER_ACCESS_TOKEN"))) {
                System.out.println("Acting as the user until " + user.expiresAt());
                var body = user.search(Config.workspaceId(), WorkspaceQueryRequest.builder(query).build()).getBody();
                Workplaces.printDocuments(body);
            }
        });
    }
}
