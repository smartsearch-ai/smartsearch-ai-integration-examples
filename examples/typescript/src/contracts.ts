/** Wire shapes used by the public examples. This is example plumbing, not an SDK.
 * Field names mirror the public Java/Python SDK's serialization, including ssapi_flags.
 */
/** Parsed response object; unknown values require narrowing before use. */
export type ObjectBody = Record<string, unknown>;
/** Narrows parsed JSON to an object.
 * @param value - Parsed JSON field.
 * @returns Response object; throws Error for non-objects.
 */
export function object(value: unknown): ObjectBody {
  if (value === null || typeof value !== "object" || Array.isArray(value))
    throw new Error("Expected JSON object");
  return value as ObjectBody;
}
function finite(value: unknown, name: string): number {
  if (typeof value !== "number" || !Number.isFinite(value))
    throw new Error(`${name} must be finite`);
  return value;
}
function bool(value: unknown): boolean {
  if (typeof value !== "boolean") throw new Error("Expected boolean");
  return value;
}
/** Requires a nonblank primitive string.
 * @param value - Untrusted scalar.
 * @param name - Safe label used in validation errors.
 * @returns Validated string; throws Error for invalid input.
 */
export function text(value: unknown, name: string): string {
  if (typeof value !== "string" || !value.trim())
    throw new Error(`${name} must not be blank`);
  return value;
}
/** Encodes one ID as a URL path segment.
 * @param value - Resource ID; dot segments and control characters are rejected.
 * @returns Encoded path segment.
 */
export function segment(value: string): string {
  text(value, "ID");
  if (value === "." || value === ".." || /[\u0000-\u0020\u007f]/u.test(value))
    throw new Error("Invalid ID");
  return encodeURIComponent(value);
}
/** Typed SSPL filter condition over your project business fields. */
export type Clause =
  | {
      search_type: "term" | "match" | "match_phrase";
      field: string;
      value: string;
    }
  | { search_type: "exists"; field: string }
  | {
      search_type: "range";
      field: string;
      condition: Partial<Record<"gt" | "gte" | "lt" | "lte", number>>;
    };
/** A required condition or OR group inside filters.all. */
export type RequiredClause = Clause | { any: Clause[] };
/** Public keyword, vector and hybrid retrieval choices. */
export type NeuralMode =
  | "BM25"
  | "A_KNN"
  | "A_KNN_AND_BM25"
  | "EXACT_AND_BM25_FUSED"
  | "EXACT_AND_BM25_RANKING";
/** Public per-request search overrides taught by these examples. */
export interface SearchFlags {
  highlight_enabled?: boolean;
  wild_card_search?: boolean;
  auto_correct?: boolean;
  trim_query?: boolean;
  remove_special_chars?: boolean;
  query_expansion_enable?: false;
  precision?: number;
  neural_mode?: NeuralMode;
  neural_top_matches?: number;
  neural_total_matches?: number;
  normalization_technique?: "RANK" | "MIN_MAX";
  normalization_combination_technique?: "RRF" | "ARITHMETIC_MEAN";
  neural_rank_window_size?: number;
  neural_rank_constant?: number;
  rerank_enabled?: boolean;
  rerank_first_stage_size?: number;
  rerank_min_score?: number;
}
/** Core SSPL wire request; HTTP serialization projects only these properties. */
export interface SearchQuery {
  q: string;
  from: number;
  size: number;
  fields?: string[];
  response_fields?: string[];
  sorts?: { field: string; order: "asc" | "desc" }[];
  filters?: { all?: RequiredClause[]; not?: Clause[] };
  boosts?: {
    search_type: "term";
    field: string;
    value: string;
    weight: number;
  }[];
  aggs?: (
    | { type: "terms"; name: string; field: string; size?: number }
    | {
        type: "cardinality";
        name: string;
        field: string;
        precisionThreshold?: number;
      }
  )[];
  ssapi_flags?: SearchFlags;
}
/** Builds a small validated SSPL search request.
 * @param q - Query text.
 * @param options - Public search fields to override.
 * @returns Search request; throws Error on invalid paging, fields or flags.
 */
