# SmartSearch AI Integration Examples

Runnable examples that show how to integrate with **SmartSearch AI** from Java and Python.

> **Status:** preview. Java examples are available; Python examples will follow the Python SDK.

## What you can do

- **Register users and give them access** (Principal Exchange). Create users from your own system, link them to your identity provider, and give them workspace access with no passwords to manage.
- **Search Workplace as your users.** Search, query and chat (with streaming) across the workspaces each user can see.
- **Search a SmartSearch AI project.** Keyword, filtered, faceted, semantic and hybrid AI search.

## Prerequisites

| | Java | Python |
|---|---|---|
| Runtime | Java 21+ | Python 3.10+ |
| SDK | `co.smartsearchai:smartsearch-ai` ([setup](examples/java/README.md#setup)) | `smartsearch-ai` (coming soon) |

## The keys you need

Your SmartSearch AI administrator gives you:

| Item | Used for |
|---|---|
| Service key (client ID `svc-…` and secret) | Registering users, and searching as your service |
| Auth, API and admin base URLs, and realm | Where the SDK connects |
| Integration ID and tenant ID | Which provisioning integration your service may use |
| Workspace, source and project IDs | What to search |

Keep secrets in environment variables (see `.env.example`). **Never commit them, and never
put an administrator's personal token in your application.**

## Two ways to create users (Principal Exchange)

1. **Admin UI.** An administrator creates the user, who receives a temporary password and
   sets their own password at first sign-in. No code is needed (see
   [Administrator setup](examples/java/README.md#00-administrator-setup-one-time-no-code)).
2. **API integration** (these examples). Your backend creates users with its service key.
   Users are linked to your identity provider, so there are no SmartSearch AI passwords.

## Examples

| # | Example | Java | Python |
|---|---|---|---|
| 00 | Administrator setup (one time, Admin UI, no code) | [README](examples/java/README.md#00-administrator-setup-one-time-no-code) | |
| 01 | Connect with a service key | `Ex01ServiceConnect` | coming soon |
| 02 | Register users | `Ex02RegisterUsers` | coming soon |
| 03 | Register users with workspace access | `Ex03OnboardWithAccess` | coming soon |
| 04 | Search Workplace as a user | `Ex04SearchAsUser` | coming soon |
| 05 | Sign in through your identity provider | `Ex05PartnerIdpLogin` | coming soon |
| 06 | Streaming chat | `Ex06StreamingChat` | coming soon |
| 07 | Search as your service | `Ex07ServiceSearch` | coming soon |
| W1–W3 | Workplace: source and field filters, retrieval then answer, multi-turn chat | `W1`–`W3` | coming soon |
| P1–P14 | Project search: filters, facets, boosts, semantic and hybrid AI search, reranking and more | `P01`–`P14` | coming soon |

See [examples/java](examples/java/README.md) for how to run them and what each one needs.

## License

Use of this repository is governed by the [SmartSearch AI SDK and Examples License](LICENSE).
Redistribution is not permitted.
