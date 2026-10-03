"""Create the signed user assertion that lets your backend act as one of your users.

Why this is needed. A service key identifies your application, never a person, and it can never
turn itself into a user. To search as a user (so that user's access rules apply), your backend
must also prove WHICH user it is acting for. It does that with an *assertion*: a short signed
statement, "this is user <subject>", signed with a private key only you hold. This is the one
piece of the integration you build yourself.

The flow.

1. Register each user with YOUR own user ID as their subject
   (``ExternalIdentity(subject=..., username=...)`` in ``register_users``).
2. Sign: when the user is signed in to your application, your backend creates an assertion for
   that subject, valid for a couple of minutes (this example).
3. Exchange: ``ss.as_user(assertion)`` sends the assertion together with your service key to the
   identity server and receives a token that acts as that SmartSearch AI user
   (``sign_in_with_your_identity_provider``).
4. Trust: SmartSearch AI accepts the signature because a SmartSearch AI Owner registered your
   issuer and the URL of your public keys once, in the Admin UI under "⋮ → Identity providers",
   and allowed your service key to act as users ("Edit service account → Act as users").

The assertion is a JSON Web Token (JWT): ``header.claims.signature``, each part
Base64url-encoded. Header: ``alg`` = RS256 (RSA signature with SHA-256), ``typ`` = JWT, ``kid`` =
the ID of the key that signed it. Claims, and what the server checks::

    iss  your issuer, e.g. https://login.your-company.example.com
         must equal the issuer registered under Identity providers
    sub  your user ID, the subject you registered the user with
         must belong to a registered user linked to your identity provider
    aud  the SmartSearch AI realm: {SMARTSEARCH_AUTH_BASE_URL}/realms/{SMARTSEARCH_REALM}
         the assertion must be addressed to SmartSearch AI
    iat  issued at, in seconds since 1970
    exp  expiry, at most 120 seconds after iat
         expired or long-lived assertions are refused
    jti  a unique ID; each assertion is accepted only once
         a replayed assertion is refused

The signature is checked against the public key with the same ``kid``, read from your public key
set (a *JWKS*, JSON Web Key Set) at the URL the Owner registered.

What this example does. It uses an RSA key from SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM (PKCS#8
PEM), or generates a throw-away one. It builds and signs an assertion for a subject (first
argument), prints the decoded header and claims (never the token itself), and prints the public
JWKS you would host. Python's standard library has no RSA signing, so this one example also needs
the ``cryptography`` package (``pip install cryptography``); everything else is standard library.
With ``--sign-in`` as the second argument it also passes the assertion to ``ss.as_user(...)``
and searches as the user. That only succeeds once an Owner has registered your issuer and JWKS
URL; with an unregistered key the identity server refuses ("Could not obtain a token"), which is
the correct result.

Settings (optional): SMARTSEARCH_ASSERTION_ISSUER (default
``https://login.your-company.example.com``), SMARTSEARCH_ASSERTION_KEY_ID (default
``example-key-1``), SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM. The JWKS URL you register is where you
publish the printed key set, e.g. ``https://login.your-company.example.com/.well-known/jwks.json``.

Production notes.

- Keep the private key in a key management service or hardware security module and sign there;
  never put it in source code, images or logs.
- Rotate keys by ``kid``: publish the new public key in the JWKS first, start signing with it,
  and remove the old one once no assertion signed with it can still be valid.
- Keep server clocks synchronised (NTP): ``iat`` and ``exp`` are checked against the identity
  server's clock.
- Create one assertion per ``as_user`` call, only for a user who is signed in to your
  application, and never send it to a browser.

Run: ./run.sh create_user_assertion sdk-example-user-1
or   ./run.sh create_user_assertion sdk-example-user-1 --sign-in "travel policy"
"""

import base64
import json
import time
import uuid

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.asymmetric import rsa
import smartsearch_ai

from examples import _common

LIFETIME_SECONDS = 120