export function searchQuery(
  q: string,
  options: Partial<Omit<SearchQuery, "q">> = {},
): SearchQuery {
  const query = {
    q: text(q, "q").trim(),
    from: 0,
    size: 5,
    response_fields: ["title"],
    ...options,
  };
  if (
    !Number.isInteger(query.from) ||
    query.from < 0 ||
    !Number.isInteger(query.size) ||
    query.size <= 0
  )
    throw new Error("Invalid search paging");
  for (const field of query.response_fields ?? []) {
    if (
      !field.trim() ||
      field.includes("*") ||
      field.toLowerCase().startsWith("_ss_ml_")
    )
      throw new Error("Response fields must be explicit business fields");
  }
  const f = query.ssapi_flags;
  if (
    f?.precision !== undefined &&
    (!Number.isInteger(f.precision) || f.precision < 1 || f.precision > 11)
  )
    throw new Error("precision must be 1..11");
  if (
    (f?.normalization_technique === "RANK" &&
      f.normalization_combination_technique !== "RRF") ||
    (f?.normalization_combination_technique === "RRF" &&
      f.normalization_technique !== "RANK")
  )
    throw new Error("RANK and RRF must be paired");
  if (
    f?.neural_top_matches !== undefined ||
    f?.neural_total_matches !== undefined
  ) {
    const top = f.neural_top_matches ?? 0,
      total = f.neural_total_matches ?? 0;
    if (
      !Number.isInteger(top) ||
      !Number.isInteger(total) ||
      top < 1 ||
      top > total ||
      total > 10000
    )
      throw new Error("Require 1 <= top <= total <= 10000");
  }
  return query;
}
/** Natural-language Workplace request; caller identity determines authorization. */
export interface WorkspaceRequest {
  query: string;
  mode?: "retrieval_only" | "answer";
  memory_mode?: "standard" | "agentic";
  session_id?: string;
  source_ids?: string[];
  filters?: { all: { search_type: "match"; field: "title"; value: string }[] };
  options?: { include_sources?: boolean; stream?: boolean };
}
/** Workspace membership and initial source grants for the onboarding demo. */
export interface Membership {
  workspace_id: string;
  source_grants: {
    source_id: string;
    expected_revision: number;
    grants: { groups: string[]; roles: string[]; security_keys: string[] };
  }[];
}
/** A federated external user to register, always assigned the GUEST platform role. */
export interface EnsureItem {
  item_key: string;
  external_user_id: string;
  profile: {
    email: string;
    first_name: string;
    last_name: string;
    display_name: string;
  };
  platform_role: "GUEST";
  permissions: string[];
  workspaces: Membership[];
  external_identity: { subject: string; username: string };
}
/** Tenant-scoped public registration job, with one to 100 user items. */
export interface SubmitJob {
  scope: { kind: "TENANT"; id: string };
  integration_id: string;
  kind: "UPSERT_USERS" | "ONBOARD_USERS";
  items: EnsureItem[];
}
/** One SSE envelope plus its decoded payload; final completion is authoritative. */
export interface StreamEvent {
  event: string;
  data: ObjectBody;
  payload: ObjectBody;
}

/** Runtime projection is deliberate: TypeScript types cannot stop extra properties in JS.
 * Only the documented example fields reach the wire, including nested flags and grants.
 */
