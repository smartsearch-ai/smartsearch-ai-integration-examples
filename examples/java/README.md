# Java examples

You have an application. Now you want to add a search box, answer questions about company
documents, or give your users access to those documents. Start with the task you need:

| Your first goal | Start here | What you should see |
|---|---|---|
| Search a regular project | `FirstSearch` (step 2) | A successful response and matching document fields |
| Search company documents in a workspace | `SearchWorkplaceAsService` (step 31) | A document count and titles your service may read |
| Register your application's users | `ConnectAndCheckAccess`, then `RegisterUsers` (steps 1, 40) | Provisioning capabilities, then a job with an outcome for each user |

You do **not** need to register users to try a service-key search. Per-user access comes later.
Step 1 checks the user-registration API; passing it does not prove access to a project or
workspace. Use the matching search example to check that access.

This is one Maven project. Each example is a single class that teaches one thing and prints a
short result. The classes are grouped by topic under
[`src/main/java/examples`](src/main/java/examples):

| Package | What is in it |
|---|---|
| `gettingstarted` | Connecting with your service key |
| `search` | Project search: queries, filters, sorting, facets, AI search options |
| `workplace` | Workplace: search, answers and chat over your company documents, as your service or as a user |
| `users` | Registering your users and giving them access |

The small helper classes (`SmartSearchConnectionConfig`, `ExampleRunner`, and the result
printers) are example plumbing, not part of the SDK.

Every example's source starts with a plain-English explanation of the idea, when to use it and
what it needs, and every call to SmartSearch AI is commented with the request it sends, what the
parameters mean, what comes back and what can go wrong. Read the code alongside the output.

Use a simple learning loop: **predict → run → inspect → change one thing**. For example, before
adding a language filter, predict which results will disappear. Then run the filtered search
and compare. You learn more from that comparison than from running all 44 examples in a row.

## How SmartSearch AI fits together

