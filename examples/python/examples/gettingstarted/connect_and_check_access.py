"""Connect with your service key and check what it may do through its provisioning capabilities.

Concepts. A *service key* is the identity of your application (not of a person) in SmartSearch
AI: an ID starting with ``svc-`` plus a secret, issued by your administrator. Your backend keeps
it in an environment variable or secret store and never ships it to a browser or mobile app. The
SDK trades it for a short-lived access token (OAuth 2.0 client credentials) and renews the token
for you; your code never handles the token.

Every call in these examples authenticates as this key, except where an example acts as one of
your users (see the Workplace examples).

Preconditions. The connection settings from ``.env.example`` (see ``_common.py``). This example
asks Search Admin which user-provisioning features are enabled for the key, which checks authentication and the Admin capabilities call only; test project and
workspace access separately. It needs no project or workspace.

Run: ./run.sh connect_and_check_access
"""

import json

from examples import _common


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Unused; this step reads its settings from the environment.
    """
    # connect() only validates the settings; the first call below fetches a
    # token.
    with _common.connect() as ss:
        # repr() shows the URLs and realm, never the secret.
        print(f"Connected: {ss!r}")

        # GET {admin_url}/search-admin/api/provisioning/v1/capabilities
        # First the SDK gets a token: POST
        # {auth_url}/realms/{realm}/protocol/openid-connect/token.
        # Errors: AuthenticationError (code TOKEN_ACQUISITION_FAILED) when the
        # identity server
        # refuses the key (wrong ID or secret, wrong realm); a SmartSearchError
        # with the HTTP
        # status and the server's error code when Search Admin refuses the key.
        body = ss.users().capabilities().body

        # The answer lists the provisioning job kinds this key may submit and
        # its limits,
        # for example kinds = ["UPSERT_USERS", "ONBOARD_USERS", ...] and
        # max_items.
        capabilities = body.get("result", body)
        print("Provisioning capabilities:")
        for key, value in capabilities.items():
            print(
                f"  {key} = {_common.shorten(json.dumps(value, separators=(',', ':')), 200)}"
            )


if __name__ == "__main__":
    _common.run(main)
