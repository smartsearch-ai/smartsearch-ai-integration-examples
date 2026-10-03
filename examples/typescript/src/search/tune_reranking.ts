/**
 * Problem: Control how many candidates the model rescores and whether low-scoring matches survive.
 *
 * Step 26: How many candidates to rerank; a minimum score.
 * Run: npm run example -- tune_reranking [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Requires a configured reranker; tuning cannot install a missing model.
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
  for (const size of [10, 50])
    printSearch(
      await client.projectSearch(
        project,
        searchQuery(q, {
          ssapi_flags: { rerank_enabled: true, rerank_first_stage_size: size },
        }),
      ),
    );
  printSearch(
    await client.projectSearch(
      project,
      searchQuery(q, {
        ssapi_flags: {
          rerank_enabled: true,
          rerank_first_stage_size: 30,
          rerank_min_score: 0.5,
        },
      }),
    ),
  );
}
