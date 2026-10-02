/**
 * Problem: You have a service key. Does Search Admin recognize it, and which registration jobs can it submit?
 *
 * Step 1: What a service key is; connect and check what the key may do.
 * Run: npm run example -- connect_and_check_access [query]
 * Expected: provisioning capabilities for this key. This does not prove project/workspace access.
 * If refused: check connection settings, service key and provisioning permission with your Owner.
 */
import { connect } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  void args;
  // GET /search-admin/api/provisioning/v1/capabilities on the ADMIN URL; first obtains a service token.
  console.log(JSON.stringify(await connect().capabilities(), null, 2));
}
