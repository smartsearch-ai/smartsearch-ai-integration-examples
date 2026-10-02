/**
 * Problem: Register a user and grant workspace/source access in one idempotent onboarding job.
 *
 * Step 41: Register a user and give workspace and source access.
 * Run: npm run example -- register_users_with_workspace_access [query]
 * Federated registration requires the Owner to enable Register users and its allowed grants.
 * Expected: a terminal job state and each user item outcome; PARTIAL means inspect each item.
 * If refused: check tenant, saved integration ID and federated identity-provider setup.
 */
import { connect, runRegistration } from "../common.js";

/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  void args;
  // ONBOARD_USERS also assigns workspace membership and a revision-0 source grant.
  await runRegistration(connect(), true);
}
