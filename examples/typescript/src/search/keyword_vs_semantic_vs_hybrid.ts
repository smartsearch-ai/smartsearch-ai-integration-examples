/**
 * Problem: Does exact wording or meaning work better for this query? Compare keyword, vector and hybrid retrieval.
 *
 * Step 22: The three search techniques, and checking which one ran.
 * Run: npm run example -- keyword_vs_semantic_vs_hybrid [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Semantic/hybrid modes require embeddings; read actual mode and warning for fallback.
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
  const modes = ["BM25", "A_KNN", "A_KNN_AND_BM25"] as const;
  for (const mode of modes) {
    const request = searchQuery(q, { ssapi_flags: { neural_mode: mode } });
    console.log("request:", JSON.stringify(request));
    printSearch(await client.projectSearch(project, request));
  }
}
