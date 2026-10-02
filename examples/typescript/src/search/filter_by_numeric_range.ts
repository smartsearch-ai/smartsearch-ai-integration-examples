/**
 * Problem: A rating slider should keep only movies inside its chosen bounds.
 *
 * Step 8: At least, at most, between.
 * Run: npm run example -- filter_by_numeric_range [query]
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
  const variants = [{ gte: 7.5 }, { gt: 5, lt: 6 }];
  for (const condition of variants) {
    const request = searchQuery(q, {
      response_fields: ["title", "vote_average"],
      filters: {
        all: [{ search_type: "range", field: "vote_average", condition }],
      },
    });
    console.log("request:", JSON.stringify(request));
    printSearch(await client.projectSearch(project, request));
  }
}
