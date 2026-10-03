/**
 * Problem: Reuse a saved search configuration rather than choosing its options on every request.
 *
 * Step 29: Search with a saved configuration.
 * Run: npm run example -- search_through_use_case [query]
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
    usecase = require("SMARTSEARCH_USECASE_ID");
  const q = queryText(args, "star wars");
  // POST /core/usecases/{usecaseId}/search applies the saved use-case configuration.
  printSearch(await client.usecaseSearch(usecase, searchQuery(q)));
}
