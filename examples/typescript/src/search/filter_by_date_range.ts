/**
 * Problem: A year picker should keep movies released during one year.
 *
 * Step 9: Date ranges.
 * Run: npm run example -- filter_by_date_range [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Movies release_date accepts whole-year bounds here; ask your administrator for your schema format.
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
    response_fields: ["title", "release_date"],
    filters: {
      all: [
        {
          search_type: "range",
          field: "release_date",
          condition: { gte: 2000, lt: 2001 },
        },
      ],
    },
  });
  console.log("request:", JSON.stringify(request));
  printSearch(await client.projectSearch(project, request));
}
