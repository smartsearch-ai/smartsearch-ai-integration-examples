/**
 * Problem: Users selected a language or genre; keep only documents with that exact value.
 *
 * Step 6: Yes/no conditions on an exact value, including properties inside lists.
 * Run: npm run example -- filter_by_exact_value [query]
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
  const variants = ["original_language", "genres.name"] as const;
  for (const field of variants) {
    const request = searchQuery(q, {
      filters: {
        all: [
          {
            search_type: "term",
            field,
            value: field === "genres.name" ? "Comedy" : "fr",
          },
        ],
      },
    });
    console.log("request:", JSON.stringify(request));
    printSearch(await client.projectSearch(project, request));
  }
}