def main(args: list[str]) -> None:
    """Runs this step after its module-level prerequisites are configured.

    Args:
        args: Query words or positional inputs shown in the run command.
    """
    subject = args[0] if len(args) > 0 else "sdk-example-user-1"
    sign_in = len(args) > 1 and args[1] == "--sign-in"
    issuer = _common.optional(
        "SMARTSEARCH_ASSERTION_ISSUER", "https://login.your-company.example.com"
    )
    key_id = _common.optional("SMARTSEARCH_ASSERTION_KEY_ID", "example-key-1")
    # The audience is the SmartSearch AI realm, built from the same settings the
    # SDK uses.
    audience = (
        _common.require("SMARTSEARCH_AUTH_BASE_URL").rstrip("/")
        + "/realms/"
        + _common.require("SMARTSEARCH_REALM")
    )

    key = load_or_generate_key()

    # 1. Header: how it is signed, and with which key.
    header = {"alg": "RS256", "typ": "JWT", "kid": key_id}

    # 2. Claims: who issued it, for which user, for whom, and for how long.
    now = int(time.time())
    claims = {
        "iss": issuer,  # your registered issuer
        "sub": subject,  # your user ID, as registered
        "aud": audience,  # the SmartSearch AI realm
        "iat": now,  # issued now
        "exp": now + LIFETIME_SECONDS,  # valid for at most 120 s
        "jti": str(uuid.uuid4()),  # unique: usable once
    }

    # 3. Sign: base64url(header) + "." + base64url(claims), signed with RS256
    #    (RSA PKCS #1 v1.5 with SHA-256).
    signing_input = encode(header) + "." + encode(claims)
    signature = key.sign(
        signing_input.encode("ascii"), padding.PKCS1v15(), hashes.SHA256()
    )
    assertion = signing_input + "." + b64url(signature)

    # The assertion is a credential: print what is in it, never the token
    # itself.
    print("header: " + compact(header))
    print("claims: " + compact(claims))
    print(
        f"assertion: {len(assertion)} characters, {len(assertion.split('.'))} parts (not printed)"
    )

    # 4. The public key set to host at your JWKS URL (public data, safe to
    # publish).
    numbers = key.public_key().public_numbers()
    jwks = {
        "keys": [
            {
                "kty": "RSA",
                "use": "sig",
                "alg": "RS256",
                "kid": key_id,
                "n": b64url(
                    numbers.n.to_bytes((numbers.n.bit_length() + 7) // 8, "big")
                ),  # big-endian, no sign byte
                "e": b64url(
                    numbers.e.to_bytes((numbers.e.bit_length() + 7) // 8, "big")
                ),
            }
        ]
    }
    print(
        "JWKS to publish (e.g. at https://login.your-company.example.com/.well-known/jwks.json):"
    )
    print(json.dumps(jwks, indent=2))

    if not sign_in:
        return

    # 5. Optional: act as the user with it (what
    # sign_in_with_your_identity_provider does).
    # POST {auth_url}/realms/{realm}/protocol/openid-connect/token
    # grant_type = urn:ietf:params:oauth:grant-type:jwt-bearer, assertion = the
    # token above,
    # authenticated with your service key. Refused (AuthenticationError) until
    # an Owner has
    #   registered this issuer and JWKS URL, or when any check above fails.
    with _common.connect() as ss, ss.as_user(assertion) as user:
        print(f"Signed in as {subject} until {user.expires_at.isoformat()}")
        # POST {api_url}/workspace/v1/workspaces/{workspace_id}/search   (as the
        # user)
        text = args[2] if len(args) > 2 else "travel policy"
        _common.print_documents(
            user.search(
                _common.workspace_id(),
                smartsearch_ai.WorkspaceQueryRequest(query=text),
            ).body
        )


def load_or_generate_key() -> rsa.RSAPrivateKey:
    """The private key from SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM (PKCS#8), or a new 2048-bit key."""
    pem = _common.optional("SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM", None)
    if pem is None:
        print(
            "No SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM: using a throw-away key (demo only)."
        )
        return rsa.generate_private_key(public_exponent=65537, key_size=2048)
    key = serialization.load_pem_private_key(pem.encode("ascii"), password=None)
    if not isinstance(key, rsa.RSAPrivateKey):
        raise ValueError(
            "SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM must be an RSA private key"
        )
    return key


def compact(value: dict) -> str:
    return json.dumps(value, separators=(",", ":"))


def encode(value: dict) -> str:
    return b64url(compact(value).encode("utf-8"))


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


if __name__ == "__main__":
    _common.run(main)
