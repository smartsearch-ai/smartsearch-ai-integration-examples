/**
 * Problem: The query matches broadly, but each movie overview must also mention a chosen word.
 *
 * Step 11: Words anywhere in a text field.
 * Run: npm run example -- filter_by_full_text_match "love" paris
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Public Core project search uses the service identity, not delegated Workplace access.
 * Try it: run the request, read the hits, then change only the option this step teaches.
 * Expected: hits=<count>, actual mode and warning, followed by returned document fields.
 * If refused: check project assignment and schema; an empty list is a valid search outcome.
 */
import { connect, require, printSearch } from "../common.js";
import { searchQuery } from "../contracts.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    project = require("SMARTSEARCH_PROJECT_ID");
  const q = args[0] ?? "love";
  // The helper sends POST /core/projects/{projectId}/search with this SSPL JSON.
  const request = searchQuery(q, {
    response_fields: ["title", "overview"],
    filters: {
      all: [
        { search_type: "match", field: "overview", value: args[1] ?? "paris" },
      ],
    },
  });
  console.log("request:", JSON.stringify(request));
  printSearch(await client.projectSearch(project, request));
}
