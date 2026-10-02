# REST examples with curl

You have a service key and want to see the HTTP request. Start with one search,
read its JSON, then change one field. These 44 examples follow the same learning
path as the [Java](../java/README.md), [Python](../python/README.md) and
[TypeScript](../typescript/README.md) versions.

Each script contains its request and a formatted sample JSON comment. Shared
[HTTP helpers](lib/common.sh) execute `curl`, acquire tokens, and check responses.
No SmartSearch SDK package is needed. This folder demonstrates the curated public
integration API; it is not a complete platform API reference.

## Choose your first task

| You want to… | Start with | What you need |
|---|---|---|
| Search a project | `first_search` | Service key assigned to that project |
| Find workspace documents | `search_workplace_as_service` | Service membership in the workspace |
| Answer a question from documents | `ask_a_question` | Workspace access and a configured answering agent |
| Register users and grant workspace access | `register_users_with_workspace_access` | Owner-configured Register users policy permitting the workspace and source |

`connect_and_check_access` reads **Admin provisioning capabilities**. It checks
that operation only. It does not prove project search or Workplace access, and
you can begin with search without running it.

## Set up once

You need Bash 3.2+, `curl`, `jq`, and Python 3.10+. Python helpers use only the
standard library. OpenSSL is needed only for `create_user_assertion`.

1. Copy [`.env.example`](../../.env.example) to `.env` at the repository root.
   If you already have `.env`, keep it and fill in any missing settings.
2. Ask your administrator for the connection settings, service key and resource
   IDs. The placeholder URLs and IDs in the template are not working credentials.
3. Run from this folder:

```bash
cd examples/rest
./run.sh --list
./run.sh first_search "star wars"
# Or search an existing workspace with a word from YOUR documents:
./run.sh search_workplace_as_service "refund"
```

`./run.sh first_search --help` shows that step's prerequisites. Help and the list
work without credentials or network access. Run examples through `run.sh`; the
individual topic scripts rely on its shared setup.

The default env file is the repository's `.env`. To use only values already
exported in your shell, launch with `SMARTSEARCH_REST_ENV_FILE=/dev/null`.
You can also set that variable to another trusted env file before launching.
The launcher sources it as shell code; use files you control.

| Settings | When used |
|---|---|
| Auth URL, realm, service key ID and secret | Acquiring a service or delegated token |
| API URL and project ID | Project searches |
| API URL and use-case ID | `search_through_use_case` |
| API URL and workspace ID | Workplace calls |
| Source ID | The source-filter and workspace-access registration examples |
| Admin URL | Capabilities and user provisioning |
| Integration and tenant IDs | Registering users |
| Short-lived user token or signed assertion | The matching per-user example |

Unlike the other versions' shared connection helpers, REST checks settings for
the selected task. Project search does not require the Admin URL. The standalone
assertion demo needs the auth URL and realm to construct its audience, but it
makes no HTTP request unless you explicitly add `--sign-in`.

The service-key secret belongs on your backend. Keep tokens and private keys out
of copied requests, browser code and issue reports. Do not enable shell tracing
(`set -x` or `bash -x`) when running with credentials.

## Read the first request

`first_search` sends `POST /core/projects/{projectId}/search`. Its complete request
looks like this; the command-line query replaces `q`:

```json
{
  "from": 0,
  "size": 5,
  "response_fields": [
    "title",
    "release_date",
    "vote_average"
  ],
  "q": "star wars"
}
```

The script prints the route and formatted JSON, then a result summary and
returned document fields. This is **illustrative output**, not a promised match:

```json
{
  "received_hits": 1,
  "actual_mode": "BM25",
  "warning": null
}
```

A **hit** is one returned document. The count is not a total-match estimate.
HTTP 200 does not guarantee search success: the Core envelope must have `code: 1`.
An application failure exits unsuccessfully instead of looking like zero hits.

**Try it:** predict the effect of changing `size` from 5 to 2, edit that value in
[first_search.sh](search/first_search.sh), then run it again. Next change the query.
Change one thing at a time so you can explain why the result changed.

Project examples use the Movies sample fields. Adapt them to your schema.
Semantic search needs embeddings, reranking needs a configured reranker, and
precision levels need a precision template. Check the actual mode and warnings
before interpreting a comparison. Facets may be restricted on projects with
document security. Ask your administrator about the supported configuration;
keep access rules in place. See the [search guide](../java/SEARCH_GUIDE.md).

