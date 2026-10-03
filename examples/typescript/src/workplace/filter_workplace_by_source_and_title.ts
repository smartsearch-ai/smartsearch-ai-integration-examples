/**
 * Problem: Search one allowed source and only documents whose title mentions a word.
 *
 * Step 32: Limit a search to one source; filter on a document field.
 * Run: npm run example -- filter_workplace_by_source_and_title "travel policy" policy
 * Requires workspace membership; answers also require a configured answering agent.
 * Standard memory keeps service callers from sharing long-term personal memory.
 * Expected: documents=<count>, followed by permitted document titles.
 * If refused: check membership, allowed sources, and answering-agent setup with your Owner.
 */
import { connect, require, printDocuments } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    workspace = require("SMARTSEARCH_WORKSPACE_ID");
  const query = args[0] ?? "What is our travel policy?";
  // source_ids narrows retrieval; this business-field filter never controls authorization.
  const source = require("SMARTSEARCH_SOURCE_ID");
  printDocuments(
    await client.workplaceSearch(workspace, {
      query,
      mode: "retrieval_only",
      memory_mode: "standard",
      source_ids: [source],
      filters: {
        all: [
          { search_type: "match", field: "title", value: args[1] ?? "policy" },
        ],
      },
    }),
  );
}
