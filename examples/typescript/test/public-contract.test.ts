import assert from "node:assert/strict";
import { test } from "node:test";
import {
  createServer,
  type IncomingMessage,
  type ServerResponse,
} from "node:http";
import { once } from "node:events";
import { execFileSync } from "node:child_process";
import { createPublicKey, verify, type JsonWebKey } from "node:crypto";
import { inspect } from "node:util";
import { ExampleClient, ExampleError, type Connection } from "../src/client.js";
import {
  searchQuery,
  projectBody,
  workspaceBody,
  jobBody,
  object,
  type SubmitJob,
} from "../src/contracts.js";
import { waitForJob } from "../src/common.js";
import { examples } from "../src/registry.js";
import { createAssertion } from "../src/users/create_user_assertion.js";
interface Seen {
  url: string;
  method: string;
  body: string;
  auth: string | undefined;
  idempotency: string | undefined;
}
async function mock(
  handler: (
    request: IncomingMessage,
    response: ServerResponse,
    seen: Seen[],
  ) => Promise<void> | void,
  run: (
    client: ExampleClient,
    seen: Seen[],
    config: Connection,
  ) => Promise<void>,
  timeoutMs = 5000,
): Promise<void> {
  const seen: Seen[] = [];
  const server = createServer(async (request, response) => {
    let body = "";
    for await (const part of request) body += String(part);
    seen.push({
      url: request.url!,
      method: request.method!,
      body,
      auth: request.headers.authorization,
      idempotency:
        typeof request.headers["idempotency-key"] === "string"
          ? request.headers["idempotency-key"]
          : undefined,
    });
    if (request.url?.endsWith("/token")) {
      response.setHeader("Content-Type", "application/json");
      response.end(
        JSON.stringify({
          access_token: "fake-safe-token",
          expires_in: 120,
          token_type: "Bearer",
        }),
      );
      return;
    }
    try {
      await handler(request, response, seen);
    } catch {
      response.statusCode = 500;
      response.end("{}");
    }
  });
  server.listen(0, "127.0.0.1");
  await once(server, "listening");
  const address = server.address();
  assert.ok(address && typeof address !== "string");
  const origin = `http://127.0.0.1:${address.port}`;
  const config = {
    apiUrl: `${origin}/api`,
    adminUrl: `${origin}/admin`,
    authUrl: `${origin}/auth`,
    realm: "example",
    clientId: "svc-example",
    clientSecret: "fake-secret",
    timeoutMs,
  };
  try {
    await run(new ExampleClient(config), seen, config);
  } finally {
    server.closeAllConnections();
    await new Promise<void>((resolve) => server.close(() => resolve()));
  }
}
function reply(response: ServerResponse, data: unknown): void {
  response.setHeader("Content-Type", "application/json");
  response.end(JSON.stringify(data));
}
const job: SubmitJob = {
  scope: { kind: "TENANT", id: "example-tenant" },
  integration_id: "example-integration",
  kind: "UPSERT_USERS",
  items: [
    {
      item_key: "user-1",
      external_user_id: "sdk-example-user-1",
      profile: {
        email: "demo@example.com",
        first_name: "Ada",
        last_name: "Example",
        display_name: "Ada Example",
      },
      platform_role: "GUEST",
      permissions: [],
      workspaces: [],
      external_identity: { subject: "demo-subject", username: "demo-user" },
    },
  ],
};

