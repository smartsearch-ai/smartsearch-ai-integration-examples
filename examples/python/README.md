# Python examples

You want to add search to your application. Start with one result you can inspect, then add
one feature at a time. Choose your first task:

| Your goal | Start here | Check the result |
|---|---|---|
| Search a regular project | `first_search` (step 2) | Matching document fields and the effective search mode |
| Search a workspace's documents | `search_workplace_as_service` (step 31) | A document count and titles your service may read |
| Register your application's users | `connect_and_check_access`, then `register_users` (steps 1, 40) | Provisioning capabilities, then an outcome for each submitted user |

Registration is not required for service-key search. Step 1 checks the registration API; it
does not prove access to every project or workspace. Check those with the corresponding search
example.

Each script teaches one idea. The learning path matches the [Java examples](../java), so you
can compare the languages without learning another API. The scripts are grouped by topic under
[`examples`](examples):

| Folder | What is in it |
|---|---|
| `gettingstarted` | Connecting with your service key |
| `search` | Project search: queries, filters, sorting, facets, AI search options |
| `workplace` | Workplace: search, answers and chat over your company documents, as your service or as a user |
| `users` | Registering your users and giving them access |

[`examples/_common.py`](examples/_common.py) (settings, the one-line error output and the result
printers) is example plumbing, not part of the SDK.

Every example starts with a plain-English explanation of the idea, when to use it and what it
needs, and every call to SmartSearch AI is commented with the request it sends, what the
parameters mean, what comes back and what can go wrong. Read the code alongside the output.

For each step: **predict → run → inspect → change one thing**. Keep your query unchanged while
adding a filter, for example, so you can see the filter's effect. You do not need to run all
44 examples before building something useful.

## How SmartSearch AI fits together

