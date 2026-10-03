"""Your users sign in to YOUR identity provider; your backend then searches and answers as them
(JWT Authorization Grant, RFC 7523). No SmartSearch AI password is involved.

How it fits together.

1. Once: register the users (``register_users``), linking each one to their account in your
   identity provider, and give them access (``register_users_with_workspace_access``).
2. Each sign-in: your identity provider, or your backend, issues a signed token (an *assertion*)
   that says who the user is. ``create_user_assertion`` shows how to build and sign one, and the
   public key set to publish.
3. Your backend passes that assertion to ``as_user(...)``. The identity server checks it against
   your registered identity provider and returns a token that acts as the linked SmartSearch AI
   user on behalf of your service.

Preconditions.

- Your identity provider is registered with SmartSearch AI, and "Act as users" is enabled for
  your service key with the JWT authorization grant (see "Administrator setup").
- The user is registered and has workspace access.
- SMARTSEARCH_USER_ASSERTION holds a FRESH assertion for the user. Assertions are single-use and
  short-lived (at most 5 minutes): get a new one for every ``as_user`` call.

Run: ./run.sh sign_in_with_your_identity_provider "What is our travel policy?"
"""

import smartsearch_ai

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    query = _common.query_text(args, "What is our travel policy?")
    workspace = _common.workspace_id()
    # as_user: POST {auth_url}/realms/{realm}/protocol/openid-connect/token
    # grant_type = urn:ietf:params:oauth:grant-type:jwt-bearer, assertion = the
    # user's token.
    # Raises AuthenticationError (code TOKEN_ACQUISITION_FAILED) when the
    # identity server
    #   refuses, for example for an expired or already-used assertion.
    with _common.connect() as ss, ss.as_user(
        _common.require("SMARTSEARCH_USER_ASSERTION")
    ) as user:
        print(f"Signed in as the user until {user.expires_at.isoformat()}")
        # Search: only the documents this user is allowed to see.
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/search   (as the
        # user)
        _common.print_documents(
            user.search(
                workspace, smartsearch_ai.WorkspaceQueryRequest(query=query)
            ).body
        )
        # Answer: written only from those documents.
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/query   (as the
        # user)
        answer = user.query(
            workspace,
            smartsearch_ai.WorkspaceQueryRequest(
                query=query,
                mode=smartsearch_ai.Mode.ANSWER,
                memory_mode=smartsearch_ai.MemoryMode.STANDARD,  # AGENTIC would use this user's own long-term memory
            ),
        ).body
        _common.print_answer(answer)


if __name__ == "__main__":
    _common.run(main)