test("public routes, SSPL JSON, array multi-search, admin origin and service OAuth form", async () => {
  await mock(
    (_, response) =>
      reply(response, { code: 1, result: { hits: { hits: [] } } }),
    async (client, seen) => {
      const query = searchQuery("star wars", {
        filters: {
          all: [
            {
              any: [
                { search_type: "term", field: "genres.name", value: "Comedy" },
              ],
            },
          ],
        },
        ssapi_flags: { rerank_enabled: false, precision: 6 },
      });
      await Promise.all([
        client.projectSearch("project/a", query),
        client.usecaseSearch("usecase", query),
      ]);
      await client.multiSearch("project", [query]);
      await client.capabilities();
      await client.submitJob(job, "demo-key");
      await client.getJob("job-1");
      await client.listJobItems("job-1");
      await client.listPrincipals("example-integration", "example tenant");
      assert.equal(
        seen.filter((v) => v.url.endsWith("/token")).length,
        1,
        "concurrent requests share a token",
      );
      const form = new URLSearchParams(seen[0]!.body);
      assert.equal(form.get("grant_type"), "client_credentials");
      assert.equal(form.get("client_secret"), "fake-secret");
      assert.ok(
        seen.some((v) => v.url === "/api/core/projects/project%2Fa/search"),
      );
      assert.ok(
        seen.some((v) => v.url === "/api/core/usecases/usecase/search"),
      );
      assert.deepEqual(
        JSON.parse(seen.find((v) => v.url.endsWith("/mSearch"))!.body),
        [query],
      );
      assert.ok(
        seen.some(
          (v) =>
            v.url === "/admin/search-admin/api/provisioning/v1/capabilities",
        ),
      );
      const submitted = seen.find(
        (v) => v.url === "/admin/search-admin/api/provisioning/v1/jobs",
      )!;
      assert.equal(submitted.idempotency, "demo-key");
      assert.deepEqual(JSON.parse(submitted.body), job);
      assert.ok(seen.some((v) => v.url.endsWith("/items?limit=100")));
      assert.ok(
        seen.some((v) =>
          v.url.includes(
            "scope_kind=TENANT&scope_id=example%20tenant&limit=100",
          ),
        ),
      );
      for (const request of seen.slice(1))
        assert.equal(request.auth, "Bearer fake-safe-token");
      assert.ok(!inspect(client).includes("fake-secret"));
    },
  );
});

test("closed projection removes unexpected keys at every nesting level; object scalars reject", () => {
  const query = searchQuery("demo", {
    ssapi_flags: { precision: 5 },
    filters: {
      all: [
        { search_type: "range", field: "vote_average", condition: { gte: 7 } },
      ],
    },
  });
  Object.assign(query, { unexpected: "do-not-forward" });
  Object.assign(query.ssapi_flags!, { unexpected: "do-not-forward" });
  Object.assign(query.filters!.all![0]!, { unexpected: "do-not-forward" });
  assert.ok(!JSON.stringify(projectBody(query)).includes("do-not-forward"));
  const workspace = {
    query: "demo",
    options: { include_sources: true, unexpected: "do-not-forward" },
    unexpected: "do-not-forward",
  };
  assert.ok(
    !JSON.stringify(workspaceBody(workspace)).includes("do-not-forward"),
  );
  const pollutedJob = structuredClone(job);
  Object.assign(pollutedJob.items[0]!.profile, {
    unexpected: "do-not-forward",
  });
  assert.ok(!JSON.stringify(jobBody(pollutedJob)).includes("do-not-forward"));
  const invalid = searchQuery("demo");
  Object.assign(invalid, { ssapi_flags: { precision: { unexpected: true } } });
  assert.throws(() => projectBody(invalid));
  assert.throws(() => searchQuery("demo", { response_fields: ["*"] }));
  assert.throws(() => searchQuery("demo", { ssapi_flags: { precision: 12 } }));
});

test("Workplace search/query/chat use direct bodies and standard memory; source filter is business data", async () => {
  await mock(
    (_, response) =>
      reply(response, {
        documents: [],
        answer: "demo",
        session_id: "session-1",
      }),
    async (client, seen) => {
      const body = {
        query: "policy",
        mode: "answer" as const,
        memory_mode: "standard" as const,
        source_ids: ["demo-source"],
        filters: {
          all: [
            {
              search_type: "match" as const,
              field: "title" as const,
              value: "policy",
            },
          ],
        },
      };
      const result = await client.workplaceSearch("workspace", body);
      assert.deepEqual(result.documents, []);
      await client.workplaceQuery("workspace", body);
      await client.workplaceChat("workspace", {
        ...body,
        session_id: "session-1",
      });
      assert.equal(
        seen[1]!.url,
        "/api/workspace/v1/workspaces/workspace/search",
      );
      assert.deepEqual(JSON.parse(seen[2]!.body), body);
      assert.equal(JSON.parse(seen[3]!.body).session_id, "session-1");
    },
  );
});

