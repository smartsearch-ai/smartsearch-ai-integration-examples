/**
 * Problem: Your backend needs permitted Workplace documents using its own service identity.
 *
 * Step 31: Workplace concepts; search, query and chat; the documents that match.
 * Run: npm run example -- search_workplace_as_service [query]
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
  const client = connect(),
    workspace = require("SMARTSEARCH_WORKSPACE_ID");
  const query = queryText(args, "What is our travel policy?");
  printDocuments(
    await client.workplaceSearch(workspace, {
      query,
      mode: "retrieval_only",
      memory_mode: "standard",
    }),
  );
}
