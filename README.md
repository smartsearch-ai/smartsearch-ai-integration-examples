# SmartSearch AI Integration Examples

Runnable examples that show how to integrate with **SmartSearch AI** from Java and Python. They
are written for developers who have never used SmartSearch AI: follow the learning path step by
step, and read each example's comments alongside its output.

> **Status:** preview. Java examples are available; Python examples will follow the Python SDK.

## What you can do

- **Search a SmartSearch AI project**: keyword, filtered, sorted, faceted, semantic and hybrid AI
  search, with reranking and precision control.
- **Search Workplace**: search, answers with sources, and chat (with streaming) over your
  company documents, as your service or as each of your users.
- **Register your users and give them access** (Principal Exchange): create users from your own
  system, linked to your identity provider, with no passwords to manage.

## Prerequisites

| | Java | Python |
|---|---|---|
| Runtime | Java 21+ | Python 3.10+ |
| SDK | `co.smartsearchai:smartsearch-ai` ([setup](examples/java/README.md#setup)) | `smartsearch-ai` (coming soon) |

## What your administrator gives you

| Item | Example value | Used for |
|---|---|---|
| API URL | `https://api.your-company.example.com` | Project search and Workplace |
| Admin URL | `https://admin.your-company.example.com` | Registering users |
| Auth URL and realm | `https://auth.your-company.example.com`, `your-realm` | Getting access tokens (the SDK does this) |
| Service key (ID `svc-…` and secret) | `svc-your-key-id` | Your application's identity |
| Project, workspace and source IDs | `your-project-id` | What to search |
| Integration and tenant IDs | `your-integration-id` | Registering users |

Keep secrets in environment variables (see [`.env.example`](.env.example), which explains every
setting). **Never commit them, and never put an administrator's personal login or token in your
application.**

## Learning path

The [Java README](examples/java/README.md#learning-path) lists every step with its run command.
In short:

1. **Getting started**: connect with your service key (`ConnectAndCheckAccess`).
2. **Project search**: from a first search to filters, sorting, facets, and semantic, hybrid and
   reranked search, one capability per example. [SEARCH_GUIDE.md](examples/java/SEARCH_GUIDE.md)
   explains the concepts.
3. **Workplace as your service**: search (a results list), query (one question, one answer with
   its sources, or the sources only; streamed or not; with or without memory), and chat (a
   conversation with follow-up questions).
4. **Your users**: register users, give them access, and search as them.

## Two ways to create users

1. **Admin UI.** An administrator creates the user, who receives a temporary password and sets
   their own password at first sign-in. No code is needed (see
   [Administrator setup](examples/java/README.md#administrator-setup-one-time-no-code)).
2. **API integration** (`RegisterUsers`, `RegisterUsersWithWorkspaceAccess`). Your backend
   registers users with its service key. Users are linked to your identity provider, so there are
   no SmartSearch AI passwords.

## License

Use of this repository is governed by the [SmartSearch AI SDK and Examples License](LICENSE).
Redistribution is not permitted.
