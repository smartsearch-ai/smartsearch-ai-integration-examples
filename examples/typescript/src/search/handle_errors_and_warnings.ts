/**
 * Problem: Distinguish invalid input, server refusal and a successful search that used a different technique.
 *
 * Step 30: Invalid requests, refusals, and adjusted answers.
 * Run: npm run example -- handle_errors_and_warnings [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Public Core project search uses the service identity, not delegated Workplace access.
 * Try it: run the request, read the hits, then change only the option this step teaches.
 * Expected: hits=<count>, actual mode and warning, followed by returned document fields.
 * If refused: check project assignment and schema; an empty list is a valid search outcome.
 */
import { connect, require, queryText, printSearch } from "../common.js";
import { searchQuery } from "../contracts.js";
import { ExampleError } from "../client.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    project = require("SMARTSEARCH_PROJECT_ID");
  const q = queryText(args, "star wars");
  try {
    searchQuery(q, { ssapi_flags: { precision: 12 } });
  } catch {
    console.log("Rejected invalid precision before sending");
  }
  try {
    await client.projectSearch("no-such-project", searchQuery(q));
  } catch (error) {
    if (!(error instanceof ExampleError)) throw error;
    console.log(`refused: ${error.message}`);
  }
  // Read effective_neural_mode and warning: the actual technique may differ from the requested one.
  printSearch(
    await client.projectSearch(
      project,
      searchQuery(q, { ssapi_flags: { neural_mode: "EXACT_AND_BM25_FUSED" } }),
    ),
  );
}