function scalarFlag(key: string, value: unknown): string | number | boolean {
  const booleanKeys = [
    "highlight_enabled",
    "wild_card_search",
    "auto_correct",
    "trim_query",
    "remove_special_chars",
    "query_expansion_enable",
    "rerank_enabled",
  ];
  if (booleanKeys.includes(key)) return bool(value);
  const choices: Record<string, readonly string[]> = {
    neural_mode: [
      "BM25",
      "A_KNN",
      "A_KNN_AND_BM25",
      "EXACT_AND_BM25_FUSED",
      "EXACT_AND_BM25_RANKING",
    ],
    normalization_technique: ["RANK", "MIN_MAX"],
    normalization_combination_technique: ["RRF", "ARITHMETIC_MEAN"],
  };
  if (choices[key]) {
    if (typeof value !== "string" || !choices[key].includes(value))
      throw new Error("Invalid flag choice");
    return value;
  }
  return finite(value, key);
}
/** Projects a request onto the public search wire contract.
 * @param input - Typed request; extra runtime properties are discarded.
 * @returns Closed validated JSON payload.
 */
export function projectBody(input: SearchQuery): SearchQuery {
  const flags = input.ssapi_flags;
  const selected: SearchFlags = {};
  if (flags)
    for (const key of [
      "highlight_enabled",
      "wild_card_search",
      "auto_correct",
      "trim_query",
      "remove_special_chars",
      "query_expansion_enable",
      "precision",
      "neural_mode",
      "neural_top_matches",
      "neural_total_matches",
      "normalization_technique",
      "normalization_combination_technique",
      "neural_rank_window_size",
      "neural_rank_constant",
      "rerank_enabled",
      "rerank_first_stage_size",
      "rerank_min_score",
    ] as const)
      if (flags[key] !== undefined)
        Object.assign(selected, { [key]: scalarFlag(key, flags[key]) });
  const clause = (c: Clause): Clause => {
    const field = text(c.field, "filter field");
    switch (c.search_type) {
      case "exists":
        return { search_type: "exists", field };
      case "term":
      case "match":
      case "match_phrase":
        return {
          search_type: c.search_type,
          field,
          value: text(c.value, "filter value"),
        };
      case "range": {
        const condition: Partial<Record<"gt" | "gte" | "lt" | "lte", number>> =
          {};
        for (const key of ["gt", "gte", "lt", "lte"] as const)
          if (c.condition[key] !== undefined) {
            if (!Number.isFinite(c.condition[key]))
              throw new Error("Invalid range bound");
            condition[key] = c.condition[key];
          }
        if (!Object.keys(condition).length)
          throw new Error("Range requires bounds");
        return { search_type: "range", field, condition };
      }
      default:
        throw new Error("Unsupported filter type");
    }
  };
  return searchQuery(input.q, {
    from: input.from,
    size: input.size,
    ...(input.fields
      ? { fields: input.fields.map((v) => text(v, "field")) }
      : {}),
    ...(input.response_fields
      ? { response_fields: [...input.response_fields] }
      : {}),
    ...(input.sorts
      ? {
          sorts: input.sorts.map((v) => ({
            field: text(v.field, "sort field"),
            order:
              v.order === "asc" || v.order === "desc"
                ? v.order
                : (() => {
                    throw new Error("Invalid sort order");
                  })(),
          })),
        }
      : {}),
    ...(input.filters
      ? {
          filters: {
            ...(input.filters.all
              ? {
                  all: input.filters.all.map((c) =>
                    "any" in c ? { any: c.any.map(clause) } : clause(c),
                  ),
                }
              : {}),
            ...(input.filters.not
              ? { not: input.filters.not.map(clause) }
              : {}),
          },
        }
      : {}),
    ...(input.boosts
      ? {
          boosts: input.boosts.map((v) => ({
            search_type: "term" as const,
            field: text(v.field, "boost field"),
            value: text(v.value, "boost value"),
            weight: finite(v.weight, "weight"),
          })),
        }
      : {}),
    ...(input.aggs
      ? {
          aggs: input.aggs.map((v) =>
            v.type === "terms"
              ? {
                  type: "terms" as const,
                  name: text(v.name, "agg name"),
                  field: text(v.field, "agg field"),
                  ...(v.size !== undefined
                    ? { size: finite(v.size, "agg size") }
                    : {}),
                }
              : {
                  type: "cardinality" as const,
                  name: text(v.name, "agg name"),
                  field: text(v.field, "agg field"),
                  ...(v.precisionThreshold !== undefined
                    ? {
                        precisionThreshold: finite(
                          v.precisionThreshold,
                          "agg precision",
                        ),
                      }
                    : {}),
                },
          ),
        }
      : {}),
    ...(flags ? { ssapi_flags: selected } : {}),
  });
}
/** Projects a request onto the Workplace fields used by the examples.
 * @param input - Typed natural-language request.
 * @returns Closed request with no caller-selected authorization data.
 */
