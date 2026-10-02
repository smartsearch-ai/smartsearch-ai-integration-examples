/** A small server-side teaching client for ONLY the calls used by these examples.
 * It is not a supported TypeScript SDK. No generic URL/path method is public.
 * Contract source: public Python SDK _auth.py, search.py, workplace.py, provisioning.py.
 */
import { inspect } from "node:util";
import {
  object,
  segment,
  text,
  projectBody,
  workspaceBody,
  jobBody,
  type ObjectBody,
  type SearchQuery,
  type WorkspaceRequest,
  type SubmitJob,
  type StreamEvent,
} from "./contracts.js";
/** Server-side connection settings. Keep clientSecret in environment variables or a secret store. */
export interface Connection {
  apiUrl: string;
  adminUrl: string;
  authUrl: string;
  realm: string;
  clientId: string;
  clientSecret: string;
  timeoutMs?: number;
}
/** Safe structured failure: status and code only; raw response bodies and credentials are discarded. */
export class ExampleError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
  ) {
    super(`HTTP ${status} ${code}`);
  }
}
async function boundedJson(response: Response): Promise<unknown> {
  if (!response.body) throw new Error("Empty response");
  const reader = response.body.getReader();
  const chunks: Uint8Array[] = [];
  let size = 0;
  try {
    while (true) {
      const part = await reader.read();
      if (part.done) break;
      size += part.value.byteLength;
      if (size > 2 * 1024 * 1024)
        throw new Error("Response exceeds example size limit");
      chunks.push(part.value);
    }
    return JSON.parse(Buffer.concat(chunks).toString("utf8")) as unknown;
  } finally {
    await reader.cancel().catch(() => {});
    reader.releaseLock();
  }
}
function base(value: string): string {
  const url = new URL(value);
  if (
    url.username ||
    url.password ||
    url.search ||
    url.hash ||
    (url.protocol !== "https:" &&
      !(
        url.protocol === "http:" &&
        ["localhost", "127.0.0.1", "[::1]"].includes(url.hostname)
      ))
  )
    throw new Error(
      "Use HTTPS URLs without credentials, query or fragment; HTTP is allowed only for loopback tests",
    );
  return url.href.replace(/\/+$/u, "");
}
function safeCode(value: unknown, fallback: string): string {
  // Server error messages/bodies can echo credentials or customer data: deliberately discard them.
  return typeof value === "string" && /^[A-Z][A-Z0-9_]{0,95}$/u.test(value)
    ? value
    : fallback;
}
class Transport {
  #connection: Connection;
  #endpoint: string;
  #cached: { token: string; expires: number } | undefined;
  #pending: Promise<string> | undefined;
  constructor(
    connection: Connection,
    readonly api: string,
    readonly admin: string,
  ) {
    this.#connection = connection;
    this.#endpoint = `${base(connection.authUrl)}/realms/${segment(connection.realm)}/protocol/openid-connect/token`;
  }
  [inspect.custom](): string {
    return "ExampleTransport";
  }
  async #fetch(url: string, init: RequestInit): Promise<Response> {
    try {
      return await fetch(url, {
        ...init,
        redirect: "error",
        signal:
          init.signal ??
          AbortSignal.timeout(this.#connection.timeoutMs ?? 60000),
      });
    } catch {
      throw new ExampleError(0, "TRANSPORT_FAILED");
    }
  }
  async #token(
    form: URLSearchParams,
  ): Promise<{ token: string; expires: number }> {
    form.set("client_id", this.#connection.clientId);
    form.set("client_secret", this.#connection.clientSecret);
    try {
      const started = Date.now();
      const response = await this.#fetch(this.#endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form,
      });
      if (!response.ok) {
        await response.body?.cancel();
        throw new Error();
      }
      const data = object(await boundedJson(response));
      const token = text(data.access_token, "token");
      if (
        !/^[\x21-\x7e]+$/u.test(token) ||
        typeof data.expires_in !== "number" ||
        !Number.isInteger(data.expires_in) ||
        data.expires_in <= 0 ||
        typeof data.token_type !== "string" ||
        data.token_type.toLowerCase() !== "bearer"
      )
        throw new Error();
      const expires = started + data.expires_in * 1000;
      if (expires <= Date.now()) throw new Error();
      return { token, expires };
    } catch {
      throw new ExampleError(0, "TOKEN_ACQUISITION_FAILED");
    }
  }
  async #serviceToken(): Promise<string> {
    if (this.#cached && Date.now() < this.#cached.expires)
      return this.#cached.token;
    if (!this.#pending)
      this.#pending = this.#token(
        new URLSearchParams({ grant_type: "client_credentials" }),
      )
        .then((data) => {
          const remaining = data.expires - Date.now();
          this.#cached = {
            ...data,
            expires: data.expires - Math.min(30000, remaining / 2),
          };
          return data.token;
        })
        .finally(() => {
          this.#pending = undefined;
        });
    return this.#pending;
  }
  async exchange(
    value: string,
    kind: "assertion" | "token",
  ): Promise<UserWorkplace> {
    if (!/^[\x21-\x7e]+$/u.test(value))
      throw new Error("Invalid user credential");
    const form = new URLSearchParams();
    if (kind === "assertion") {
      form.set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
      form.set("assertion", value);
    } else {
      form.set("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
      form.set("subject_token", value);
      form.set(
        "subject_token_type",
        "urn:ietf:params:oauth:token-type:access_token",
      );
      form.set(
        "requested_token_type",
        "urn:ietf:params:oauth:token-type:access_token",
      );
      for (const audience of ["workspace-api", "cloud-gateway", "search-admin"])
        form.append("audience", audience);
    }
    const result = await this.#token(form);
    return new UserWorkplace(this, result.token, result.expires);
  }
  async request(
    origin: "api" | "admin",
    path: string,
    method: "GET" | "POST",
    body?: unknown,
    user?: { token: string; expires: number },
    headers: Record<string, string> = {},
    stream = false,
  ): Promise<Response> {
    if (user && Date.now() >= user.expires)
      throw new ExampleError(401, "USER_TOKEN_EXPIRED");
    const token = user?.token ?? (await this.#serviceToken());
    const send = (access: string) =>
      this.#fetch((origin === "api" ? this.api : this.admin) + path, {
        method,
        headers: {
          Accept: stream ? "text/event-stream" : "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${access}`,
          ...headers,
        },
        ...(body === undefined ? {} : { body: JSON.stringify(body) }),
      });
    let response = await send(token);
    if (response.status === 401 && !user) {
      await response.body?.cancel();
      if (this.#cached?.token === token) this.#cached = undefined;
      response = await send(await this.#serviceToken());
    }
    if (!response.ok) {
      let data: ObjectBody = {};
      try {
        data = object(await boundedJson(response));
      } catch {
        /* discard untrusted/non-JSON bodies */
      }
      throw new ExampleError(
        response.status,
        safeCode(
          data.code ??
            (data.error !== null && typeof data.error === "object"
              ? object(data.error).code
              : undefined),
          "REQUEST_REFUSED",
        ),
      );
    }
    return response;
  }
  get timeoutMs(): number {
    return this.#connection.timeoutMs ?? 60000;
  }
}
async function json(response: Response): Promise<ObjectBody> {
  try {
    return object(await boundedJson(response));
  } catch {
    throw new ExampleError(response.status, "INVALID_RESPONSE");
  }
}
async function projectJson(response: Response): Promise<ObjectBody> {
  const body = await json(response);
  if (body.code !== 1)
    throw new ExampleError(response.status, "SEARCH_APPLICATION_FAILED");
  return body;
}
function workspacePath(
  id: string,
  operation: "search" | "query" | "chat",
): string {
  return `/workspace/v1/workspaces/${segment(id)}/${operation}`;
}
async function* stream(
  transport: Transport,
  id: string,
  operation: "query" | "chat",
  request: WorkspaceRequest,
  user?: { token: string; expires: number },
): AsyncGenerator<StreamEvent> {
  const response = await transport.request(
    "api",
    workspacePath(id, operation),
    "POST",
    {
      ...workspaceBody(request),
      options: { ...workspaceBody(request).options, stream: true },
    },
    user,
    {},
    true,
  );
  if (
    !response.headers.get("content-type")?.startsWith("text/event-stream") ||
    !response.body
  ) {
    await response.body?.cancel();
    throw new ExampleError(
      response.status,
      "WORKSPACE_STREAM_INVALID_RESPONSE",
    );
  }
  const reader = response.body.getReader(),
    decoder = new TextDecoder("utf-8", { fatal: true });
  let buffer = "",
    event = "message",
    lines: string[] = [],
    frameSize = 0,
    skipLF = false;
  let session = request.session_id;
  try {
    while (true) {
      // Node fetch's request deadline bounds total lifetime; this also bounds each read.
      let timer: ReturnType<typeof setTimeout> | undefined;
      const part = await Promise.race([
        reader.read(),
        new Promise<never>((_, reject) => {
          timer = setTimeout(
            () => reject(new ExampleError(0, "WORKSPACE_STREAM_TIMEOUT")),
            transport.timeoutMs,
          );
        }),
      ]).finally(() => clearTimeout(timer));
      if (part.done) throw new ExampleError(0, "WORKSPACE_STREAM_INCOMPLETE");
      buffer += decoder.decode(part.value, { stream: true });
      if (buffer.length > 1024 * 1024)
        throw new ExampleError(0, "WORKSPACE_STREAM_EVENT_TOO_LARGE");
      if (skipLF && buffer.length) {
        if (buffer.startsWith("\n")) buffer = buffer.slice(1);
        skipLF = false;
      }
      let boundary: number;
      while ((boundary = buffer.search(/[\r\n]/u)) >= 0) {
        const line = buffer.slice(0, boundary);
        const width =
          buffer[boundary] === "\r" && buffer[boundary + 1] === "\n" ? 2 : 1;
        skipLF = buffer[boundary] === "\r" && boundary === buffer.length - 1;
        buffer = buffer.slice(boundary + width);
        frameSize += line.length + width;
        if (frameSize > 1024 * 1024)
          throw new ExampleError(0, "WORKSPACE_STREAM_EVENT_TOO_LARGE");
        if (!line) {
          if (lines.length) {
            let data: ObjectBody;
            try {
              data = object(JSON.parse(lines.join("\n")));
            } catch {
              throw new ExampleError(0, "WORKSPACE_STREAM_INVALID_EVENT");
            }
            const payload =
              data.payload === undefined ? {} : object(data.payload);
            if (event === "run.failed" || event === "error")
              throw new ExampleError(0, "WORKSPACE_STREAM_FAILED");
            if (
              event === "run.started" &&
              typeof payload.session_id === "string"
            )
              session = payload.session_id;
            if (event === "run.completed") {
              if (payload.status !== "COMPLETED")
                throw new ExampleError(0, "WORKSPACE_STREAM_INVALID_EVENT");
              if (session && payload.session_id === undefined)
                payload.session_id = session;
              yield { event, data, payload };
              return;
            }
            yield { event, data, payload };
          }
          event = "message";
          lines = [];
          frameSize = 0;
        } else if (line.startsWith("event:"))
          event = line.slice(6).replace(/^ /u, "");
        else if (line.startsWith("data:"))
          lines.push(line.slice(5).replace(/^ /u, ""));
      }
    }
  } catch (error) {
    if (error instanceof ExampleError) throw error;
    throw new ExampleError(0, "WORKSPACE_STREAM_READ_ERROR");
  } finally {
    await reader.cancel().catch(() => {});
    reader.releaseLock();
  }
}
/** Delegated user credentials cannot call Core search or provisioning through this helper. */
export class UserWorkplace {
  #transport: Transport;
  #user: { token: string; expires: number };
  constructor(transport: Transport, token: string, expires: number) {
    this.#transport = transport;
    this.#user = { token, expires };
  }
  [inspect.custom](): string {
    return "UserWorkplace";
  }
  /** Expiry of the exchanged user credential; obtain a fresh exchange before it expires. */
  get expiresAt(): Date {
    return new Date(this.#user.expires);
  }
  /** Retrieves Workplace documents using only the delegated user credential.
   * @param id - Allowed workspace ID.
   * @param body - Query with optional source narrowing.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  search(id: string, body: WorkspaceRequest): Promise<ObjectBody> {
    return this.#run(id, "search", body);
  }
  /** Answers as the delegated user, applying that user access and memory.
   * @param id - Allowed workspace ID.
   * @param body - Query request; answer mode must be explicit.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  query(id: string, body: WorkspaceRequest): Promise<ObjectBody> {
    return this.#run(id, "query", body);
  }
  /** Continues a conversation under the delegated user identity.
   * @param id - Allowed workspace ID.
   * @param body - Chat turn and optional returned session ID.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  chat(id: string, body: WorkspaceRequest): Promise<ObjectBody> {
    return this.#run(id, "chat", body);
  }
  #run(
    id: string,
    operation: "search" | "query" | "chat",
    body: WorkspaceRequest,
  ): Promise<ObjectBody> {
    text(body.query, "query");
    return this.#transport
      .request(
        "api",
        workspacePath(id, operation),
        "POST",
        workspaceBody(body),
        this.#user,
      )
      .then(json);
  }
}
/** Curated HTTP teaching helper, not an SDK. Service tokens are cached and retried once on 401.
 * @example
 * const client = new ExampleClient(settings);
 * const response = await client.projectSearch(projectId, searchQuery("star wars"));
 */
export class ExampleClient {
  #transport: Transport;
  constructor(connection: Connection) {
    text(connection.clientId, "clientId");
    text(connection.clientSecret, "clientSecret");
    if (
      connection.timeoutMs !== undefined &&
      (!Number.isInteger(connection.timeoutMs) || connection.timeoutMs <= 0)
    )
      throw new Error("Invalid timeoutMs");
    this.#transport = new Transport(
      { ...connection },
      base(connection.apiUrl),
      base(connection.adminUrl),
    );
  }
  [inspect.custom](): string {
    return "ExampleClient";
  }
  /** Searches an ordinary project as the service identity.
   * @param id - Assigned project ID.
   * @param body - SSPL request limited to the taught public fields.
   * @returns Successful Core envelope; code must be 1.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  projectSearch(id: string, body: SearchQuery): Promise<ObjectBody> {
    return this.#transport
      .request(
        "api",
        `/core/projects/${segment(id)}/search`,
        "POST",
        projectBody(body),
      )
      .then(projectJson);
  }
  /** Searches using an existing saved use-case configuration.
   * @param id - Assigned use-case ID.
   * @param body - SSPL request.
   * @returns Successful Core envelope; code must be 1.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  usecaseSearch(id: string, body: SearchQuery): Promise<ObjectBody> {
    return this.#transport
      .request(
        "api",
        `/core/usecases/${segment(id)}/search`,
        "POST",
        projectBody(body),
      )
      .then(projectJson);
  }
  /** Sends a bare array of independent project searches in one request.
   * @param id - Assigned project ID.
   * @param body - Individual SSPL queries; slice returned hits for display.
   * @returns Successful Core envelope; code must be 1.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  multiSearch(id: string, body: SearchQuery[]): Promise<ObjectBody> {
    return this.#transport
      .request(
        "api",
        `/core/projects/${segment(id)}/mSearch`,
        "POST",
        body.map(projectBody),
      )
      .then(projectJson);
  }
  /** Retrieves permitted Workplace documents without generating an answer.
   * @param id - Workspace ID the service can access.
   * @param body - Natural-language query and optional source/business-field narrowing.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  workplaceSearch(id: string, body: WorkspaceRequest): Promise<ObjectBody> {
    return this.#workplace(id, "search", body);
  }
  /** Retrieves evidence or generates a grounded answer, according to mode.
   * @param id - Workspace ID the service can access.
   * @param body - Query request; use standard memory when serving many people.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  workplaceQuery(id: string, body: WorkspaceRequest): Promise<ObjectBody> {
    return this.#workplace(id, "query", body);
  }
  /** Runs a chat turn, preserving context when session_id is supplied.
   * @param id - Workspace ID the service can access.
   * @param body - Turn query and optional session ID.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  workplaceChat(id: string, body: WorkspaceRequest): Promise<ObjectBody> {
    return this.#workplace(id, "chat", body);
  }
  #workplace(
    id: string,
    operation: "search" | "query" | "chat",
    body: WorkspaceRequest,
  ): Promise<ObjectBody> {
    text(body.query, "query");
    return this.#transport
      .request("api", workspacePath(id, operation), "POST", workspaceBody(body))
      .then(json);
  }
  /** Streams query events until a valid run.completed event.
   * @param id - Workspace ID.
   * @param body - Query request; stream is added by the helper.
   * @returns Events; run.completed payload is authoritative and includes retained session context.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  streamQuery(id: string, body: WorkspaceRequest): AsyncGenerator<StreamEvent> {
    return stream(this.#transport, id, "query", body);
  }
  /** Streams chat events and retains the session ID in the final completion.
   * @param id - Workspace ID.
   * @param body - Chat request and optional session ID.
   * @returns Events; run.completed payload is authoritative and includes retained session context.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  streamChat(id: string, body: WorkspaceRequest): AsyncGenerator<StreamEvent> {
    return stream(this.#transport, id, "chat", body);
  }
  /** Exchanges a fresh external identity assertion for a one-shot delegated Workplace identity.
   * @param assertion - Short-lived signed assertion for a user authenticated in your backend.
   * @returns Delegated Workplace-only client with an expiry time.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  asUser(assertion: string): Promise<UserWorkplace> {
    return this.#transport.exchange(assertion, "assertion");
  }
  /** Exchanges a user access token using the public token-exchange grant.
   * @param token - Current SmartSearch user access token; never persist or log it.
   * @returns Delegated Workplace-only client with an expiry time.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  asUserFromToken(token: string): Promise<UserWorkplace> {
    return this.#transport.exchange(token, "token");
  }
  /** Reads provisioning capabilities from Search Admin; does not prove project or workspace access.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  capabilities(): Promise<ObjectBody> {
    return this.#transport
      .request("admin", "/search-admin/api/provisioning/v1/capabilities", "GET")
      .then(json);
  }
  /** Submits a federated registration or onboarding job.
   * @param body - Tenant-scoped users and allowed grants.
   * @param idempotencyKey - Stable key for exactly this request; reuse only with the same body.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  submitJob(body: SubmitJob, idempotencyKey: string): Promise<ObjectBody> {
    if (!/^[\x21-\x7e]{1,128}$/u.test(idempotencyKey))
      throw new Error("Invalid idempotency key");
    return this.#transport
      .request(
        "admin",
        "/search-admin/api/provisioning/v1/jobs",
        "POST",
        jobBody(body),
        undefined,
        { "Idempotency-Key": idempotencyKey },
      )
      .then(json);
  }
  /** Reads the current state of a submitted registration job.
   * @param id - Job ID returned by submitJob.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  getJob(id: string): Promise<ObjectBody> {
    return this.#transport
      .request(
        "admin",
        `/search-admin/api/provisioning/v1/jobs/${segment(id)}`,
        "GET",
      )
      .then(json);
  }
  /** Reads the first page of up to 100 registration item outcomes.
   * @param id - Job ID; examples submit no more than two users.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  listJobItems(id: string): Promise<ObjectBody> {
    return this.#transport
      .request(
        "admin",
        `/search-admin/api/provisioning/v1/jobs/${segment(id)}/items?limit=100`,
        "GET",
      )
      .then(json);
  }
  /** Maps external user IDs to principal IDs within the saved integration.
   * @param integration - Saved integration ID.
   * @param tenant - Tenant ID associated with the service key and integration.
   * @returns Direct response body.
   * @throws ExampleError on HTTP, authentication, application or streaming failure.
   */
  listPrincipals(integration: string, tenant: string): Promise<ObjectBody> {
    return this.#transport
      .request(
        "admin",
        `/search-admin/api/provisioning/v1/integrations/${segment(integration)}/principals?scope_kind=TENANT&scope_id=${encodeURIComponent(tenant)}&limit=100`,
        "GET",
      )
      .then(json);
  }
}
