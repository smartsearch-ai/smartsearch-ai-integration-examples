"""Register a user and give them Workplace access in one job (ONBOARD_USERS).

Concepts. A *workspace* in Workplace is a shared space that searches and answers over a set of
*sources*: connected document collections such as a file share, a wiki or a ticket system.
Access has two layers:

- a *membership* lets the user into the workspace;
- a *source grant* decides which documents of one source they can see, by group, role or
  security key (the same values your documents' access rules use).

A user only ever sees documents allowed by both.

Grants carry a *revision* number to prevent lost updates: you send the revision you expect
(0 = no grant yet), and the server refuses the change if someone else changed it first.

Preconditions. Everything ``register_users`` needs, plus an integration that may grant
SMARTSEARCH_WORKSPACE_ID and SMARTSEARCH_SOURCE_ID. SMARTSEARCH_GRANT_GROUP is the group granted
on the source (default ``everyone``).

Run: ./run.sh register_users_with_workspace_access
"""

import smartsearch_ai

from examples import _common

IDEMPOTENCY_KEY = "sdk-example-onboard-users-v1"


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Unused; this step reads its settings from the environment.
    """
    workspace = _common.workspace_id()
    source_id = _common.require("SMARTSEARCH_SOURCE_ID")
    group = _common.optional("SMARTSEARCH_GRANT_GROUP", "everyone")
    # Source grant: on this source, the user sees documents open to `group`.
    # Grants(groups, roles, security_keys); expected revision 0 = the user has
    # no grant yet.
    grant = smartsearch_ai.SourceGrant(
        source_id=source_id,
        expected_revision=0,
        grants=smartsearch_ai.Grants(groups=[group]),
    )
    # Membership: the user joins the workspace, with the grant above.
    # expected_revision None = not sent.
    membership = smartsearch_ai.Membership(
        workspace_id=workspace, source_grants=[grant]
    )
    user = smartsearch_ai.EnsureItem(
        item_key="user-1",
        external_user_id="sdk-example-user-1",
        profile=smartsearch_ai.Profile(
            email="sdk-example-user-1@example.com",
            first_name="Ada",
            last_name="Example",
            display_name="Ada Example",
        ),
        platform_role="GUEST",
        permissions=[],
        workspaces=[membership],  # the access to give
        external_identity=smartsearch_ai.ExternalIdentity(
            subject="sdk-example-user-1", username="sdk-example-user-1"
        ),
    )
    with _common.connect() as ss:
        provisioning = ss.users()
        job = smartsearch_ai.SubmitJob(
            scope=_common.tenant_scope(),
            integration_id=_common.integration_id(),
            kind=smartsearch_ai.JobKind.ONBOARD_USERS,
            items=[user],
        )
        # POST {admin_url}/search-admin/api/provisioning/v1/jobs  (Idempotency-
        # Key header)
        # Errors: as register_users. Each user's outcome is reported per item
        # (print_items below).
        job_id = provisioning.submit_job(job, IDEMPOTENCY_KEY).body["job_id"]
        print(f"Submitted onboarding job {job_id}")
        done = _common.wait_for_job(provisioning, job_id, 60)
        print(f"Job finished: state={done.get('state')}")
        _common.print_items(provisioning, job_id)


if __name__ == "__main__":
    _common.run(main)
