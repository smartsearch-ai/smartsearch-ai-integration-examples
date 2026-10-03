/**
 * Problem: Search with the end user permissions rather than the service identity permissions.
 *
 * Step 42: Act as a user from the user's own access token, and search.
 * Run: npm run example -- search_workplace_as_user [query]
 * Requires workspace membership; answers also require a configured answering agent.
 * Standard memory keeps service callers from sharing long-term personal memory.
 * Expected: documents=<count>, followed by permitted document titles.
 * If refused: check membership, allowed sources, and answering-agent setup with your Owner.
 */
import { connect, require, queryText, printDocuments } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  // Exchange is one-shot: the resulting user token is not silently renewed or replaced by a service token.
  const user = await connect().asUserFromToken(
    require("SMARTSEARCH_USER_ACCESS_TOKEN"),
  );
  console.log("Delegated identity expires:", user.expiresAt.toISOString());
  const query = queryText(args, "What is our travel policy?");
  const workspace = require("SMARTSEARCH_WORKSPACE_ID");
  printDocuments(
    await user.search(workspace, { query, memory_mode: "standard" }),
  );
}