## Learning path

Each row links to a runnable script. Commands below use example defaults. Most
search steps accept query words after the name; quote a multi-word positional
argument when a step accepts more than one input. Each file shows its run syntax,
HTTP route, prerequisites and formatted sample JSON.

| Step | Example | What to examine | Command |
|---|---|---|---|
| 1 | [connect_and_check_access](gettingstarted/connect_and_check_access.sh) | What a service key is; connect and check what the key may do | `./run.sh connect_and_check_access` |
| 2 | [first_search](search/first_search.sh) | Build a request, send it, read the hits and the response envelope | `./run.sh first_search` |
| 3 | [choose_searched_and_returned_fields](search/choose_searched_and_returned_fields.sh) | Where the query is looked for versus what each hit returns | `./run.sh choose_searched_and_returned_fields` |
| 4 | [page_through_results](search/page_through_results.sh) | Paging with `from` and `size` (reranking off) | `./run.sh page_through_results` |
| 5 | [sort_results](search/sort_results.sh) | Sort by a field instead of relevance (reranking off) | `./run.sh sort_results` |
| 6 | [filter_by_exact_value](search/filter_by_exact_value.sh) | Yes/no conditions on an exact value, including properties inside lists | `./run.sh filter_by_exact_value` |
| 7 | [filter_by_any_of_several_values](search/filter_by_any_of_several_values.sh) | One field, any of several values (multi-select facets) | `./run.sh filter_by_any_of_several_values` |
| 8 | [filter_by_numeric_range](search/filter_by_numeric_range.sh) | At least, at most, between | `./run.sh filter_by_numeric_range` |
| 9 | [filter_by_date_range](search/filter_by_date_range.sh) | Date ranges | `./run.sh filter_by_date_range` |
| 10 | [filter_where_field_exists](search/filter_where_field_exists.sh) | Only documents that have a value in a field | `./run.sh filter_where_field_exists` |
| 11 | [filter_by_full_text_match](search/filter_by_full_text_match.sh) | Words anywhere in a text field | `./run.sh filter_by_full_text_match` |
| 12 | [filter_by_exact_phrase](search/filter_by_exact_phrase.sh) | Words together and in order | `./run.sh filter_by_exact_phrase` |
| 13 | [exclude_results](search/exclude_results.sh) | Remove documents that match a condition | `./run.sh exclude_results` |
| 14 | [combine_filters_with_any_of](search/combine_filters_with_any_of.sh) | AND, OR and NOT together | `./run.sh combine_filters_with_any_of` |
| 15 | [boost_term_values](search/boost_term_values.sh) | Move preferred documents up without removing others | `./run.sh boost_term_values` |
| 16 | [highlight_matches](search/highlight_matches.sh) | Show why a result matched | `./run.sh highlight_matches` |
| 17 | [wildcard_and_prefix_search](search/wildcard_and_prefix_search.sh) | `avat*` style queries | `./run.sh wildcard_and_prefix_search` |
| 18 | [correct_spelling](search/correct_spelling.sh) | Find results despite typos | `./run.sh correct_spelling` |
| 19 | [clean_up_user_input](search/clean_up_user_input.sh) | Trim spaces and remove special characters from typed text | `./run.sh clean_up_user_input` |
| 20 | [turn_off_query_expansion](search/turn_off_query_expansion.sh) | Stop related-word expansion for one request | `./run.sh turn_off_query_expansion` |
| 21 | [facet_counts](search/facet_counts.sh) | Value counts, distinct counts, drill-down | `./run.sh facet_counts` |
| 22 | [keyword_vs_semantic_vs_hybrid](search/keyword_vs_semantic_vs_hybrid.sh) | The three search techniques, and checking which one ran | `./run.sh keyword_vs_semantic_vs_hybrid` |
| 23 | [tune_hybrid_search](search/tune_hybrid_search.sh) | How keyword and semantic results are fused | `./run.sh tune_hybrid_search` |
| 24 | [limit_semantic_matches](search/limit_semantic_matches.sh) | How many close-in-meaning documents to consider and keep | `./run.sh limit_semantic_matches` |
| 25 | [rerank_results](search/rerank_results.sh) | AI reranking on, off, and the project default | `./run.sh rerank_results` |
| 26 | [tune_reranking](search/tune_reranking.sh) | How many candidates to rerank; a minimum score | `./run.sh tune_reranking` |
| 27 | [precision_levels](search/precision_levels.sh) | Broad versus strict matching | `./run.sh precision_levels` |
| 28 | [run_several_searches_at_once](search/run_several_searches_at_once.sh) | Several searches in one round trip | `./run.sh run_several_searches_at_once` |
| 29 | [search_through_use_case](search/search_through_use_case.sh) | Search with a saved configuration | `./run.sh search_through_use_case` |
| 30 | [handle_errors_and_warnings](search/handle_errors_and_warnings.sh) | Invalid requests, refusals, and adjusted answers | `./run.sh handle_errors_and_warnings` |
| 31 | [search_workplace_as_service](workplace/search_workplace_as_service.sh) | Workplace concepts; search, query and chat; the documents that match | `./run.sh search_workplace_as_service` |
| 32 | [filter_workplace_by_source_and_title](workplace/filter_workplace_by_source_and_title.sh) | Limit a search to one source; filter on a document field | `./run.sh filter_workplace_by_source_and_title` |
| 33 | [ask_a_question](workplace/ask_a_question.sh) | Query in answer mode: the request, and reading answer, sources, citations, run ID, status | `./run.sh ask_a_question` |
| 34 | [retrieve_sources_only](workplace/retrieve_sources_only.sh) | Query in retrieval-only mode: the evidence without an answer | `./run.sh retrieve_sources_only` |
| 35 | [stream_an_answer](workplace/stream_an_answer.sh) | Stream a query answer as it is written; events, errors, timeouts | `./run.sh stream_an_answer` |
| 36 | [answer_with_memory](workplace/answer_with_memory.sh) | Memory modes: STANDARD versus AGENTIC, and when to use each | `./run.sh answer_with_memory` |
| 37 | [query_with_filters](workplace/query_with_filters.sh) | Answer from one source and documents that pass a filter | `./run.sh query_with_filters` |
| 38 | [chat_with_follow_up_questions](workplace/chat_with_follow_up_questions.sh) | Conversations: reuse the session for follow-ups | `./run.sh chat_with_follow_up_questions` |
| 39 | [stream_chat_answer](workplace/stream_chat_answer.sh) | Stream a chat answer | `./run.sh stream_chat_answer` |
| 40 | [register_users](users/register_users.sh) | Register users from your system, linked to your identity provider | `./run.sh register_users` |
| 41 | [register_users_with_workspace_access](users/register_users_with_workspace_access.sh) | Register a user and give workspace and source access | `./run.sh register_users_with_workspace_access` |
| 42 | [search_workplace_as_user](workplace/search_workplace_as_user.sh) | Act as a user from the user's own access token, and search | `./run.sh search_workplace_as_user` |
| 43 | [create_user_assertion](users/create_user_assertion.sh) | Build and sign the user assertion your backend creates (JWT, RS256), and the public key set (JWKS) to publish; optionally sign in with it | `./run.sh create_user_assertion` |
| 44 | [sign_in_with_your_identity_provider](workplace/sign_in_with_your_identity_provider.sh) | Act as a user who signed in to your identity provider; search and ask a question as them | `./run.sh sign_in_with_your_identity_provider` |