- Your **backend** holds a **service key** (your application's identity) and calls SmartSearch AI
  through this SDK. The SDK turns the key into short-lived access tokens for you.
- A **project** is a searchable collection of your documents. You search it with **SSPL**
  requests built by `SearchQuery(...)`. See the [search guide](../java/SEARCH_GUIDE.md).
- **Workplace** searches and answers over company knowledge grouped in **workspaces**. Each
  workspace has **sources** (connected document collections) and an answering agent.
- To respect each person's access, your backend can **act as a user**. Users are first
  **registered** from your system and linked to their account in your own identity provider, so
  they never get a SmartSearch AI password.

## Setup

1. **Python 3.10+**, ideally in a virtual environment:

   ```bash
   cd examples/python
   python3 -m venv .venv && . .venv/bin/activate
   ```

2. **The SDK preview.** Public package publication is pending. Install the wheel supplied by
   SmartSearch AI into that same virtual environment:

   ```bash
   python -m pip install /path/to/smartsearch_ai-<version>-py3-none-any.whl
   ```

   Replace the path and `<version>` with the actual file you received; do not paste the angle
   brackets literally. If you do not have the wheel yet, obtain it before continuing. The
   public package version differs from the internal release number. Once the public package
   is published, installation by package name will be available. Its dependencies include
   `httpx` and `pydantic`.

   Step 43 (`create_user_assertion`) signs a token with RSA, which Python's standard library
   cannot do; for that step also run `pip install cryptography`.

3. **Settings.** Copy [`.env.example`](../../.env.example) to `.env` at the repository root and
   fill it in. Every URL, key and ID is read from environment variables
   ([`_common.py`](examples/_common.py)); nothing is hard-coded.

   The helper reads all three base URLs, the realm and your service-key ID/secret. Add the
   resource ID needed for your chosen task. A source ID is needed only for source-specific
   filtering or access grants; integration and tenant IDs are needed for registration.

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

The requests the examples send are the same as in Java; see the
[request table in the Java README](../java/README.md#what-each-setting-is).

## Run an example

```bash
cd examples/python
./run.sh first_search "star wars"           # an existing Movies project your key can search
# Or, for an existing workspace:
./run.sh search_workplace_as_service "refund" # choose a word from YOUR documents
```

`run.sh` finds the script in any folder and runs it with `python3` from your `PATH` (set
`PYTHON=/path/to/python` to use another). Without it, export the variables yourself and run
the script as a module from this folder:

```bash
python3 -m examples.search.first_search "star wars"
```

On failure an example prints one `ERROR` line with the HTTP status and the server's message, and
exits with status 1. It never prints tokens or secrets.

### Read your first result

A **hit** is one returned document. `first_search` prints its request, a response summary,
then document fields. This output is **illustrative**; your documents and search mode may differ:

```text
code=1 message=Success searchId=present mode=BM25 warning=None
  A matching movie (2000, rated 7.5)
```

A successful response with zero hits is different from a refused request. Try a known word
from an accessible document before changing credentials. HTTP 200 alone is not sufficient:
the search envelope must also report success (`code=1`).

**Try it:** change `size=5` to `size=2` in `first_search.py`, predict the output, and run again.
Then restore it and try `choose_searched_and_returned_fields`. Changing displayed fields should
not by itself change which documents match.

## Java and Python names

The Python SDK has the same calls as the Java SDK, with Python naming. Requests are immutable
objects built with keyword arguments instead of builders. The table abbreviates SDK names;
the examples use `import smartsearch_ai` and qualify them, for example
`smartsearch_ai.SearchQuery(...)`, so you can see where each name comes from:

| Java | Python |
|---|---|
| `SmartSearchAi.builder().apiUrl(...).serviceKey(id, secret).build()` | `SmartSearchAi(api_url=..., client_id=..., client_secret=...)` |
| `SearchQuery.builder().q("x").responseFields("title").size(5).build()` | `SearchQuery("x", response_fields=["title"], size=5)` |
| `.from(10)` | `from_=10` (`from` is a Python keyword) |
| `.filter(f)`, `.anyOf(f1, f2)`, `.exclude(f)` | `filters=[f, AnyOf(f1, f2)]`, `exclude=[f]` |
| `.termBoost(field, value, weight)` | `boosts=[TermBoost(field, value, weight)]` |
| `.termsAgg(...)`, `.cardinalityAgg(...)` | `aggs=[TermsAgg(...), CardinalityAgg(...)]` |
| `.normalization(n, c)` | `normalization=n, combination=c` |
| `.neuralMatches(top, total)` | `neural_matches=(top, total)` |
| `WorkspaceQueryRequest.builder(q).mode(...).addSourceId(id).build()` | `WorkspaceQueryRequest(query=q, mode=..., source_ids=[id])` |
| `stream.next()` until empty | `for event in stream:` |
| `SearchException`, `WorkspaceClientException`, `ProvisioningClientException`, `CredentialAcquisitionException` | `SmartSearchError` and its subclasses (`AuthenticationError`, `PermissionDeniedError`, `NotFoundError`, `ConflictError`, `RequestTimeoutError`, `TransportError`) |
| `IllegalArgumentException` before sending | `ValueError` before sending |
| try-with-resources | `with` |

## Learning path

Choose a part for your task, then work through that part in order. Every step is one script.
Project search and Workplace are separate paths; user registration adds per-user identity.

### Part 1: Getting started

| Step | Example | What you learn | Run |
|---|---|---|---|
| 1 | `connect_and_check_access` | What a service key is; connect and check what the key may do | `./run.sh connect_and_check_access` |

### Part 2: Project search

The search examples use the **Movies sample data set** (fields `title`, `overview`, `tagline`,
`genres`, `release_date`, `vote_average`, `original_language`, `runtime`, `status`). Change the
field names to run them on your own data. All need `SMARTSEARCH_PROJECT_ID`, a project your key
is assigned to. The [search guide](../java/SEARCH_GUIDE.md) (written for Java; the concepts and request fields are the same, see the name mapping below) explains the concepts.

| Step | Example | What you learn | Run |
|---|---|---|---|
| 2 | `first_search` | Build a request, send it, read the hits and the response envelope | `./run.sh first_search "star wars"` |
| 3 | `choose_searched_and_returned_fields` | Where the query is looked for versus what each hit returns | `./run.sh choose_searched_and_returned_fields galaxy` |
| 4 | `page_through_results` | Paging with `from_` and `size` (reranking off) | `./run.sh page_through_results love` |
| 5 | `sort_results` | Sort by a field instead of relevance (reranking off) | `./run.sh sort_results love` |
| 6 | `filter_by_exact_value` | Yes/no conditions on an exact value, including properties inside lists | `./run.sh filter_by_exact_value love` |
| 7 | `filter_by_any_of_several_values` | One field, any of several values (multi-select facets) | `./run.sh filter_by_any_of_several_values love` |
| 8 | `filter_by_numeric_range` | At least, at most, between | `./run.sh filter_by_numeric_range love` |
| 9 | `filter_by_date_range` | Date ranges | `./run.sh filter_by_date_range love` |
| 10 | `filter_where_field_exists` | Only documents that have a value in a field | `./run.sh filter_where_field_exists love` |
| 11 | `filter_by_full_text_match` | Words anywhere in a text field | `./run.sh filter_by_full_text_match love paris` |
| 12 | `filter_by_exact_phrase` | Words together and in order | `./run.sh filter_by_exact_phrase` |
| 13 | `exclude_results` | Remove documents that match a condition | `./run.sh exclude_results love` |
| 14 | `combine_filters_with_any_of` | AND, OR and NOT together | `./run.sh combine_filters_with_any_of space` |
| 15 | `boost_term_values` | Move preferred documents up without removing others | `./run.sh boost_term_values love` |
| 16 | `highlight_matches` | Show why a result matched | `./run.sh highlight_matches princess` |
| 17 | `wildcard_and_prefix_search` | `avat*` style queries | `./run.sh wildcard_and_prefix_search "avat*"` |
| 18 | `correct_spelling` | Find results despite typos | `./run.sh correct_spelling terminater` |
| 19 | `clean_up_user_input` | Trim spaces and remove special characters from typed text | `./run.sh clean_up_user_input` |
| 20 | `turn_off_query_expansion` | Stop related-word expansion for one request | `./run.sh turn_off_query_expansion car` |
| 21 | `facet_counts` | Value counts, distinct counts, drill-down | `./run.sh facet_counts war` |
| 22 | `keyword_vs_semantic_vs_hybrid` | The three search techniques, and checking which one ran | `./run.sh keyword_vs_semantic_vs_hybrid` |
| 23 | `tune_hybrid_search` | How keyword and semantic results are fused | `./run.sh tune_hybrid_search` |
| 24 | `limit_semantic_matches` | How many close-in-meaning documents to consider and keep | `./run.sh limit_semantic_matches` |
| 25 | `rerank_results` | AI reranking on, off, and the project default | `./run.sh rerank_results "wizard school"` |
| 26 | `tune_reranking` | How many candidates to rerank; a minimum score | `./run.sh tune_reranking "wizard school"` |
| 27 | `precision_levels` | Broad versus strict matching | `./run.sh precision_levels "dark knight rises"` |
| 28 | `run_several_searches_at_once` | Several searches in one round trip | `./run.sh run_several_searches_at_once` |
| 29 | `search_through_use_case` | Search with a saved configuration | `./run.sh search_through_use_case "star wars"` |
| 30 | `handle_errors_and_warnings` | Invalid requests, refusals, and adjusted answers | `./run.sh handle_errors_and_warnings` |

### Part 3: Workplace as your service

All need `SMARTSEARCH_WORKSPACE_ID`, a workspace your key is a member of; answers also need an
answering agent in it. Which call to use:

- **search**: a list of matching documents, no language model. Use it for a results page.
- **query**: one question, one answer written from the documents with its sources, or only the
  sources (retrieval only). Use it for a question box, or to feed your own model.
- **chat**: like query, in a conversation where follow-up questions build on earlier turns.

| Step | Example | What you learn | Run |
|---|---|---|---|
| 31 | `search_workplace_as_service` | Workplace concepts; search, query and chat; the documents that match | `./run.sh search_workplace_as_service "blood pressure"` |
| 32 | `filter_workplace_by_source_and_title` | Limit a search to one source; filter on a document field | `./run.sh filter_workplace_by_source_and_title "blood pressure" pressure` |
| 33 | `ask_a_question` | Query in answer mode: the request, and reading answer, sources, citations, run ID, status | `./run.sh ask_a_question "What is a normal blood pressure?"` |
| 34 | `retrieve_sources_only` | Query in retrieval-only mode: the evidence without an answer | `./run.sh retrieve_sources_only "What is a normal blood pressure?"` |
| 35 | `stream_an_answer` | Stream a query answer as it is written; events, errors, timeouts | `./run.sh stream_an_answer "What causes high blood pressure?"` |
| 36 | `answer_with_memory` | Memory modes: STANDARD versus AGENTIC, and when to use each | `./run.sh answer_with_memory "What is a normal blood pressure?"` |
| 37 | `query_with_filters` | Answer from one source and documents that pass a filter | `./run.sh query_with_filters "What is a normal blood pressure?" pressure` |
| 38 | `chat_with_follow_up_questions` | Conversations: reuse the session for follow-ups | `./run.sh chat_with_follow_up_questions "Who was President Kennedy?" "When was he born?"` |
| 39 | `stream_chat_answer` | Stream a chat answer | `./run.sh stream_chat_answer "What causes high blood pressure?"` |

### Part 4: Your users

These examples submit real registration jobs when run against a live environment. Use your
administrator's test integration and the demo users shown in the source. Submission returns a
job ID, not proof of success: wait for a terminal state and read each item's outcome. `PARTIAL`
means some items failed. Keep the same idempotency key only when resubmitting the same request;
change it when changing users or grants.

| Step | Example | What you learn | Needs | Run |
|---|---|---|---|---|
| 40 | `register_users` | Register users from your system, linked to your identity provider | Provisioning integration | `./run.sh register_users` |
| 41 | `register_users_with_workspace_access` | Register a user and give workspace and source access | As 40, allowed to grant the workspace and source | `./run.sh register_users_with_workspace_access` |
| 42 | `search_workplace_as_user` | Act as a user from the user's own access token, and search | "Act as users" with token exchange; `SMARTSEARCH_USER_ACCESS_TOKEN` | `./run.sh search_workplace_as_user` |
| 43 | `create_user_assertion` | Build and sign the user assertion your backend creates (JWT, RS256), and the public key set (JWKS) to publish; optionally sign in with it | The `cryptography` package; your signing key; for `--sign-in`, issuer and JWKS URL registered by an Owner | `./run.sh create_user_assertion sdk-example-user-1` |
| 44 | `sign_in_with_your_identity_provider` | Act as a user who signed in to your identity provider; search and ask a question as them | Registered identity provider; "Act as users" with JWT grant; `SMARTSEARCH_USER_ASSERTION` | `./run.sh sign_in_with_your_identity_provider` |

## Administrator setup and things worth knowing

The one-time administrator setup (service key, identity provider, "Act as users", provisioning
integration) and the practical notes (check what actually ran, reranking and ordering, counting
results, memory, short-lived user tokens, timeouts) are the same for Python. Read them in the
Java README: [Administrator setup](../java/README.md#administrator-setup-one-time-no-code) and
[Things worth knowing](../java/README.md#things-worth-knowing). In Python the read timeout is
`SmartSearchAi(read_timeout=...)` (seconds, default 60) and a user token's expiry is
`UserWorkplace.expires_at`.

## When the result surprises you

| What you see | What to check next |
|---|---|
| `ModuleNotFoundError: smartsearch_ai` | Install the supplied wheel with the same interpreter that runs the example. Check your activated virtual environment or `PYTHON` override. |
| A missing environment variable | Fill in that exact name in the root `.env`; a resource ID is not a credential. |
| Token acquisition fails | Check the auth URL, realm and service key with your administrator. Keep secrets and tokens out of bug reports. |
| HTTP 403 or `SCOPE_DENIED` | Check access to the specific resource and operation and whether the resource is active. Permission to register users does not imply permission to search. |
| A successful request returns no documents | Use a word from a document you can access; verify the resource ID and filters. Movie fields only apply to a project with that schema. |
| A registration timeout | Keep the job ID and check that same job again; timeout does not cancel server work. |
| A terminal `PARTIAL` or `FAILED` job | Inspect individual item states and error codes before treating users as registered. |

## Check changes locally

With the SDK preview and example dependencies installed, run from `examples/python`:

```bash
python -m unittest discover -s tests -v
```

The tests run all 44 examples with in-memory HTTP fixtures and synthetic credentials. They
also verify that failed search envelopes and unfinished provisioning jobs are reported as failures.

## Writing another example

Follow the [Google Python Style Guide](https://google.github.io/styleguide/pyguide.html).
Use clear module and function docstrings, explaining arguments, returned values and meaningful
exceptions where needed. Keep comments focused on decisions and API behavior. Describe one
problem, show a command, explain how to recognize the result, and suggest one small experiment.
Keep every example within the curated public integration API.
