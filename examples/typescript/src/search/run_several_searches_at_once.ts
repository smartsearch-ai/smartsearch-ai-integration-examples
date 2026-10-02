/**
 * Problem: Your page needs several independent result lists in one HTTP round trip.
 *
 * Step 28: Several searches in one round trip.
 * Run: npm run example -- run_several_searches_at_once [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Public Core project search uses the service identity, not delegated Workplace access.
 * Try it: run the request, read the hits, then change only the option this step teaches.
 * Expected: each query with its received/shown counts, followed by sliced titles.
 * If refused: check project assignment and schema; an empty list is a valid search outcome.
 */
import { connect, require, records } from "../common.js";
import { searchQuery, object } from "../contracts.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    project = require("SMARTSEARCH_PROJECT_ID");
  // POST /core/projects/{projectId}/mSearch: the body is a bare array, not an object wrapper.
  const queries = (args.length ? args : ["alien", "titanic", "toy story"]).map(
    (q) => searchQuery(q),
  );
  const result = await client.multiSearch(project, queries);
  // Each response has its own hits. The server may ignore per-query size; slice for display.
  for (const [index, response] of records(
    object(result.result).responses,
  ).entries()) {
    const hits = records(object(response.hits).hits);
    console.log(
      `query=${queries[index]?.q} received=${hits.length} showing=${Math.min(5, hits.length)}`,
    );
    for (const hit of hits.slice(0, 5))
      console.log(String(object(hit._source).title));
  }
}