## How a user gets workspace access

`register_users` creates or updates federated users without adding workspace
access. `register_users_with_workspace_access` submits an **ONBOARD_USERS** job
with both membership and source grants. These examples submit real registration
jobs when pointed at your service: review the demo users and grants first.

The onboarding request contains one user like this. Resource ID strings below
are placeholders for the corresponding `.env` values:

```json
{
  "scope": {
    "kind": "TENANT",
    "id": "your-tenant-id"
  },
  "integration_id": "your-integration-id",
  "kind": "ONBOARD_USERS",
  "items": [
    {
      "item_key": "user-1",
      "external_user_id": "sdk-example-user-1",
      "profile": {
        "email": "sdk-example-user-1@example.com",
        "first_name": "Ada",
        "last_name": "Example",
        "display_name": "Ada Example"
      },
      "platform_role": "GUEST",
      "permissions": [],
      "workspaces": [
        {
          "workspace_id": "your-workspace-id",
          "source_grants": [
            {
              "source_id": "your-source-id",
              "expected_revision": 0,
              "grants": {
                "groups": [
                  "everyone"
                ],
                "roles": [],
                "security_keys": []
              }
            }
          ]
        }
      ],
      "external_identity": {
        "subject": "sdk-example-user-1",
        "username": "sdk-example-user-1"
      }
    }
  ]
}
```

