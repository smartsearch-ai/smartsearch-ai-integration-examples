# SmartSearch AI Integration Examples

Runnable examples that show how to integrate with **SmartSearch AI** from Java, Python and TypeScript. They
are written for developers who have never used SmartSearch AI: follow the learning path step by
step, and read each example's comments alongside its output.

> **Status:** preview. Java, Python and TypeScript examples follow the same learning path.
> Use the supplied public SDK preview built from SDK 4.5.0.7. Package publication is pending;
> follow the local artifact setup instructions below. TypeScript uses Node native HTTP calls
> and has no unpublished SDK dependency.

## What you can do

- **Search a SmartSearch AI project**: keyword, filtered, sorted, faceted, semantic and hybrid AI
  search, with reranking and precision control.
- **Search Workplace**: search, answers with sources, and chat (with streaming) over your
  company documents, as your service or as each of your users.
- **Register your users and give them access** (Principal Exchange): create users from your own
  system, linked to your identity provider, with no passwords to manage.

## Prerequisites

| | Java | Python | TypeScript |
|---|---|---|---|
| Runtime | Java 21+ | Python 3.10+ | Node.js 22.9+ |
| SDK | `co.smartsearchai:smartsearch-ai` ([setup](examples/java/README.md#setup)) | `smartsearch-ai` ([setup](examples/python/README.md#setup)) | Native HTTP teaching helper ([setup](examples/typescript/README.md#set-up-once)) |

## What your administrator sets up

A SmartSearch AI Owner configures the parts your application needs in the Admin UI.
Project search needs a service key assigned to its project. General Workplace calls need
workspace membership. Registration and delegation setup are optional until you use those steps:

1. **Identity provider:** under **⋮ → Identity providers**, register your issuer and public
   JWKS URL for password-free users and signed assertions.
2. **Service key:** create a key in your tenant, assign its projects/workspaces and required
   permissions. Keep its secret on your backend.
3. **Register users:** edit the saved service account and enable **Register users**. Choose
   **No password: linked to their account in** and your registered identity provider for these
   examples. Allow user registration and, for onboarding, the workspace/source grants. Save
   and copy the **Integration ID**. Users, the key and allowed workspaces share one tenant.
   The alternative email/password invitation mode needs configured outgoing email; these
   examples demonstrate the federated mode.
4. **Act as users:** enable this separately for per-user Workplace calls. Select trusted
   providers and allowed operations/workspaces/sources; enable JWT grant for signed assertions
   and token exchange for user access tokens. Registration alone does not enable delegation.

Workspace load keys are for ingestion. Use a service key or the user's delegated identity for
search, answers and chat. Managed workspace data must be searched through Workplace so access
rules apply; do not substitute direct Core project searches.

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

The [Java README](examples/java/README.md#learning-path),
[Python README](examples/python/README.md#learning-path) and
[TypeScript README](examples/typescript/README.md#learning-path) list every step with its run command.
In short:

1. **Choose your task**: begin with project search or Workplace. If your key has provisioning
   permission, `ConnectAndCheckAccess` checks its Admin provisioning capabilities.
2. **Project search**: from a first search to filters, sorting, facets, and semantic, hybrid and
   reranked search, one capability per example. [SEARCH_GUIDE.md](examples/java/SEARCH_GUIDE.md)
   explains the concepts.
3. **Workplace as your service**: search (a results list), query (one question, one answer with
   its sources, or the sources only; streamed or not; with or without memory), and chat (a
   conversation with follow-up questions).
4. **Your users**: register users, give them access, create the signed assertion that proves
   which user your backend acts for, and search as them.

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
