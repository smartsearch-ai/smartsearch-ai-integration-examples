"""Search Workplace as one of your users, starting from a token the user already has
(OAuth 2.0 Token Exchange, RFC 8693).

Why act as the user? Each user should see only the documents they are allowed to see. Acting as
the user makes SmartSearch AI apply that user's workspace memberships and document permissions,
and gives the user their own memory. The exchanged token may only search, query and chat.

How. Your backend sends the user's SmartSearch AI access token together with your service key to
the identity server, and gets back a token that acts as the user on behalf of your service. Use
this when your users already sign in to SmartSearch AI (for example through single sign-on). If
they sign in to your own identity provider instead, see ``sign_in_with_your_identity_provider``.

Preconditions.

- "Act as users" is enabled for your service key with token exchange allowed (see
  "Administrator setup" in the README).
- SMARTSEARCH_USER_ACCESS_TOKEN holds the user's current access token. It is short-lived: set it
  just before running, never store it in a file.

Without token exchange enabled for the key the identity server refuses, and the example stops
with "Could not obtain a token".

Run: ./run.sh search_workplace_as_user "What is our travel policy?"
"""

from smartsearch_ai import WorkspaceQueryRequest

from examples._common import connect, print_documents, query_text, require, run, workspace_id


def main(args: list[str]) -> None:
    query = query_text(args, "What is our travel policy?")
    # as_user_from_token: POST {auth_url}/realms/{realm}/protocol/openid-connect/token
    #   grant_type = urn:ietf:params:oauth:grant-type:token-exchange, subject_token = the user's token.
    #   Raises AuthenticationError (code TOKEN_ACQUISITION_FAILED) when refused (grant not
    #   enabled, token expired).
    # The UserWorkplace holds the exchanged token; close it when done (with).
    with connect() as ss, ss.as_user_from_token(require("SMARTSEARCH_USER_ACCESS_TOKEN")) as user:
        # The token is not renewed: get a new one before this time.
        print(f"Acting as the user until {user.expires_at.isoformat()}")
        # Same call as the service search, now limited to what this user may see.
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/search   (as the user)
        print_documents(user.search(workspace_id(), WorkspaceQueryRequest(query=query)).body)


if __name__ == "__main__":
    run(main)
