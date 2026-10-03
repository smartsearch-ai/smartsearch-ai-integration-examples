/**
 * Problem: Show language counts next to results and turn a selected facet into a filter.
 *
 * Step 21: Value counts, distinct counts, drill-down.
 * Run: npm run example -- facet_counts [query]
 * Requires SMARTSEARCH_PROJECT_ID with the Movies sample schema; adapt fields to your data.
 * Requires a project WITHOUT document security; secured projects refuse facets.
 * Public Core project search uses the service identity, not delegated Workplace access.
 * Try it: run the request, read the hits, then change only the option this step teaches.
 * Expected: facet buckets/counts, followed by hits after selecting the first language.
 * If refused: check project assignment and schema; an empty list is a valid search outcome.
 */
import {
  connect,
  require,
  queryText,
  printSearch,
  records,
} from "../common.js";
import { searchQuery, object } from "../contracts.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  const client = connect(),
    project = require("SMARTSEARCH_PROJECT_ID");
  const q = queryText(args, "star wars");
  const response = await client.projectSearch(
    project,
    searchQuery(q, {
      aggs: [
        {
          type: "terms",
          name: "languages",
          field: "original_language",
          size: 10,
        },
        { type: "cardinality", name: "distinct_genres", field: "genres.name" },
      ],
    }),
  );
  const aggregations = object(object(response.result).aggregations);
  console.log(JSON.stringify(aggregations));
  const first = records(object(aggregations.languages).buckets)[0];
  if (first)
    printSearch(
      await client.projectSearch(
        project,
        searchQuery(q, {
          filters: {
            all: [
              {
                search_type: "term",
                field: "original_language",
                value: String(first.key),
              },
            ],
          },
        }),
      ),
    );
}
