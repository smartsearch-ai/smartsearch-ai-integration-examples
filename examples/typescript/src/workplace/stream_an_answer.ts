/**
 * Problem: Show answer text as it arrives, then use the final completion as the authoritative answer.
 *
 * Step 35: Stream a query answer as it is written; events, errors, timeouts.
 * Run: npm run example -- stream_an_answer [query]
 * Requires workspace membership; answers also require a configured answering agent.
 * Standard memory keeps service callers from sharing long-term personal memory.
 * Expected: documents, an answer with sources, or streamed text followed by a final answer.
 * If refused: check membership, allowed sources, and answering-agent setup with your Owner.
 */
import { connect, require, queryText, printAnswer } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after the response is printed; streaming waits for final completion.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    workspace = require("SMARTSEARCH_WORKSPACE_ID");
  const query = queryText(args, "What is our travel policy?");
  // Same /query endpoint; options.stream=true and Accept: text/event-stream.
  for await (const event of client.streamQuery(workspace, {
    query,
    mode: "answer",
    memory_mode: "standard",
  })) {
    if (event.event === "answer.delta")
      process.stdout.write(String(event.payload.text ?? ""));
    // The final completion is authoritative, replacing any provisional answer pieces.
    if (event.event === "run.completed") {
      console.log();
      printAnswer(event.payload);
    }
  }
}
