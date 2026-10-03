/**
 * Problem: Return one grounded answer with its evidence instead of only a results list.
 *
 * Step 33: Query in answer mode: the request, and reading answer, sources, citations, run ID, status.
 * Run: npm run example -- ask_a_question [query]
 * Requires workspace membership; answers also require a configured answering agent.
 * Standard memory keeps service callers from sharing long-term personal memory.
 * Expected: documents, an answer with sources, or streamed text followed by a final answer.
 * If refused: check membership, allowed sources, and answering-agent setup with your Owner.
 */
import { connect, require, queryText, printAnswer } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    workspace = require("SMARTSEARCH_WORKSPACE_ID");
  const query = queryText(args, "What is our travel policy?");
  const response = await client.workplaceQuery(workspace, {
    query,
    mode: "answer",
    memory_mode: "standard",
  });
  printAnswer(response);
  console.log(
    `run=${String(response.run_id)} status=${String(response.status)}`,
  );
}
