"""Register users from your own system, with no SmartSearch AI password (Principal Exchange).

Concepts. A *principal* is anyone SmartSearch AI can identify: a person or a service key.
*Principal Exchange* is how your backend creates principals for your users and links each one to
the account it already has in YOUR identity provider (your single sign-on), so users never get a
SmartSearch AI password. Your backend does this with its service key through a *provisioning
integration*: a policy, set up once by an administrator, that says which tenant the key may
register users into and which access it may grant.

Registration runs as an asynchronous *job*: you submit a list of users, the server processes
them, and you poll for the outcome of each one. Jobs are idempotent: submitting the same
Idempotency-Key again returns the same job instead of creating users twice, so this example is
safe to rerun with the unchanged request. Changing users or grants requires a new key.

Preconditions. A provisioning integration that lists your service key and allows registering
users (SMARTSEARCH_INTEGRATION_ID, SMARTSEARCH_TENANT_ID), and your identity provider registered
with SmartSearch AI. See "Administrator setup" in the README. Without an integration for your key
the server answers HTTP 404 INTEGRATION_NOT_FOUND.

Run: ./run.sh register_users
"""

import smartsearch_ai

from examples import _common

# Stable key: rerunning the example returns the same job instead of registering
# again.
IDEMPOTENCY_KEY = "sdk-example-upsert-users-v1"


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Unused; this step reads its settings from the environment.
    """
    users = [user(1, "Ada", "Example"), user(2, "Grace", "Example")]
    with _common.connect() as ss:
        provisioning = ss.users()
        # What to do: UPSERT_USERS = create each user, or update them if they
        # already exist.
        # scope = where; integration_id = under which policy; items = who.
        job = smartsearch_ai.SubmitJob(
            scope=_common.tenant_scope(),
            integration_id=_common.integration_id(),
            kind=smartsearch_ai.JobKind.UPSERT_USERS,
            items=users,
        )

        # 1. Submit. POST {admin_url}/search-admin/api/provisioning/v1/jobs
        # (Idempotency-Key header)
        # Returns at once (HTTP 202) with the job ID and its first state; the
        # work continues on
        # the server. Errors: SmartSearchError with the server code, for example
        # 404
        # INTEGRATION_NOT_FOUND (no integration for this key) or a validation
        # error code.
        submitted = provisioning.submit_job(job, IDEMPOTENCY_KEY).body
        job_id = submitted["job_id"]
        print(f"Submitted job {job_id} state={submitted.get('state')}")

        # 2. Wait for the job, then show what happened to each user. A job can
        # finish as PARTIAL:
        #    some users registered, others refused; each item says why.
        done = _common.wait_for_job(provisioning, job_id, 60)
        print(f"Job finished: state={done.get('state')}")
        _common.print_items(provisioning, job_id)

        # 3. Map your user IDs to SmartSearch AI principal IDs (store them next
        # to your users).
        # GET {admin_url}/search-
        # admin/api/provisioning/v1/integrations/{integration_id}/principals
        #        ?scope_kind=TENANT&scope_id={tenant_id}&limit=100
        #    For more than 100 users, pass the next-page cursor as after=...
        principals = provisioning.list_principals(
            _common.integration_id(), _common.tenant_scope(), limit=100
        ).body
        print("Principals known to this integration:")
        for p in principals.get("values", []):
            print(f"  {p.get('external_user_id')} -> {p.get('principal_id')}")


def user(n: int, first_name: str, last_name: str) -> smartsearch_ai.EnsureItem:
    """One user as your system knows them. The IDs here are demo values: use your own."""
    external_id = f"sdk-example-user-{n}"
    return smartsearch_ai.EnsureItem(
        item_key=f"user-{n}",  # names this entry in the job's results
        external_user_id=external_id,  # your stable, never-reused user ID
        profile=smartsearch_ai.Profile(
            email=f"{external_id}@example.com",
            first_name=first_name,
            last_name=last_name,
            display_name=f"{first_name} {last_name}",
        ),
        # profile data only: the email never links to an existing account
        platform_role="GUEST",  # the platform role registered users get (the only one allowed)
        permissions=[],  # no extra permissions
        workspaces=[],  # no workspace access yet (see register_users_with_workspace_access)
        external_identity=smartsearch_ai.ExternalIdentity(
            subject=external_id, username=external_id
        ),
        # the user's subject and username in YOUR identity provider
    )


if __name__ == "__main__":
    _common.run(main)
