/**
 * Problem: Balance semantic candidate depth against the number of closest matches you keep.
 *
 * Step 24: How many close-in-meaning documents to consider and keep.
 * Run: npm run example -- limit_semantic_matches [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Requires embeddings; request options cannot enable missing vector data.
 * Public Core project search uses the service identity, not delegated Workplace access.
 * Try it: run the request, read the hits, then change only the option this step teaches.
 * Expected: hits=<count>, actual mode and warning, followed by returned document fields.
 * If refused: check project assignment and schema; an empty list is a valid search outcome.
 */
import { connect, require, queryText, printSearch } from "../common.js";
import { searchQuery } from "../contracts.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    project = require("SMARTSEARCH_PROJECT_ID");
  const q = queryText(args, "star wars");

  for (const top of [3, 10]) {
    const request = searchQuery(q, {
      size: 10,
      ssapi_flags: {
        neural_mode: "A_KNN",
        neural_top_matches: top,
        neural_total_matches: 100,
      },
    });
    console.log("request:", JSON.stringify(request));
    printSearch(await client.projectSearch(project, request));
  }
}
