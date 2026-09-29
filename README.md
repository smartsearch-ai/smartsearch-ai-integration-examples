# SmartSearch AI Integration Examples

Runnable examples that show how to integrate with **SmartSearch AI** from Java and Python.

> **Status:** under construction. Examples are added as each one is verified end to end.

## What you can do

- **Register users and give them access** (Principal Exchange). Create users from your own system, link them to your identity provider, and give them workspace access with no passwords to manage.
- **Search Workplace as your users.** Search, query and chat (with streaming) across the workspaces each user can see.
- **Search a SmartSearch AI project.** Keyword, filtered, faceted, semantic and hybrid AI search.

## Prerequisites

| | Java | Python |
|---|---|---|
| Runtime | Java 21+ | Python 3.10+ |
| SDK | `co.smartsearchai:smartsearch-ai` (Maven Central) | `pip install smartsearch-ai` |

## The keys you need

Your SmartSearch AI administrator gives you:

| Item | Used for |
|---|---|
| Service key (client ID `svc-…` and secret) | Registering users, and searching as your service |
| Auth, API and admin base URLs, and realm | Where the SDK connects |
| Integration ID | Which provisioning integration your service may use |
| Workspace and project IDs | What to search |

Keep secrets in environment variables (see `.env.example`). **Never commit them, and never
put an administrator's personal token in your application.**

## Two ways to create users (Principal Exchange)

1. **Admin UI.** An administrator creates the user, who receives a temporary password and
   sets their own password at first sign-in. No code is needed.
2. **API integration** (these examples). Your backend creates users with its service key.
   Users are linked to your identity provider, so there are no SmartSearch AI passwords.

## Examples

| # | Example | Java | Python |
|---|---|---|---|
| 01 | Connect with a service key | planned | planned |
| 02 | Register users | planned | planned |
| 03 | Register users with workspace access | planned | planned |
| 04 | Search Workplace as a user | planned | planned |
| 05 | Sign in through your identity provider | planned | planned |
| 06 | Streaming chat | planned | planned |
| 07 | Search as your service | planned | planned |
| P1–P14 | Project search: filters, facets, boosts, semantic and hybrid AI search, reranking and more | planned | planned |

## License

Use of this repository is governed by the [SmartSearch AI SDK and Examples License](LICENSE).
Redistribution is not permitted.
