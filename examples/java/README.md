# Java examples

One Maven project; each example is a single, self-contained class in
[`src/main/java/examples`](src/main/java/examples) that prints a short, readable result.

## Setup

1. **Java 21+ and Maven 3.9+.**
2. **The SDK** `co.smartsearchai:smartsearch-ai`. The version is set by the
   `smartsearch-ai.version` property in [`pom.xml`](pom.xml). Until the SDK is published to
   Maven Central, install the jar and pom you received from SmartSearch AI into your local
   Maven repository:

   ```bash
   mvn install:install-file -Dfile=smartsearch-ai-<version>.jar -DpomFile=smartsearch-ai-<version>.pom
   ```

3. **Settings.** Copy [`.env.example`](../../.env.example) to `.env` at the repository root and
   fill it in. Every URL, key and ID is read from environment variables
   ([`Config.java`](src/main/java/examples/Config.java)); nothing is hard-coded.

## Run an example

```bash
cd examples/java
./run.sh Ex01ServiceConnect                 # loads ../../.env, then runs the example
./run.sh P01BasicSearch "star wars"         # most examples take an optional query
```

`run.sh` is a thin wrapper around Maven. Without it, export the variables yourself and run:

```bash
mvn -q compile exec:java -Dexec.mainClass=examples.P01BasicSearch -Dexec.args="'star wars'"
```

On failure an example prints one `ERROR` line with the HTTP status and the server's error code,
and exits with status 1. It never prints tokens or secrets.

## 00 Administrator setup (one time, no code)

A SmartSearch AI Owner does this in the Admin UI. Your application never holds an Owner or
administrator token.

1. **Create a service key** for your integration, assign it the workspaces and projects it
   may use, and give it the permissions it needs (for example query read, workspace read, and
   scoped provisioning for examples 02-03).
2. **Register your identity provider** (needed for example 05): account menu
   **⋮ → Identity providers**.
3. **Let the key act as users** (needed for examples 04-05): **Edit service account →
   Act as users**. This sets the identity providers, the allowed operations (search, query,
   chat) and the workspaces and sources the key may act on. Example 05 uses the JWT
   authorization grant; example 04 additionally needs token exchange enabled for the key.
4. **Provisioning integration** (needed for examples 02-03): your SmartSearch AI contact sets
   up the integration that lets your key create users and grant workspace access, and gives you
   its integration ID and your tenant ID.

### Creating users without code (Admin UI)

An administrator can also create a user in the Admin UI's user management. The user receives
a temporary password and sets their own password at first sign-in. Use this for a handful of
users; use examples 02-03 to register users from your own system with no SmartSearch AI
password at all.

## Examples

### Principal Exchange and Workplace

| # | Class | What it shows | Needs |
|---|---|---|---|
| 01 | `Ex01ServiceConnect` | Connect with a service key; read provisioning capabilities | Service key |
| 02 | `Ex02RegisterUsers` | Register users (`UPSERT_USERS`) linked to your identity provider; idempotent job; poll; list principals | Provisioning integration, `SMARTSEARCH_INTEGRATION_ID`, `SMARTSEARCH_TENANT_ID` |
| 03 | `Ex03OnboardWithAccess` | Register a user with a workspace membership and a source grant (`ONBOARD_USERS`) | As 02, plus `SMARTSEARCH_WORKSPACE_ID`, `SMARTSEARCH_SOURCE_ID` |
| 04 | `Ex04SearchAsUser` | Search Workplace as a user, from the user's access token (token exchange) | "Act as users" with token exchange; `SMARTSEARCH_USER_ACCESS_TOKEN` |
| 05 | `Ex05PartnerIdpLogin` | User signs in to your identity provider; search and answer as that user (JWT authorization grant) | Registered identity provider; "Act as users" with JWT grant; `SMARTSEARCH_USER_ASSERTION` |
| 06 | `Ex06StreamingChat` | Streaming chat: print the answer as it is generated | Key is a workspace member |
| 07 | `Ex07ServiceSearch` | Search and answer as your service, memory off | Key is a workspace member |

### Workplace depth

| # | Class | What it shows | Needs |
|---|---|---|---|
| W1 | `W1SourceAndFieldFilters` | Limit a search to one source and filter on the title | `SMARTSEARCH_SOURCE_ID` |
| W2 | `W2RetrievalThenAnswer` | Retrieval only (evidence, no model call), then a generated answer with sources | Workspace with an answering agent |
| W3 | `W3ChatFollowUp` | Multi-turn chat: reuse the session so follow-ups keep context | Workspace with an answering agent |

### Project search

The project examples use the **Movies sample dataset** (fields `title`, `overview`, `tagline`,
`genres`, `release_date`, `vote_average`, `original_language`, `status`). Change the field
names to run them on your own data. All need `SMARTSEARCH_PROJECT_ID`, a project your key is
assigned to.

| # | Class | What it shows | Needs |
|---|---|---|---|
| P1 | `P01BasicSearch` | Query, searched and returned fields, sort, paging | |
| P2 | `P02Filters` | Required, any-of and excluded filters; term, terms, range, exists, phrase | |
| P3 | `P03Facets` | Terms and cardinality facets, then drill down | Project **without** document security |
| P4 | `P04Boosts` | Boost a field value without filtering | |
| P5 | `P05Highlighting` | Matching fragments with the query terms marked | |
| P6 | `P06Wildcard` | `avat*` style wildcard queries | |
| P7 | `P07SpellingCorrection` | Misspelled queries, with and without correction | Spelling correction enabled |
| P8 | `P08Hybrid` | Hybrid keyword + vector search fused with RRF; detect a fallback | Embeddings |
| P9 | `P09Semantic` | Pure vector (semantic) search | Embeddings |
| P10 | `P10Reranking` | Reranking on, off, and the project default | Reranker configured |
| P11 | `P11Precision` | Precision levels 1 (broad) to 11 (strict) | Precision template |
| P12 | `P12DirtyInput` | Trim whitespace and strip special characters from user input | |
| P13 | `P13MultiSearch` | Several queries in one round trip | |
| P14 | `P14UseCaseSearch` | Search through a named use case | `SMARTSEARCH_USECASE_ID` |

## Things worth knowing

- **Check what actually ran.** AI features have preconditions. When one is missing the server
  falls back, and says so: read `SearchResult.effectiveNeuralMode()` and `warning()` (see P8).
- **Memory belongs to an identity.** Every request made with one service key shares one memory.
  When answering for many people as your service, send `MemoryMode.STANDARD`; to give each person
  their own memory, act as them (04, 05).
- **Sorting and reranking.** A reranker reorders the top hits by relevance, which overrides a
  field sort and the page offset. Turn it off for sorted or deep-paged requests (P1).
- **User tokens are short-lived and not refreshed.** `UserWorkplace.expiresAt()` tells you when
  to get a new one. JWT assertions are single-use.
- **Timeouts.** Answers and chat can take tens of seconds; the SDK's default read timeout is
  60 seconds (`SmartSearchAi.builder().readTimeout(...)`).
