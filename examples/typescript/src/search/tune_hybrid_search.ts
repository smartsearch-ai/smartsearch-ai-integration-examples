/**
 * Problem: Two retrieval lists need combining; compare rank fusion with normalized score fusion.
 *
 * Step 23: How keyword and semantic results are fused.
 * Run: npm run example -- tune_hybrid_search [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Requires embeddings; check actual retrieval mode before comparing fusion.
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
  for (const flags of [
    {
      neural_mode: "A_KNN_AND_BM25",
      normalization_technique: "RANK",
      normalization_combination_technique: "RRF",
      neural_rank_window_size: 50,
      neural_rank_constant: 60,
    },
    {
      neural_mode: "A_KNN_AND_BM25",
      normalization_technique: "MIN_MAX",
      normalization_combination_technique: "ARITHMETIC_MEAN",
    },
  ] as const) {
    console.log(
      `Fusion: ${flags.normalization_technique} + ${flags.normalization_combination_technique}`,
    );
    printSearch(
      await client.projectSearch(
        project,
        searchQuery(q, { ssapi_flags: flags }),
      ),
    );
  }
}