export function workspaceBody(input: WorkspaceRequest): WorkspaceRequest {
  return {
    query: text(input.query, "query"),
    ...(input.mode
      ? {
          mode:
            input.mode === "answer" || input.mode === "retrieval_only"
              ? input.mode
              : (() => {
                  throw new Error("Invalid mode");
                })(),
        }
      : {}),
    ...(input.memory_mode
      ? {
          memory_mode:
            input.memory_mode === "standard" || input.memory_mode === "agentic"
              ? input.memory_mode
              : (() => {
                  throw new Error("Invalid memory mode");
                })(),
        }
      : {}),
    ...(input.session_id
      ? { session_id: text(input.session_id, "session_id") }
      : {}),
    ...(input.source_ids
      ? { source_ids: input.source_ids.map((v) => text(v, "source_id")) }
      : {}),
    ...(input.filters
      ? {
          filters: {
            all: input.filters.all.map((v) => {
              if (v.field !== "title" || v.search_type !== "match")
                throw new Error("Unsupported example filter");
              return {
                search_type: "match" as const,
                field: "title" as const,
                value: text(v.value, "filter value"),
              };
            }),
          },
        }
      : {}),
    ...(input.options
      ? {
          options: {
            ...(input.options.include_sources !== undefined
              ? { include_sources: bool(input.options.include_sources) }
              : {}),
          },
        }
      : {}),
  };
}
/** Projects the small federated registration shape onto its public wire contract.
 * @param input - Tenant-scoped registration job.
 * @returns Closed public job request; throws Error for unsupported kind or item count.
 */
export function jobBody(input: SubmitJob): SubmitJob {
  if (
    input.scope.kind !== "TENANT" ||
    !["UPSERT_USERS", "ONBOARD_USERS"].includes(input.kind) ||
    input.items.length < 1 ||
    input.items.length > 100
  )
    throw new Error("Invalid registration job");
  return {
    scope: { kind: "TENANT", id: text(input.scope.id, "tenant") },
    integration_id: text(input.integration_id, "integration"),
    kind: input.kind,
    items: input.items.map((v) => ({
      item_key: text(v.item_key, "item key"),
      external_user_id: text(v.external_user_id, "external user ID"),
      profile: {
        email: text(v.profile.email, "profile email"),
        first_name: text(v.profile.first_name, "profile first_name"),
        last_name: text(v.profile.last_name, "profile last_name"),
        display_name: text(v.profile.display_name, "profile display_name"),
      },
      platform_role: "GUEST",
      permissions: v.permissions.map((v) => text(v, "permission")),
      external_identity: {
        subject: text(v.external_identity.subject, "subject"),
        username: text(v.external_identity.username, "username"),
      },
      workspaces: v.workspaces.map((w) => ({
        workspace_id: text(w.workspace_id, "workspace ID"),
        source_grants: w.source_grants.map((g) => ({
          source_id: text(g.source_id, "source ID"),
          expected_revision: finite(g.expected_revision, "revision"),
          grants: {
            groups: g.grants.groups.map((v) => text(v, "grant")),
            roles: g.grants.roles.map((v) => text(v, "grant")),
            security_keys: g.grants.security_keys.map((v) => text(v, "grant")),
          },
        })),
      })),
    })),
  };
}