test("JWT grant and token exchange include exact fields; delegated token never refreshes or calls Core", async () => {
  await mock(
    (request, response) => {
      if (request.url?.endsWith("/query")) {
        response.statusCode = 401;
        reply(response, { code: "DENIED", message: "fake-secret" });
      } else reply(response, { documents: [] });
    },
    async (client, seen) => {
      const user = await client.asUserFromToken("fake-user-token");
      await user.search("workspace", { query: "policy" });
      const form = new URLSearchParams(seen[0]!.body);
      assert.equal(form.get("subject_token"), "fake-user-token");
      assert.deepEqual(form.getAll("audience"), [
        "workspace-api",
        "cloud-gateway",
        "search-admin",
      ]);
      assert.equal(
        form.get("requested_token_type"),
        "urn:ietf:params:oauth:token-type:access_token",
      );
      const other = await client.asUser("fake-user-assertion");
      const assertionForm = new URLSearchParams(seen[2]!.body);
      assert.equal(
        assertionForm.get("grant_type"),
        "urn:ietf:params:oauth:grant-type:jwt-bearer",
      );
      assert.equal(assertionForm.get("assertion"), "fake-user-assertion");
      await assert.rejects(
        other.query("workspace", { query: "demo" }),
        (error: unknown) =>
          error instanceof ExampleError &&
          !error.message.includes("fake-secret"),
      );
      assert.equal(
        seen.filter((v) => v.url.endsWith("/token")).length,
        2,
        "delegated 401 is terminal",
      );
      assert.ok(!("projectSearch" in user));
      assert.ok(!inspect(user).includes("fake-user-token"));
    },
  );
});

test("service 401 refreshes once; errors and redirect failures redact credentials and bodies", async () => {
  let requests = 0;
  await mock(
    (_, response) => {
      requests++;
      response.statusCode = 401;
      reply(response, { code: "REQUEST_DENIED", message: "fake-secret" });
    },
    async (client, seen) => {
      await assert.rejects(
        client.capabilities(),
        (error) =>
          error instanceof ExampleError &&
          error.code === "REQUEST_DENIED" &&
          !inspect(error).includes("fake-secret"),
      );
      assert.equal(requests, 2);
      assert.equal(seen.filter((v) => v.url.endsWith("/token")).length, 2);
    },
  );
  await mock(
    (_, response) => {
      response.statusCode = 302;
      response.setHeader("Location", "https://elsewhere.example.com");
      response.end();
    },
    async (client) => {
      await assert.rejects(
        client.capabilities(),
        (error) =>
          error instanceof ExampleError && error.code === "TRANSPORT_FAILED",
      );
    },
  );
});

test("SSE fragments, CRLF boundaries, multi-line data, split Unicode and chat session retention", async () => {
  await mock(
    async (_, response) => {
      response.setHeader("Content-Type", "text/event-stream");
      const bytes = Buffer.from(
        ': keepalive\r\nevent: run.started\r\ndata: {"payload": {"session_id": "session-1"}}\r\n\r\n' +
          'event: answer.delta\r\ndata: {"payload":\r\ndata: {"text":"café ☕"}}\r\n\r\n' +
          'event: run.completed\r\ndata: {"payload":{"status":"COMPLETED","answer":"café ☕","sources":[]}}\r\n\r\n',
      );
      for (let i = 0; i < bytes.length; i++) {
        response.write(bytes.subarray(i, i + 1));
        await new Promise((resolve) => setTimeout(resolve, 1));
      }
      response.end();
    },
    async (client, seen) => {
      const events = [];
      for await (const event of client.streamChat("workspace", {
        query: "policy",
      }))
        events.push(event);
      assert.equal(events.length, 3);
      assert.equal(events[1]!.payload.text, "café ☕");
      assert.equal(events[2]!.payload.session_id, "session-1");
      assert.equal(JSON.parse(seen[1]!.body).options.stream, true);
    },
  );
});

test("SSE EOF, malformed JSON, failure event and invalid completion are failures", async () => {
  for (const frame of [
    'event: answer.delta\ndata: {"payload":{"text":"unfinished"}}\n\n',
    "event: run.completed\ndata: invalid\n\n",
    'event: run.failed\ndata: {"payload":{"message":"fake-secret"}}\n\n',
    'event: run.completed\ndata: {"payload":{"status":"FAILED"}}\n\n',
  ])
    await mock(
      (_, response) => {
        response.setHeader("Content-Type", "text/event-stream");
        response.end(frame);
      },
      async (client) => {
        await assert.rejects(
          async () => {
            for await (const event of client.streamQuery("workspace", {
              query: "demo",
            }))
              void event;
          },
          (error) =>
            error instanceof ExampleError &&
            !error.message.includes("fake-secret"),
        );
      },
    );
});

test("bounded JSON and SSE timeouts fail safely", async () => {
  await mock(
    (_, response) => {
      response.end("x".repeat(2 * 1024 * 1024 + 1));
    },
    async (client) => {
      await assert.rejects(
        client.capabilities(),
        (error) =>
          error instanceof ExampleError && error.code === "INVALID_RESPONSE",
      );
    },
  );
  await mock(
    (_, response) => {
      response.setHeader("Content-Type", "text/event-stream");
      response.flushHeaders();
    },
    async (client) => {
      await assert.rejects(
        async () => {
          for await (const event of client.streamQuery("workspace", {
            query: "demo",
          }))
            void event;
        },
        (error) => error instanceof ExampleError,
      );
    },
    40,
  );
});

