# TypeScript examples

You have a backend, a service key and a search problem. Start with one working request, inspect
its output, then change one thing. These 44 small examples follow the same learning path as the
[Java](../java/README.md) and [Python](../python/README.md) examples.

These are **Node.js server-side HTTP examples**, using native `fetch` and a small typed helper.
They are not a TypeScript SDK. They use built-in Node APIs and have no dependency
on publishing the Java or Python SDK. Runtime requests follow the current public SDK contracts.

## Pick your first problem

| You need                                             | Start here                    |
| ---------------------------------------------------- | ----------------------------- |
| A results page for a project                         | `first_search`                |
| Company documents with Workplace access rules        | `search_workplace_as_service` |
| An answer grounded in those documents                | `ask_a_question`              |
| Each person to see only their own permitted evidence | `search_workplace_as_user`    |
| Users registered from your identity provider         | `register_users`              |

A **project** is a searchable collection. A **workspace** groups sources of company knowledge.
A **service key** identifies your backend, never a person. **Delegation** exchanges a user token
or signed assertion for an identity that acts as that person, within both identities' limits.

## Set up once

1. Install **Node.js 22.9+** and npm. Your administrator completes
   [What your administrator sets up](../../README.md#what-your-administrator-sets-up).
2. Copy [`.env.example`](../../.env.example) to `.env` at the repository root, then fill in the
   connection settings. Keep the service secret in your backend; never embed it in browser code.
3. Install the pinned development tools and compile:

   ```bash
   cd examples/typescript
   npm ci
   npm run build
   npm run list
   # Optional, when the key has provisioning permission:
   npm run example -- connect_and_check_access
   ```

Node loads the root `.env` file if present. Help needs no credentials:

```bash
npm run example -- first_search --help
npm run example -- first_search "star wars"
```

`connect_and_check_access` reads **Search Admin provisioning capabilities**. Success proves that
one call can authenticate. It does not prove project assignment, Workplace membership or
answering-agent configuration; test the actual feature next. If your key has no provisioning
permission, begin with the search example it is assigned to.

## Read one request before adding the next option

For a first project search, expect a line similar to:

```text
hits=5 mode=BM25 warning=none
{"title":"Example movie", ...}
```

The count is the number of received hits, not a reliable total match count. The mode reports
what actually ran. A warning can explain why the server adjusted a requested technique.
HTTP success is separate from Core application success: the helper requires envelope `code=1`.
Workplace and provisioning responses use direct bodies instead of the Core `result` envelope.

1. Predict what should change before running the next step.
2. Read its source: the opening comment explains the problem, prerequisites and expected output.
3. Run it; inspect the request and returned fields, not only whether the command exited.
4. Change one value, such as a genre, source or page offset, and compare the result.

The project examples assume the **Movies** sample schema (`title`, `overview`, `tagline`,
`genres.name`, `release_date`, `vote_average`, `original_language`, `status`). Adapt fields to
your project. The date-range step uses whole-year bounds for that sample; ask your administrator
which format your own schema accepts. See the [search guide](../java/SEARCH_GUIDE.md) for concepts.

## Learning path

Each row is a separate module under [`src`](src). Run it by name; query words are optional.
Source-filter and source-grant examples need `SMARTSEARCH_SOURCE_ID`; general Workplace calls do
not. Answers/chat need an answering agent. User registration examples use federated no-password
mode, linked to an existing identity-provider account; the Owner enables Register users first.

| Step | Example                                | Problem/capability                                                                                                                        | Command                                                   |
| ---- | -------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| 1    | `connect_and_check_access`             | What a service key is; connect and check what the key may do                                                                              | `npm run example -- connect_and_check_access`             |
| 2    | `first_search`                         | Build a request, send it, read the hits and the response envelope                                                                         | `npm run example -- first_search`                         |
| 3    | `choose_searched_and_returned_fields`  | Where the query is looked for versus what each hit returns                                                                                | `npm run example -- choose_searched_and_returned_fields`  |
| 4    | `page_through_results`                 | Paging with `from` and `size` (reranking off)                                                                                             | `npm run example -- page_through_results`                 |
| 5    | `sort_results`                         | Sort by a field instead of relevance (reranking off)                                                                                      | `npm run example -- sort_results`                         |
| 6    | `filter_by_exact_value`                | Yes/no conditions on an exact value, including properties inside lists                                                                    | `npm run example -- filter_by_exact_value`                |
| 7    | `filter_by_any_of_several_values`      | One field, any of several values (multi-select facets)                                                                                    | `npm run example -- filter_by_any_of_several_values`      |
| 8    | `filter_by_numeric_range`              | At least, at most, between                                                                                                                | `npm run example -- filter_by_numeric_range`              |
| 9    | `filter_by_date_range`                 | Date ranges                                                                                                                               | `npm run example -- filter_by_date_range`                 |
| 10   | `filter_where_field_exists`            | Only documents that have a value in a field                                                                                               | `npm run example -- filter_where_field_exists`            |
| 11   | `filter_by_full_text_match`            | Words anywhere in a text field                                                                                                            | `npm run example -- filter_by_full_text_match`            |
| 12   | `filter_by_exact_phrase`               | Words together and in order                                                                                                               | `npm run example -- filter_by_exact_phrase`               |
| 13   | `exclude_results`                      | Remove documents that match a condition                                                                                                   | `npm run example -- exclude_results`                      |
| 14   | `combine_filters_with_any_of`          | AND, OR and NOT together                                                                                                                  | `npm run example -- combine_filters_with_any_of`          |
| 15   | `boost_term_values`                    | Move preferred documents up without removing others                                                                                       | `npm run example -- boost_term_values`                    |
| 16   | `highlight_matches`                    | Show why a result matched                                                                                                                 | `npm run example -- highlight_matches`                    |
| 17   | `wildcard_and_prefix_search`           | `avat*` style queries                                                                                                                     | `npm run example -- wildcard_and_prefix_search`           |
| 18   | `correct_spelling`                     | Find results despite typos                                                                                                                | `npm run example -- correct_spelling`                     |
| 19   | `clean_up_user_input`                  | Trim spaces and remove special characters from typed text                                                                                 | `npm run example -- clean_up_user_input`                  |
| 20   | `turn_off_query_expansion`             | Stop related-word expansion for one request                                                                                               | `npm run example -- turn_off_query_expansion`             |
| 21   | `facet_counts`                         | Value counts, distinct counts, drill-down                                                                                                 | `npm run example -- facet_counts`                         |
| 22   | `keyword_vs_semantic_vs_hybrid`        | The three search techniques, and checking which one ran                                                                                   | `npm run example -- keyword_vs_semantic_vs_hybrid`        |
| 23   | `tune_hybrid_search`                   | How keyword and semantic results are fused                                                                                                | `npm run example -- tune_hybrid_search`                   |
| 24   | `limit_semantic_matches`               | How many close-in-meaning documents to consider and keep                                                                                  | `npm run example -- limit_semantic_matches`               |
| 25   | `rerank_results`                       | AI reranking on, off, and the project default                                                                                             | `npm run example -- rerank_results`                       |
| 26   | `tune_reranking`                       | How many candidates to rerank; a minimum score                                                                                            | `npm run example -- tune_reranking`                       |
| 27   | `precision_levels`                     | Broad versus strict matching                                                                                                              | `npm run example -- precision_levels`                     |
| 28   | `run_several_searches_at_once`         | Several searches in one round trip                                                                                                        | `npm run example -- run_several_searches_at_once`         |
| 29   | `search_through_use_case`              | Search with a saved configuration                                                                                                         | `npm run example -- search_through_use_case`              |
| 30   | `handle_errors_and_warnings`           | Invalid requests, refusals, and adjusted answers                                                                                          | `npm run example -- handle_errors_and_warnings`           |
| 31   | `search_workplace_as_service`          | Workplace concepts; search, query and chat; the documents that match                                                                      | `npm run example -- search_workplace_as_service`          |
| 32   | `filter_workplace_by_source_and_title` | Limit a search to one source; filter on a document field                                                                                  | `npm run example -- filter_workplace_by_source_and_title` |
| 33   | `ask_a_question`                       | Query in answer mode: the request, and reading answer, sources, citations, run ID, status                                                 | `npm run example -- ask_a_question`                       |
| 34   | `retrieve_sources_only`                | Query in retrieval-only mode: the evidence without an answer                                                                              | `npm run example -- retrieve_sources_only`                |
| 35   | `stream_an_answer`                     | Stream a query answer as it is written; events, errors, timeouts                                                                          | `npm run example -- stream_an_answer`                     |
| 36   | `answer_with_memory`                   | Memory modes: STANDARD versus AGENTIC, and when to use each                                                                               | `npm run example -- answer_with_memory`                   |
| 37   | `query_with_filters`                   | Answer from one source and documents that pass a filter                                                                                   | `npm run example -- query_with_filters`                   |
| 38   | `chat_with_follow_up_questions`        | Conversations: reuse the session for follow-ups                                                                                           | `npm run example -- chat_with_follow_up_questions`        |
| 39   | `stream_chat_answer`                   | Stream a chat answer                                                                                                                      | `npm run example -- stream_chat_answer`                   |
| 40   | `register_users`                       | Register users from your system, linked to your identity provider                                                                         | `npm run example -- register_users`                       |
| 41   | `register_users_with_workspace_access` | Register a user and give workspace and source access                                                                                      | `npm run example -- register_users_with_workspace_access` |
| 42   | `search_workplace_as_user`             | Act as a user from the user's own access token, and search                                                                                | `npm run example -- search_workplace_as_user`             |
| 43   | `create_user_assertion`                | Build and sign the user assertion your backend creates (JWT, RS256), and the public key set (JWKS) to publish; optionally sign in with it | `npm run example -- create_user_assertion`                |
| 44   | `sign_in_with_your_identity_provider`  | Act as a user who signed in to your identity provider; search and ask a question as them                                                  | `npm run example -- sign_in_with_your_identity_provider`  |

## Before acting as a user

- Step 42 uses `SMARTSEARCH_USER_ACCESS_TOKEN`, a current SmartSearch user token; Act as users
  must allow token exchange. Set this just before running, never save it in `.env`.
- Step 43 builds an **assertion**: a short signed statement identifying a user who has already
  authenticated in your application. It prints decoded claims and the **public JWKS** (public
  verification keys), never the JWT or private key. The default key is throw-away demonstration
  material. For a real exchange use your stable RSA key from `SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM`,
  with `SMARTSEARCH_ASSERTION_ISSUER` and `SMARTSEARCH_ASSERTION_KEY_ID` matching the registered
  issuer and public JWKS. Keep the private key in a secret store or key-management service.
- Step 44 uses a fresh `SMARTSEARCH_USER_ASSERTION`; Act as users must allow JWT grant and trust
  that issuer. Assertions last 120 seconds and may be accepted only once. Regenerate for each
  exchange; do not retry a consumed assertion.

```bash
npm run example -- create_user_assertion sdk-example-user-1
# Only after the Owner trusts your stable public key and the user has been registered:
npm run example -- create_user_assertion sdk-example-user-1 --sign-in "travel policy"
```

Registration alone does not grant delegated access. The delegated helper exposes only Workplace
search/query/chat. Use Workplace for managed company documents so authorization stays in the
public Workplace flow. Workspace load keys serve ingestion, not these search examples.

## Understand a failure before retrying

| Outcome                              | Next check                                                                                       |
| ------------------------------------ | ------------------------------------------------------------------------------------------------ |
| Missing environment setting          | Set the named variable from `.env.example`. Never paste its secret into an issue.                |
| `TOKEN_ACQUISITION_FAILED`           | Check auth URL/realm/key; for a user exchange, also expiry, registered issuer and allowed grant. |
| HTTP 403/404                         | Check the assigned project or workspace/source and the saved integration ID with your Owner.     |
| `SEARCH_APPLICATION_FAILED`          | HTTP completed but Core refused the search; check project/schema/permission.                     |
| Zero hits/documents                  | Valid outcome: check query, selected fields, filters and your permitted evidence.                |
| Provisioning `PARTIAL` or `FAILED`   | Read each item state/error code. A finished job is not proof that every user succeeded.          |
| `PROVISIONING_JOB_TIMEOUT`           | Continue polling the submitted job. Do not create a replacement job just because the wait ended. |
| Stream failure/incomplete completion | Discard provisional text; a valid `run.completed` is required. Do not replay/reconnect silently. |

The helper discards raw error bodies and unexpected exception messages so credentials and
customer data cannot accidentally appear in its error output. Successful example output can
contain your document text; handle it according to your application's normal data policy.

Paging and field sorting disable reranking. Offset pages can still shift when the index changes
or scores tie. Multi-search returns one list per query; the example slices each list because a
server may ignore individual sizes. Standard memory avoids storing shared service-identity
context; the agentic memory comparison intentionally records a question under the service.

## Read and change the teaching helper

[`client.ts`](src/client.ts) owns authentication and the narrow public HTTP calls. Service tokens
refresh before expiry and retry once after a 401; delegated tokens do not renew or fall back to
service credentials. Redirects are refused. Responses are size-bounded. Streams support fragmented
UTF-8 and LF/CRLF/CR framing, retain chat session IDs, and require a valid final completion.

[`contracts.ts`](src/contracts.ts) projects typed requests onto a closed public JSON shape;
extra runtime properties are not sent. [`common.ts`](src/common.ts) reads settings, prints results
and polls registration. These helpers are teaching code; they do not reproduce every SDK feature
or provide a production support contract. The default request/stream lifetime is 60 seconds and
the response limit is 2 MiB; choose limits appropriate to your backend before adapting them.

TSDoc comments explain intent, inputs, results and failures without duplicating TypeScript types.
Types follow the [official TypeScript declaration guidance](https://www.typescriptlang.org/docs/handbook/declaration-files/do-s-and-don-ts.html):
primitive types, `unknown` at untrusted boundaries, meaningful callbacks and union parameters.
The guidance informs helper signatures; this repository does not publish a declaration package.

```bash
npm run typecheck
npm test
```

Tests use local HTTP fixtures and offline keys. They check public routes and OAuth forms, closed
payloads, error redaction, delegation, job timeouts, fragmented streaming and RS256 signatures,
and load/run all 44 examples without production credentials.
