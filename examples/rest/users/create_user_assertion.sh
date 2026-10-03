# Step 43: create_user_assertion
# HTTP: Local RS256 signing (no HTTP request unless --sign-in).
# HTTP (optional): POST /realms/{realm}/protocol/openid-connect/token (form)
# HTTP (optional): POST /workspace/v1/workspaces/{workspaceId}/search
# Problem: Your backend needs to prove which signed-in external user it acts for.
# Needs: Python 3.10+, OpenSSL, auth URL and realm; no API/Admin/integration settings for offline signing.
# Optional: your RSA PKCS#8 PEM, issuer and key ID; default key is throw-away demonstration material.
# Expect: formatted header, claims and PUBLIC JWKS. The JWT/private key are never printed.
# Try: create an offline demo, then register your stable public keys before --sign-in.
# Optional sign-in needs a registered subject, Act as users/JWT grant and allowed workspace membership.
# Run: ./run.sh create_user_assertion sdk-example-user-1 [--sign-in "travel policy"]

# Local assertion construction has no HTTP request body; optional sign-in uses an OAuth form.
# JWT header JSON (public metadata):
# {
#   "alg": "RS256",
#   "typ": "JWT",
#   "kid": "your-key-id"
# }
# JWT claims JSON (representative, never a bearer token):
# {
#   "iss": "your-registered-issuer",
#   "sub": "sdk-example-user-1",
#   "aud": "https://auth.example.com/realms/your-realm",
#   "iat": 1700000000,
#   "exp": 1700000120,
#   "jti": "fresh-unique-identifier"
# }

python3 "$_REST_DIR/lib/assertion.py" "$_REST_TMP" "$@"
if [ "${2:-}" = --sign-in ]; then
  SMARTSEARCH_USER_ASSERTION=$(cat "$_REST_TMP/assertion.jwt")
  rest_as_user assertion
  q="${3:-travel policy}"
  rest_workspace search "$(jq -n --arg q "$q" '
{
  query: $q,
  mode: "retrieval_only",
  memory_mode: "standard"
}
')"
fi
