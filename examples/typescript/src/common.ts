import { ExampleClient, ExampleError } from "./client.js";
import { object, type ObjectBody, type EnsureItem } from "./contracts.js";
/** Safe settings error identifies the missing variable without exposing its value. */
export class ConfigurationError extends Error {
  /** @param name - Safe environment variable name; values are never included. */
  constructor(name: string) {
    super(
      /^SMARTSEARCH_[A-Z0-9_]+$/.test(name)
        ? `Environment variable ${name} is not set (see .env.example)`
        : "Missing required setting",
    );
  }
}
/** Reads a nonblank setting from the process environment.
 * @param name - Environment variable name from the root .env.example.
 * @returns Trimmed setting value.
 * @throws Error if the setting is missing or blank.
 */
export function require(name: string): string {
  const value = process.env[name]?.trim();
  if (!value) throw new ConfigurationError(name);
  return value;
}
/** Constructs the server-side example client; authentication starts on its first request.
 * @returns Client restricted to the public calls taught here.
 * @throws Error when required settings or URLs are invalid.
 */
export function connect(): ExampleClient {
  return new ExampleClient({
    apiUrl: require("SMARTSEARCH_API_BASE_URL"),
    adminUrl: require("SMARTSEARCH_ADMIN_BASE_URL"),
    authUrl: require("SMARTSEARCH_AUTH_BASE_URL"),
    realm: require("SMARTSEARCH_REALM"),
    clientId: require("SMARTSEARCH_CLIENT_ID"),
    clientSecret: require("SMARTSEARCH_CLIENT_SECRET"),
  });
}
/** Joins command-line query words while preserving quoted arguments.
 * @param args - CLI argument values.
 * @param fallback - Query used when no arguments are supplied.
 * @returns Query text.
 */
export function queryText(args: string[], fallback: string): string {
  return args.length ? args.join(" ") : fallback;
}
/** Reads a JSON array of objects for result printing.
 * @param value - An untrusted response field.
 * @returns Validated object entries, or an empty list when no array is supplied.
 * @throws Error if an array contains a non-object entry.
 */
export function records(value: unknown): ObjectBody[] {
  return Array.isArray(value) ? value.map(object) : [];
}
/** Shows received hits, actual retrieval mode and warning; counts are not a total-match estimate.
 * @param body - Successful Core response envelope.
 */
export function printSearch(body: ObjectBody): void {
  const result = body.result === undefined ? {} : object(body.result);
  const hits =
    result.hits === undefined ? [] : records(object(result.hits).hits);
  console.log(
    `hits=${hits.length} mode=${String(body.effective_neural_mode ?? "unspecified")} warning=${String(body.warning ?? "none")}`,
  );
  for (const hit of hits)
    console.log(
      JSON.stringify(hit._source ?? {}),
      ...(hit.highlight ? [JSON.stringify(hit.highlight)] : []),
    );
}
/** Shows the permitted documents returned by Workplace retrieval.
 * @param body - Direct Workplace response body.
 */
export function printDocuments(body: ObjectBody): void {
  const documents = records(body.documents);
  console.log(`documents=${documents.length}`);
  for (const item of documents) console.log(String(item.title ?? "Untitled"));
}
/** Prints retrieval-only evidence without implying an answer was generated.
 * @param body - Direct Workplace query response containing sources.
 */
export function printSources(body: ObjectBody): void {
  const sources = records(body.sources);
  console.log(`sources=${sources.length}`);
  for (const source of sources) console.log(String(source.title ?? "Untitled"));
}
/** Shows the final answer together with its source titles.
 * @param body - Direct Workplace response body or completed SSE payload.
 */
