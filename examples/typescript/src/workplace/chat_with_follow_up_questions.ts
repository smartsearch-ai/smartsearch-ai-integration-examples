/**
 * Problem: A follow-up refers to the first question; preserve the returned conversation session.
 *
 * Step 38: Conversations: reuse the session for follow-ups.
 * Run: npm run example -- chat_with_follow_up_questions [query]
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
  const first = await client.workplaceChat(workspace, {
    query: args[0] ?? query,
    memory_mode: "standard",
  });
  printAnswer(first);
  if (typeof first.session_id !== "string")
    throw new Error("No chat session returned");
  // POST /chat again with the returned session ID preserves the conversation, even with standard memory.
  printAnswer(
    await client.workplaceChat(workspace, {
      query: args[1] ?? "When was it last updated?",
      session_id: first.session_id,
      memory_mode: "standard",
    }),
  );
}
