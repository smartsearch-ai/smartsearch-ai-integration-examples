/**
 * Problem: Prefer a language without excluding relevant movies in other languages.
 *
 * Step 15: Move preferred documents up without removing others.
 * Run: npm run example -- boost_term_values [query]
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
  // The helper sends POST /core/projects/{projectId}/search with this SSPL JSON.
  const request = searchQuery(q, {
    ssapi_flags: { rerank_enabled: false },
    boosts: [
      {
        search_type: "term",
        field: "original_language",
        value: "en",
        weight: 3,
      },
    ],
  });
  console.log("request:", JSON.stringify(request));
  printSearch(await client.projectSearch(project, request));
}