test("polling never labels a nonterminal job finished", async () => {
  for (const state of [
    "SUCCEEDED",
    "PARTIAL",
    "FAILED",
    "RECONCILIATION_REQUIRED",
    "CANCELLED",
  ])
    assert.equal(
      (await waitForJob({ getJob: async () => ({ state }) }, "demo", 0)).state,
      state,
    );
  await assert.rejects(
    waitForJob({ getJob: async () => ({ state: "RUNNING" }) }, "demo", 0),
    (error) =>
      error instanceof ExampleError &&
      error.code === "PROVISIONING_JOB_TIMEOUT",
  );
});

test("RS256 assertion verifies, expires in 120 seconds, contains no private JWKS data; CLI prints no JWT", async () => {
  const previousAuth = process.env.SMARTSEARCH_AUTH_BASE_URL,
    previousRealm = process.env.SMARTSEARCH_REALM;
  process.env.SMARTSEARCH_AUTH_BASE_URL =
    "https://auth.your-company.example.com";
  process.env.SMARTSEARCH_REALM = "example";
  try {
    const result = createAssertion("sdk-example-user-1");
    const parts = result.assertion.split(".");
    const key = object((result.jwks.keys as unknown[])[0]);
    assert.ok(!("d" in key));
    assert.equal(Number(result.claims.exp) - Number(result.claims.iat), 120);
    assert.equal(
      result.claims.aud,
      "https://auth.your-company.example.com/realms/example",
    );
    assert.ok(
      verify(
        "RSA-SHA256",
        Buffer.from(`${parts[0]}.${parts[1]}`),
        createPublicKey({ key: key as JsonWebKey, format: "jwk" }),
        Buffer.from(parts[2]!, "base64url"),
      ),
    );
    const output = execFileSync(
      process.execPath,
      ["dist/src/run.js", "create_user_assertion"],
      { encoding: "utf8" },
    );
    assert.ok(!/eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\./u.test(output));
    assert.ok(!output.includes("PRIVATE KEY"));
  } finally {
    if (previousAuth === undefined)
      delete process.env.SMARTSEARCH_AUTH_BASE_URL;
    else process.env.SMARTSEARCH_AUTH_BASE_URL = previousAuth;
    if (previousRealm === undefined) delete process.env.SMARTSEARCH_REALM;
    else process.env.SMARTSEARCH_REALM = previousRealm;
  }
});

test("all 44 modules load and CLI help/list run without credentials or network", async () => {
  assert.equal(examples.length, 44);
  assert.equal(new Set(examples.map((v) => v.name)).size, 44);
  for (const example of examples) {
    assert.equal(typeof (await example.load()).main, "function");
    const output = execFileSync(
      process.execPath,
      ["dist/src/run.js", example.name, "--help"],
      { env: { PATH: process.env.PATH }, encoding: "utf8" },
    );
    assert.ok(output.includes(example.name));
  }
  assert.ok(
    execFileSync(process.execPath, ["dist/src/run.js", "--list"], {
      env: { PATH: process.env.PATH },
      encoding: "utf8",
    }).includes("44."),
  );
});

test("HTTP success cannot hide a project application failure", async () => {
  await mock(
    (_, response) => reply(response, { code: 0, message: "fake-secret" }),
    async (client) => {
      const query = searchQuery("demo");
      for (const call of [
        () => client.projectSearch("project", query),
        () => client.usecaseSearch("usecase", query),
        () => client.multiSearch("project", [query]),
      ])
        await assert.rejects(
          call(),
          (error) =>
            error instanceof ExampleError &&
            error.code === "SEARCH_APPLICATION_FAILED" &&
            !error.message.includes("fake-secret"),
        );
    },
  );
});

test("CR-only SSE frames complete successfully", async () => {
  await mock(
    (_, response) => {
      response.setHeader("Content-Type", "text/event-stream");
      response.end(
        'event: run.completed\rdata: {"payload":{"status":"COMPLETED"}}\r\r',
      );
    },
    async (client) => {
      let completed = false;
      for await (const event of client.streamQuery("workspace", {
        query: "demo",
      }))
        completed = event.event === "run.completed";
      assert.ok(completed);
    },
  );
});