- Your **backend** holds a **service key** (your application's identity) and calls SmartSearch AI
  through this SDK. The SDK turns the key into short-lived access tokens for you.
- A **project** is a searchable collection of your documents. You search it with **SSPL**
  requests built by `SearchQuery.builder()`. See [SEARCH_GUIDE.md](SEARCH_GUIDE.md).
- **Workplace** searches and answers over company knowledge grouped in **workspaces**. Each
  workspace has **sources** (connected document collections) and an answering agent.
- To respect each person's access, your backend can **act as a user**. Users are first
  **registered** from your system and linked to their account in your own identity provider, so
  they never get a SmartSearch AI password.

## Setup

1. **Java 21+ and Maven 3.9+.**
2. **The SDK** `co.smartsearchai:smartsearch-ai`. The version is set by the
   `smartsearch-ai.version` property in [`pom.xml`](pom.xml). Until the SDK is published to
   Maven Central, install the jar and pom you received from SmartSearch AI into your local
   Maven repository:

   ```bash
   mvn install:install-file -Dfile=smartsearch-ai-<version>.jar -DpomFile=smartsearch-ai-<version>.pom
   ```

   Replace `<version>` with the version of the supplied files; it is a placeholder, not shell
   syntax to paste literally. The version must match `smartsearch-ai.version` in `pom.xml`
   (currently `0.1.0-SNAPSHOT`). The internal release number is not the public Maven version.
   If you have not received the preview artifacts, obtain them before continuing; a failed
   dependency download is not an authentication problem.

3. **Settings.** Copy [`.env.example`](../../.env.example) to `.env` at the repository root and
   fill it in. Every URL, key and ID is read from environment variables
   ([`SmartSearchConnectionConfig.java`](src/main/java/examples/SmartSearchConnectionConfig.java)); nothing is hard-coded.

   The connection helper reads all three base URLs, the realm, and the service-key ID/secret.
   Then fill in the ID for the task you chose. Registration IDs are only needed for registration;
   a source ID is only needed when an example filters or grants access to a particular source.

### What each setting is

All values come from your **SmartSearch AI administrator**. The hosts below are placeholders.

| Variable | Example value | What it is |
|---|---|---|
| `SMARTSEARCH_API_BASE_URL` | `https://api.your-company.example.com` | SmartSearch API gateway. Project search and Workplace calls go here. |
| `SMARTSEARCH_ADMIN_BASE_URL` | `https://admin.your-company.example.com` | Search Admin. User provisioning (registering users) goes here. |
| `SMARTSEARCH_AUTH_BASE_URL` | `https://auth.your-company.example.com` | Identity server base URL: the part **before** `/realms/...`. |
| `SMARTSEARCH_REALM` | `your-realm` | The name of your login realm on that identity server. |
| `SMARTSEARCH_CLIENT_ID` | `svc-your-key-id` | Your service key's ID (starts with `svc-`). |
| `SMARTSEARCH_CLIENT_SECRET` | *(secret)* | Your service key's secret. Never commit it. |
| `SMARTSEARCH_PROJECT_ID` | `your-project-id` | The project the search examples use. |
| `SMARTSEARCH_USECASE_ID` | `your-use-case-id` | Optional: a use case of that project. |
| `SMARTSEARCH_WORKSPACE_ID` | `your-workspace-id` | The Workplace workspace the Workplace examples use. |
| `SMARTSEARCH_SOURCE_ID` | `your-source-id` | Optional for general Workplace calls; required for source-filter and source-grant examples. |
| `SMARTSEARCH_INTEGRATION_ID` | `your-integration-id` | Your provisioning integration (user registration). |
| `SMARTSEARCH_TENANT_ID` | `your-tenant-id` | Your organisation's tenant, where users are registered. |

The SDK builds the token endpoint from the auth URL and the realm:

```
https://auth.your-company.example.com/realms/your-realm/protocol/openid-connect/token
```

The requests the examples send, relative to those URLs:

| Area | Request |
|---|---|
| Token (automatic) | `POST {SMARTSEARCH_AUTH_BASE_URL}/realms/{realm}/protocol/openid-connect/token` |
| Project search | `POST {SMARTSEARCH_API_BASE_URL}/core/projects/{projectId}/search` |
| Multi-search | `POST {SMARTSEARCH_API_BASE_URL}/core/projects/{projectId}/mSearch` |
| Use-case search | `POST {SMARTSEARCH_API_BASE_URL}/core/usecases/{usecaseId}/search` |
| Workplace | `POST {SMARTSEARCH_API_BASE_URL}/workspace/v1/workspaces/{workspaceId}/search`, `/query`, `/chat` |
| User provisioning | `{SMARTSEARCH_ADMIN_BASE_URL}/search-admin/api/provisioning/v1/...` (`capabilities`, `jobs`, `integrations/{id}/principals`) |

## Run an example

```bash
cd examples/java
./run.sh FirstSearch "star wars"            # an existing Movies project your key can search
# Or, for an existing workspace:
./run.sh SearchWorkplaceAsService "refund"  # use a word from YOUR documents
```

`run.sh` finds the class in any package, compiles it with Maven, then launches Java. It
preserves your shell arguments, including apostrophes and quoted phrases. Without it,
export the variables yourself and run:

```bash
mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/example-classpath.txt
java -cp "target/classes:$(cat target/example-classpath.txt)" examples.search.FirstSearch "star wars"
```

On failure an example prints one `ERROR` line with the HTTP status and the server's message, and
exits with status 1. It never prints tokens or secrets.

### Read your first result

`FirstSearch` prints the request, a response summary, then document fields. A summary might look
like this **illustrative** output; the actual mode, warning and documents depend on your project:

```text
code=1 message=Success searchId=present mode=BM25 warning=null
```

`code=1` is success. A **hit** is one returned document. No document lines can mean the request
succeeded but found no matches; try a word you know exists before changing your credentials.
HTTP 200 alone is not enough: a search envelope with `code=0` is still a failure. An `ERROR`
line is a different outcome from zero hits: use the troubleshooting table below.

Now change `.size(5)` to `.size(2)` in `FirstSearch.java`. Predict the effect, run it again, and
count the returned documents. Then restore it and try `ChooseSearchedAndReturnedFields`.
Keep the query fixed while changing one option so you can see what that option does.

## Learning path

Choose the part that matches your task, then work through its steps in order. Every step is one
class. Project search and Workplace are separate paths; the user-registration path adds
per-user identity when your application needs it.

### Part 1: Getting started

| Step | Example | What you learn | Run |
|---|---|---|---|
| 1 | `ConnectAndCheckAccess` | What a service key is; connect and check what the key may do | `./run.sh ConnectAndCheckAccess` |

### Part 2: Project search

The search examples use the **Movies sample data set** (fields `title`, `overview`, `tagline`,
`genres`, `release_date`, `vote_average`, `original_language`, `runtime`, `status`). Change the
field names to run them on your own data. All need `SMARTSEARCH_PROJECT_ID`, a project your key
is assigned to. [SEARCH_GUIDE.md](SEARCH_GUIDE.md) explains the concepts.

| Step | Example | What you learn | Run |
|---|---|---|---|
| 2 | `FirstSearch` | Build a request, send it, read the hits and the response envelope | `./run.sh FirstSearch "star wars"` |
| 3 | `ChooseSearchedAndReturnedFields` | Where the query is looked for versus what each hit returns | `./run.sh ChooseSearchedAndReturnedFields galaxy` |
| 4 | `PageThroughResults` | Paging with `from` and `size` (reranking off) | `./run.sh PageThroughResults love` |
| 5 | `SortResults` | Sort by a field instead of relevance (reranking off) | `./run.sh SortResults love` |
| 6 | `FilterByExactValue` | Yes/no conditions on an exact value, including properties inside lists | `./run.sh FilterByExactValue love` |
| 7 | `FilterByAnyOfSeveralValues` | One field, any of several values (multi-select facets) | `./run.sh FilterByAnyOfSeveralValues love` |
| 8 | `FilterByNumericRange` | At least, at most, between | `./run.sh FilterByNumericRange love` |
| 9 | `FilterByDateRange` | Date ranges | `./run.sh FilterByDateRange love` |
| 10 | `FilterWhereFieldExists` | Only documents that have a value in a field | `./run.sh FilterWhereFieldExists love` |
| 11 | `FilterByFullTextMatch` | Words anywhere in a text field | `./run.sh FilterByFullTextMatch love paris` |
| 12 | `FilterByExactPhrase` | Words together and in order | `./run.sh FilterByExactPhrase` |
| 13 | `ExcludeResults` | Remove documents that match a condition | `./run.sh ExcludeResults love` |
| 14 | `CombineFiltersWithAnyOf` | AND, OR and NOT together | `./run.sh CombineFiltersWithAnyOf space` |
| 15 | `BoostTermValues` | Move preferred documents up without removing others | `./run.sh BoostTermValues love` |
| 16 | `HighlightMatches` | Show why a result matched | `./run.sh HighlightMatches princess` |
| 17 | `WildcardAndPrefixSearch` | `avat*` style queries | `./run.sh WildcardAndPrefixSearch "avat*"` |
| 18 | `CorrectSpelling` | Find results despite typos | `./run.sh CorrectSpelling terminater` |
| 19 | `CleanUpUserInput` | Trim spaces and remove special characters from typed text | `./run.sh CleanUpUserInput` |
| 20 | `TurnOffQueryExpansion` | Stop related-word expansion for one request | `./run.sh TurnOffQueryExpansion car` |
| 21 | `FacetCounts` | Value counts, distinct counts, drill-down | `./run.sh FacetCounts war` |
| 22 | `KeywordVsSemanticVsHybrid` | The three search techniques, and checking which one ran | `./run.sh KeywordVsSemanticVsHybrid` |
| 23 | `TuneHybridSearch` | How keyword and semantic results are fused | `./run.sh TuneHybridSearch` |
| 24 | `LimitSemanticMatches` | How many close-in-meaning documents to consider and keep | `./run.sh LimitSemanticMatches` |
| 25 | `RerankResults` | AI reranking on, off, and the project default | `./run.sh RerankResults "wizard school"` |
| 26 | `TuneReranking` | How many candidates to rerank; a minimum score | `./run.sh TuneReranking "wizard school"` |
| 27 | `PrecisionLevels` | Broad versus strict matching | `./run.sh PrecisionLevels "dark knight rises"` |
| 28 | `RunSeveralSearchesAtOnce` | Several searches in one round trip | `./run.sh RunSeveralSearchesAtOnce` |
| 29 | `SearchThroughUseCase` | Search with a saved configuration | `./run.sh SearchThroughUseCase "star wars"` |
| 30 | `HandleErrorsAndWarnings` | Invalid requests, refusals, and adjusted answers | `./run.sh HandleErrorsAndWarnings` |

### Part 3: Workplace as your service

All need `SMARTSEARCH_WORKSPACE_ID`, a workspace your key is a member of; answers also need an
answering agent in it. Which call to use:

- **search**: a list of matching documents, no language model. Use it for a results page.
- **query**: one question, one answer written from the documents with its sources, or only the
  sources (retrieval only). Use it for a question box, or to feed your own model.
- **chat**: like query, in a conversation where follow-up questions build on earlier turns.

| Step | Example | What you learn | Run |
|---|---|---|---|
| 31 | `SearchWorkplaceAsService` | Workplace concepts; search, query and chat; the documents that match | `./run.sh SearchWorkplaceAsService "blood pressure"` |
| 32 | `FilterWorkplaceBySourceAndTitle` | Limit a search to one source; filter on a document field | `./run.sh FilterWorkplaceBySourceAndTitle "blood pressure" pressure` |
| 33 | `AskAQuestion` | Query in answer mode: the request, and reading answer, sources, citations, run ID, status | `./run.sh AskAQuestion "What is a normal blood pressure?"` |
| 34 | `RetrieveSourcesOnly` | Query in retrieval-only mode: the evidence without an answer | `./run.sh RetrieveSourcesOnly "What is a normal blood pressure?"` |
| 35 | `StreamAnAnswer` | Stream a query answer as it is written; events, errors, timeouts | `./run.sh StreamAnAnswer "What causes high blood pressure?"` |
| 36 | `AnswerWithMemory` | Memory modes: STANDARD versus AGENTIC, and when to use each | `./run.sh AnswerWithMemory "What is a normal blood pressure?"` |
| 37 | `QueryWithFilters` | Answer from one source and documents that pass a filter | `./run.sh QueryWithFilters "What is a normal blood pressure?" pressure` |
| 38 | `ChatWithFollowUpQuestions` | Conversations: reuse the session for follow-ups | `./run.sh ChatWithFollowUpQuestions "Who was President Kennedy?" "When was he born?"` |
| 39 | `StreamChatAnswer` | Stream a chat answer | `./run.sh StreamChatAnswer "What causes high blood pressure?"` |

### Part 4: Your users

These examples submit real registration jobs when pointed at a live environment. Use your
administrator's test integration and the demo identities shown in the source. A submitted job
is not a finished job: wait for its terminal state and inspect each item's outcome. `PARTIAL`
means some items failed. Reuse the idempotency key only with the same request; changing the
users or grants requires a new key. An idempotency key identifies one submission, not a login.

| Step | Example | What you learn | Needs | Run |
|---|---|---|---|---|
| 40 | `RegisterUsers` | Register users from your system, linked to your identity provider | Provisioning integration | `./run.sh RegisterUsers` |
| 41 | `RegisterUsersWithWorkspaceAccess` | Register a user and give workspace and source access | As 40, allowed to grant the workspace and source | `./run.sh RegisterUsersWithWorkspaceAccess` |
| 42 | `SearchWorkplaceAsUser` | Act as a user from the user's own access token, and search | "Act as users" with token exchange; `SMARTSEARCH_USER_ACCESS_TOKEN` | `./run.sh SearchWorkplaceAsUser` |
| 43 | `CreateUserAssertion` | Build and sign the user assertion your backend creates (JWT, RS256), and the public key set (JWKS) to publish; optionally sign in with it | Your signing key; for `--sign-in`, issuer and JWKS URL registered by an Owner | `./run.sh CreateUserAssertion sdk-example-user-1` |
| 44 | `SignInWithYourIdentityProvider` | Act as a user who signed in to your identity provider; search and ask a question as them | Registered identity provider; "Act as users" with JWT grant; `SMARTSEARCH_USER_ASSERTION` | `./run.sh SignInWithYourIdentityProvider` |

## Administrator setup (one time, no code)

A SmartSearch AI Owner does this in the Admin UI. Your application never holds an Owner or
administrator login or token.

Follow [What your administrator sets up](../../README.md#what-your-administrator-sets-up):
identity provider → service key → **Register users** → **Act as users**. The Owner configures
and copies the integration ID directly in **Edit service account → Register users**; use the
federated, no-password mode for steps 40-41. The key, users and allowed workspaces must share
the tenant whose ID you put in `SMARTSEARCH_TENANT_ID`.

### Creating users without code (Admin UI)

An administrator can also create users in the Admin UI's user management. Each user receives a
temporary password and sets their own password at first sign-in. Use this for a handful of
users; use steps 40-41 to register users from your own system with no SmartSearch AI password
at all.

## Things worth knowing

- **Check what actually ran.** AI features have preconditions. When one is missing the server
  falls back and says so: read `SearchResult.effectiveNeuralMode()` and `warning()`
  (steps 22 and 30).
- **Reranking and ordering.** The reranker reorders the top hits by relevance. That overrides a
  field sort and ignores the page offset, so turn it off (`rerank(false)`) for sorted or paged
  lists (steps 4, 5) and when a boost must decide the order (step 15).
- **Counting results.** Count the hits you received. The response does not carry a reliable total
  number of matches, so do not show "N results" from it.
- **Multi-search returns more hits than asked.** Take the first n of each list yourself (step 28).
- **Facets need a project without document security** (step 21).
- **Memory belongs to an identity.** Every request made with one service key shares one memory.
  When answering for many people as your service, send `MemoryMode.STANDARD`; to give each person
  their own memory, act as them (steps 42, 44). Set `memoryMode` explicitly on every Workplace request: the
  server default can store memory (step 36).
- **User tokens are short-lived and not renewed.** `UserWorkplace.expiresAt()` tells you when to
  get a new one. Assertions from your identity provider are single-use.
- **Timeouts.** Answers and chat can take tens of seconds; the SDK's default read timeout is
  60 seconds (`SmartSearchAi.builder().readTimeout(...)`).

## When the result surprises you

| What you see | What to check next |
|---|---|
| Maven cannot resolve `smartsearch-ai` | Install the supplied public preview jar and pom; check the version in `pom.xml`. No API request has happened yet. |
| A required environment variable is missing | Fill in that exact name in the root `.env`. IDs select resources; they are not credentials. |
| Token acquisition fails | Check the auth base URL, realm and service key with your administrator. Never paste the secret or token into a bug report. |
| HTTP 403 or `SCOPE_DENIED` | Confirm the key/user is allowed to use that resource and operation, and that the resource is active. Registration permission alone does not grant search access. |
| A successful search returns no hits | Try a known word from an accessible document; check the project/workspace ID and any filters. The Movies examples require the Movies schema. |
| A requested search mode changes | Read `effectiveNeuralMode()` and `warning()`; check the project's configured features before tuning more parameters. |
| A registration job times out | Keep its job ID and check that same job again. A timeout does not mean the server cancelled it. |
| A job finishes as `PARTIAL` or `FAILED` | Read the item outcomes and error codes. Do not treat a terminal state as proof every user was registered. |

When asking for help, include the example name, HTTP status/error code and search or job ID.
Keep credentials and private document content out of the report.

## Check changes locally

After installing the SDK preview, run these from `examples/java`. The Python checks use only
the standard library and synthetic credentials; HTTP fixtures listen on loopback.

```bash
python3 tests/test_runner.py
mvn -q compile dependency:build-classpath -Dmdep.outputFile=target/example-classpath.txt
python3 tests/test_public_examples.py
javac -cp "target/classes:$(cat target/example-classpath.txt)" -d target/test-classes tests/OfflineChecks.java
java -cp "target/test-classes:target/classes:$(cat target/example-classpath.txt)" examples.search.OfflineChecks
```

These cover argument preservation, failed search envelopes and final streamed answers.

## Writing another example

Follow the [Cornell Java style guide](https://www.cs.cornell.edu/courses/JavaAndDS/JavaStyle.html#Indentation)
and this repository's `.editorconfig`: four-space Java indentation, consistent braces, and
readable lines. Describe a method's purpose and contract in Javadoc. Use inline comments to
explain decisions or meaningful groups of statements, rather than narrating each Java keyword.

Teach one decision per example. Give the reader a problem, prerequisites, a run command,
something specific to check in the result, and one small change to try. Keep helper mechanics
separate from the API concept, and use only the curated public integration surface.
