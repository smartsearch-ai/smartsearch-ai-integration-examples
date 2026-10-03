/**
 * Problem: Your results page needs a second and third page without reranking ignoring the offset.
 *
 * Step 4: Paging with `from_` and `size` (reranking off).
 * Run: npm run example -- page_through_results [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
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

  for (const page of [0, 1, 2]) {
    const request = searchQuery(q, {
      from: page * 5,
      ssapi_flags: { neural_mode: "BM25", rerank_enabled: false },
    });
    console.log("request:", JSON.stringify(request));
    printSearch(await client.projectSearch(project, request));
  }
}
