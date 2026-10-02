/**
 * Problem: Related-word expansion is too broad for this request; compare the project default with expansion off.
 *
 * Step 20: Stop related-word expansion for one request.
 * Run: npm run example -- turn_off_query_expansion [query]
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

  for (const disabled of [false, true]) {
    const request = searchQuery(q, {
      ssapi_flags: disabled
        ? { query_expansion_enable: false, neural_mode: "BM25" }
        : { neural_mode: "BM25" },
    });
    console.log("request:", JSON.stringify(request));
    printSearch(await client.projectSearch(project, request));
  }
}
