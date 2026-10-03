/**
 * Problem: Compare a one-off answer with an answer that records context in the caller memory.
 *
 * Step 36: Memory modes: STANDARD versus AGENTIC, and when to use each.
 * Run: npm run example -- answer_with_memory [query]
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
  // AGENTIC records the question in the SERVICE identity's memory; do not share it across people.
  for (const memory of ["standard", "agentic"] as const) {
    const response = await client.workplaceQuery(workspace, {
      query,
      mode: "answer",
      memory_mode: memory,
    });
    printAnswer(response);
    console.log(JSON.stringify(response.memory));
  }
}
