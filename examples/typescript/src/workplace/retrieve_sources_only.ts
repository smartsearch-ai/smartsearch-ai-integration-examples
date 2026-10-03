/**
 * Problem: Your own model needs the permitted evidence, without asking Workplace to generate an answer.
 *
 * Step 34: Query in retrieval-only mode: the evidence without an answer.
 * Run: npm run example -- retrieve_sources_only [query]
 * Requires workspace membership and permitted source evidence; no answering agent is needed.
 * Standard memory keeps service callers from sharing long-term personal memory.
 * Expected: source titles and a sources count, without a generated answer.
 * If refused: check membership and allowed sources with your Owner.
 */
import { connect, require, queryText, printSources } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    workspace = require("SMARTSEARCH_WORKSPACE_ID");
  const query = queryText(args, "What is our travel policy?");
  printSources(
    await client.workplaceQuery(workspace, {
      query,
      mode: "retrieval_only",
      memory_mode: "standard",
    }),
  );
}
