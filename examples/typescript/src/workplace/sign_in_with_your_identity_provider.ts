/**
 * Problem: Exchange a fresh signed identity assertion, then search and answer as that user.
 *
 * Step 44: Act as a user who signed in to your identity provider; search and ask a question as them.
 * Run: npm run example -- sign_in_with_your_identity_provider [query]
 * Requires workspace membership; answers also require a configured answering agent.
 * Standard memory keeps service callers from sharing long-term personal memory.
 * Expected: documents, an answer with sources, or streamed text followed by a final answer.
 * If refused: check membership, allowed sources, and answering-agent setup with your Owner.
 */
import {
  connect,
  require,
  queryText,
  printDocuments,
  printAnswer,
} from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  // Exchange is one-shot: the resulting user token is not silently renewed or replaced by a service token.
  const user = await connect().asUser(require("SMARTSEARCH_USER_ASSERTION"));
  console.log("Delegated identity expires:", user.expiresAt.toISOString());
  const query = queryText(args, "What is our travel policy?");
  const workspace = require("SMARTSEARCH_WORKSPACE_ID");
  printDocuments(
    await user.search(workspace, { query, memory_mode: "standard" }),
  );
  printAnswer(
    await user.query(workspace, {
      query,
      mode: "answer",
      memory_mode: "standard",
    }),
  );
}
