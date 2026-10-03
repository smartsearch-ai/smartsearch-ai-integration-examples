/**
 * Problem: Your backend needs to prove which signed-in external user it acts for.
 *
 * Step 43: Sign a short-lived user assertion for a user signed in to YOUR application.
 * Owner registers issuer + public JWKS and enables Act as users before --sign-in succeeds.
 * Run: npm run example -- create_user_assertion sdk-example-user-1 [--sign-in]
 * Expected: header, claims and PUBLIC JWKS; the assertion itself is never printed.
 * Try it: generate the demo, then register your stable public key before requesting sign-in.
 * If sign-in fails: check the registered issuer/JWKS, subject and Act as users permission.
 * Keep production keys in a key management service. Never send private keys/JWTs to browsers.
 */
import {
  createPrivateKey,
  createPublicKey,
  generateKeyPairSync,
  randomUUID,
  sign,
  type KeyObject,
} from "node:crypto";
import { connect, require, printDocuments } from "../common.js";
import type { ObjectBody } from "../contracts.js";
/** Builds a one-use RS256 assertion and public JWKS for the configured issuer.
 * @param subject - External user ID already registered and authenticated in your application.
 * @returns Credential JWT plus decoded claims and public key material; never log `assertion`.
 * @throws Error when the subject, required realm settings, PEM or RSA key is invalid.
 * @example
 * const result = createAssertion('sdk-example-user-1');
 * // Give result.assertion only to the token exchange; publish result.jwks.
 */
export function createAssertion(subject: string): {
  assertion: string;
  header: ObjectBody;
  claims: ObjectBody;
  jwks: ObjectBody;
} {
  if (!subject.trim()) throw new Error("Subject must not be blank");
  const pem = process.env.SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM;
  const key: KeyObject = pem
    ? createPrivateKey(pem)
    : generateKeyPairSync("rsa", { modulusLength: 2048 }).privateKey;
  if (
    key.asymmetricKeyType !== "rsa" ||
    (key.asymmetricKeyDetails?.modulusLength ?? 0) < 2048
  )
    throw new Error("Require RSA key of at least 2048 bits");
  const kid = process.env.SMARTSEARCH_ASSERTION_KEY_ID || "example-key-1";
  const header = { alg: "RS256", typ: "JWT", kid };
  const now = Math.floor(Date.now() / 1000);
  const claims = {
    iss:
      process.env.SMARTSEARCH_ASSERTION_ISSUER ||
      "https://login.your-company.example.com",
    sub: subject,
    aud: `${require("SMARTSEARCH_AUTH_BASE_URL").replace(/\/+$/u, "")}/realms/${require("SMARTSEARCH_REALM")}`,
    iat: now,
    exp: now + 120,
    jti: randomUUID(),
  };
  const encode = (value: ObjectBody): string =>
    Buffer.from(JSON.stringify(value)).toString("base64url");
  const unsigned = `${encode(header)}.${encode(claims)}`;
  const assertion = `${unsigned}.${sign("RSA-SHA256", Buffer.from(unsigned), key).toString("base64url")}`;
  const publicJwk = createPublicKey(key).export({ format: "jwk" });
  return {
    assertion,
    header,
    claims,
    jwks: {
      keys: [
        {
          kty: "RSA",
          use: "sig",
          alg: "RS256",
          kid,
          n: publicJwk.n,
          e: publicJwk.e,
        },
      ],
    },
  };
}
/** Runs this teaching step using environment settings; see the tutorial above.
 * @param args - Query words or positional inputs shown in the run command.
 * @returns Resolves after this example prints its results.
 * @throws `ExampleError` when a public request fails; configuration errors stop before use.
 */
export async function main(args: string[]): Promise<void> {
  if (!process.env.SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM)
    console.log("Throw-away demonstration key; not registered for sign-in.");
  const result = createAssertion(args[0] ?? "sdk-example-user-1");
  // Publish only PUBLIC key material. Never print the assertion or private key.
  console.log("header:", JSON.stringify(result.header));
  console.log("claims:", JSON.stringify(result.claims));
  console.log("Public JWKS:", JSON.stringify(result.jwks, null, 2));
  if (args[1] === "--sign-in") {
    const user = await connect().asUser(result.assertion);
    printDocuments(
      await user.search(require("SMARTSEARCH_WORKSPACE_ID"), {
        query: args[2] ?? "travel policy",
        memory_mode: "standard",
      }),
    );
  }
}