export function printAnswer(body: ObjectBody): void {
  console.log(String(body.answer ?? ""));
  const sources = records(body.sources);
  console.log(`sources=${sources.length}`);
  for (const source of sources) console.log(String(source.title ?? "Untitled"));
}
const FINISHED = new Set([
  "SUCCEEDED",
  "PARTIAL",
  "FAILED",
  "RECONCILIATION_REQUIRED",
  "CANCELLED",
]);
/** Polls registration until a terminal job state; never treats a timeout as completion.
 * @param client - Client exposing only getJob for polling.
 * @param id - Job ID returned by submitJob.
 * @param timeoutMs - Polling window in milliseconds; default 60 seconds.
 * @returns Terminal job body; inspect item outcomes for PARTIAL/FAILED states.
 * @throws ExampleError if the polling deadline passes or a request fails.
 */
export async function waitForJob(
  client: Pick<ExampleClient, "getJob">,
  id: string,
  timeoutMs = 60000,
): Promise<ObjectBody> {
  const deadline = performance.now() + timeoutMs;
  let job = await client.getJob(id);
  while (!FINISHED.has(String(job.state)) && performance.now() < deadline) {
    await new Promise((resolve) =>
      setTimeout(
        resolve,
        Math.min(1000, Math.max(0, deadline - performance.now())),
      ),
    );
    job = await client.getJob(id);
  }
  if (!FINISHED.has(String(job.state)))
    throw new ExampleError(0, "PROVISIONING_JOB_TIMEOUT");
  return job;
}
/** Submits the small federated registration demo and reports job/item outcomes.
 * @param client - Service client with provisioning permission.
 * @param onboard - Whether to add workspace membership and a source grant.
 * @returns Resolves after the job and item outcomes are shown.
 * @throws ExampleError if registration, polling or outcome reads fail.
 */
export async function runRegistration(
  client: ExampleClient,
  onboard: boolean,
): Promise<void> {
  const makeUser = (n: number, first: string): EnsureItem => {
    const id = `sdk-example-user-${n}`;
    return {
      item_key: `user-${n}`,
      external_user_id: id,
      profile: {
        email: `${id}@example.com`,
        first_name: first,
        last_name: "Example",
        display_name: `${first} Example`,
      },
      platform_role: "GUEST",
      permissions: [],
      external_identity: { subject: id, username: id },
      workspaces: [],
    };
  };
  const users = onboard
    ? [makeUser(1, "Ada")]
    : [makeUser(1, "Ada"), makeUser(2, "Grace")];
  if (onboard)
    users[0]!.workspaces = [
      {
        workspace_id: require("SMARTSEARCH_WORKSPACE_ID"),
        source_grants: [
          {
            source_id: require("SMARTSEARCH_SOURCE_ID"),
            expected_revision: 0,
            grants: {
              groups: [
                process.env.SMARTSEARCH_GRANT_GROUP?.trim() || "everyone",
              ],
              roles: [],
              security_keys: [],
            },
          },
        ],
      },
    ];
  const tenant = require("SMARTSEARCH_TENANT_ID"),
    integration = require("SMARTSEARCH_INTEGRATION_ID");
  // Same key and body return the original job. Change the key when changing the demo users/grants.
  const submitted = await client.submitJob(
    {
      scope: { kind: "TENANT", id: tenant },
      integration_id: integration,
      kind: onboard ? "ONBOARD_USERS" : "UPSERT_USERS",
      items: users,
    },
    onboard ? "sdk-example-onboard-users-v1" : "sdk-example-upsert-users-v1",
  );
  if (typeof submitted.job_id !== "string")
    throw new Error("No provisioning job ID");
  const done = await waitForJob(client, submitted.job_id);
  console.log(`Job finished: ${String(done.state)}`);
  for (const item of records(
    (await client.listJobItems(submitted.job_id)).values,
  ))
    console.log(
      `item=${String(item.item_key)} state=${String(item.state)} error=${String(item.error_code ?? "none")}`,
    );
  if (!onboard)
    for (const item of records(
      (await client.listPrincipals(integration, tenant)).values,
    ))
      console.log(
        `${String(item.external_user_id)} -> ${String(item.principal_id)}`,
      );
}