test("all 44 teaching examples execute against offline public HTTP fixtures", async () => {
  await mock(
    (request, response, seen) => {
      const last = seen.at(-1)!;
      const body = last.body
        ? (JSON.parse(last.body) as Record<string, unknown>)
        : {};
      if (object(body.options ?? {}).stream === true) {
        response.setHeader("Content-Type", "text/event-stream");
        response.end(
          'event: run.completed\ndata: {"payload":{"status":"COMPLETED","answer":"Demo answer","sources":[]}}\n\n',
        );
        return;
      }
      if (request.url?.includes("/core/")) {
        if (request.url.includes("no-such-project")) {
          response.statusCode = 404;
          reply(response, { code: "PROJECT_NOT_FOUND" });
          return;
        }
        const hits = { hits: [{ _source: { title: "Example movie" } }] };
        reply(response, {
          code: 1,
          effective_neural_mode: "BM25",
          result: {
            hits,
            responses: [{ hits }, { hits }, { hits }],
            aggregations: {
              languages: { buckets: [{ key: "en", doc_count: 1 }] },
              distinct_genres: { value: 1 },
            },
          },
        });
        return;
      }
      if (request.url?.endsWith("/jobs")) {
        reply(response, { job_id: "demo-job", state: "QUEUED" });
        return;
      }
      if (request.url?.endsWith("/demo-job")) {
        reply(response, { state: "SUCCEEDED" });
        return;
      }
      reply(response, {
        documents: [],
        sources: [],
        values: [],
        answer: "Demo answer",
        session_id: "demo-session",
        status: "COMPLETED",
        result: { kinds: ["UPSERT_USERS"] },
      });
    },
    async (_client, seen, config) => {
      const variables: Record<string, string> = {
        SMARTSEARCH_API_BASE_URL: config.apiUrl,
        SMARTSEARCH_ADMIN_BASE_URL: config.adminUrl,
        SMARTSEARCH_AUTH_BASE_URL: config.authUrl,
        SMARTSEARCH_REALM: config.realm,
        SMARTSEARCH_CLIENT_ID: config.clientId,
        SMARTSEARCH_CLIENT_SECRET: config.clientSecret,
        SMARTSEARCH_PROJECT_ID: "demo-project",
        SMARTSEARCH_USECASE_ID: "demo-usecase",
        SMARTSEARCH_WORKSPACE_ID: "demo-workspace",
        SMARTSEARCH_SOURCE_ID: "demo-source",
        SMARTSEARCH_TENANT_ID: "demo-tenant",
        SMARTSEARCH_INTEGRATION_ID: "demo-integration",
        SMARTSEARCH_USER_ACCESS_TOKEN: "fake-user-token",
        SMARTSEARCH_USER_ASSERTION: "fake-user-assertion",
      };
      const original = { ...process.env };
      const log = console.log;
      Object.assign(process.env, variables);
      console.log = () => {};
      try {
        for (const example of examples) await (await example.load()).main([]);
        assert.ok(
          seen.length > 80,
          "examples perform substantive distinct public calls",
        );
        assert.ok(
          seen.every((v) =>
            /^\/(auth\/realms\/example\/protocol\/openid-connect\/token|api\/(core\/(projects|usecases)\/[^/]+\/(search|mSearch)|workspace\/v1\/workspaces\/[^/]+\/(search|query|chat))|admin\/search-admin\/api\/provisioning\/v1\/(capabilities|jobs(?:\/[^/?]+(?:\/items)?)?|integrations\/[^/]+\/principals))(?:\?.*)?$/u.test(
              v.url,
            ),
          ),
          "every request stays within curated routes",
        );
      } finally {
        console.log = log;
        for (const key of Object.keys(variables))
          if (original[key] === undefined) delete process.env[key];
          else process.env[key] = original[key];
      }
    },
  );
});

test("nested Workplace code and missing setting guidance are safe and actionable", async () => {
  await mock(
    (_, response) => {
      response.statusCode = 403;
      reply(response, {
        error: { code: "SCOPE_DENIED", message: "fake-secret" },
      });
    },
    async (client) => {
      await assert.rejects(
        client.workplaceSearch("workspace", { query: "demo" }),
        (error) =>
          error instanceof ExampleError &&
          error.code === "SCOPE_DENIED" &&
          !error.message.includes("fake-secret"),
      );
    },
  );
  try {
    execFileSync(process.execPath, ["dist/src/run.js", "first_search"], {
      env: { PATH: process.env.PATH },
      encoding: "utf8",
      stdio: "pipe",
    });
    assert.fail("Missing config must fail");
  } catch (error) {
    assert.ok(
      String((error as { stderr: string }).stderr).includes(
        "SMARTSEARCH_API_BASE_URL",
      ),
    );
  }
});