Read the access fields separately:

- **`platform_role: "GUEST"`** is the registered user's platform role. This public
  registration flow accepts `GUEST`; it does not create Owners or administrators.
- **`workspaces`** adds workspace membership.
- **`source_grants`** assigns document-access groups, roles or security keys for a
  source. These labels must match your document access rules and be allowed by
  the provisioning integration. The example uses `SMARTSEARCH_GRANT_GROUP`,
  defaulting to `everyone`; that is a group label, not a bypass of access checks.
- **`expected_revision: 0`** expects that no grant already exists. A revision
  conflict needs investigation, not a blind retry with a different revision.
- **`external_identity`** links the user's existing identity-provider account.
  Use its real subject and username. Email alone does not establish the link.

The script submits the job, polls its state and prints individual item outcomes.
`PARTIAL`, `FAILED` and other terminal states mean processing ended; they do not
mean every user received access. A timeout also does not cancel the server job.
Keep the job ID and continue checking that job.

Reuse the idempotency key only with the **same request body**. If you change demo
users or grants, change the example's key too. Owner setup is explained in the
[root README](../../README.md#what-your-administrator-sets-up).

## Act as an individual user

Service search uses your application's identity. The two delegated examples
exchange a user credential and then call Workplace with that user's access:

| Example | Credential provided securely through the environment |
|---|---|
| `search_workplace_as_user` | `SMARTSEARCH_USER_ACCESS_TOKEN`: the user's current SmartSearch realm access token, exchanged using the token-exchange grant |
| `sign_in_with_your_identity_provider` | `SMARTSEARCH_USER_ASSERTION`: a fresh signed assertion, exchanged using the JWT bearer grant |

These require the Owner's **Act as users** configuration. Register users and
Act as users are separate permissions. Delegated credentials expire; they are
never silently replaced with the service identity. The streaming examples in
this folder use the service identity.

`create_user_assertion` demonstrates signing locally with RS256:

```bash
./run.sh create_user_assertion sdk-example-user-1
# Only after your stable issuer/JWKS and user access are configured:
./run.sh create_user_assertion sdk-example-user-1 --sign-in "travel policy"
```

It prints formatted header, claims and **public** JWKS data, never the JWT or
private key. A temporary demo key cannot authenticate until its public key is
trusted. For configured sign-in, supply a stable PKCS#8 RSA key of at least 2048
bits via `SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM`; issuer and key ID must match
your registered public JWKS. Keep production keys in your key-management system.

The OAuth token requests use URL-encoded form data, not JSON. GET requests such
as capabilities have no request body. Those files describe the appropriate wire
format instead of inventing a JSON payload.

## Streaming and failures

Streamed answer fragments are provisional. The parser waits for a valid
`run.completed` event with `status: "COMPLETED"`, then prints the formatted final
payload. Failed events, malformed data and EOF without completion exit
unsuccessfully. A session returned when chat starts is retained if needed in the
final result. Use the final answer and sources, not only the text fragments.

HTTP calls have bounded timeouts; the request/stream default is 60 seconds.
`SMARTSEARCH_REST_TIMEOUT_SECONDS` and `SMARTSEARCH_REST_JOB_TIMEOUT_SECONDS`
control the limits documented in [`.env.example`](../../.env.example).

| What happened | Check next |
|---|---|
| Missing setting | The exact variable named in the error and that step's prerequisites |
| Token acquisition failed | Auth URL, realm, service key and the selected grant |
| HTTP 403 / `SCOPE_DENIED` | Caller access to the specific project, workspace or source |
| Core application failure | Project availability, query fields and permitted operation; HTTP status alone is insufficient |
| Zero hits after a successful request | Known text from an accessible document, resource ID and filters |
| Stream failed | Discard provisional output; inspect answering configuration and timeout |
| Job timeout or partial result | The same job's state and each user's item outcome |

The transport reads credentials from protected temporary files/stdin, keeps them
out of command arguments, ignores user curl configuration, and does not follow
redirects. HTTPS is required except for loopback test fixtures. Temporary files
are removed when the launcher exits. The helper is teaching code, not an SDK
with a production support contract.

## Check changes locally

Run the offline tests from this folder with the same tools installed:

```bash
python3 -m unittest discover -s tests -v
```

Tests use local HTTP fixtures and synthetic credentials. They do not load your
real `.env` or modify your SmartSearch deployment. The source comments and
terminal output intentionally keep JSON expanded and indented for learning.
