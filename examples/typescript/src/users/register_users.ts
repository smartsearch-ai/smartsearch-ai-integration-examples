/**
 * Problem: Create two users from your identity provider without managing SmartSearch passwords.
 *
 * Step 40: Register users from your system, linked to your identity provider.
 * Run: npm run example -- register_users [query]
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
  // UPSERT_USERS registers demo external identities; email alone never links existing users.
  await runRegistration(connect(), false);
}
