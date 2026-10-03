"""Build a one-use RS256 assertion with OpenSSL; print only claims and public JWKS.

The protected JWT file exists only for the shell's optional --sign-in step and is removed when
run.sh exits. No private key or assertion is printed or passed in a process argument.
"""

import base64
import json
import os
import pathlib
import secrets
import subprocess
import sys
import time
import uuid


def encode(value):
    """Encode JSON or bytes as unpadded base64url."""
    if not isinstance(value, bytes):
        value = json.dumps(value, separators=(",", ":")).encode()
    return base64.urlsafe_b64encode(value).decode().rstrip("=")


def protected_file(path, content):
    """Create a credential file exclusively, with owner-only access."""
    descriptor = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(descriptor, "wb") as output:
        output.write(content)


def openssl(*arguments, data=None):
    """Run OpenSSL with filenames as arguments and message bytes on stdin."""
    return subprocess.run(
        ["openssl", *arguments], input=data, capture_output=True, check=True
    ).stdout


def item(data, offset):
    """Read one DER tag-length-value item from the public RSA key."""
    tag = data[offset]
    length = data[offset + 1]
    start = offset + 2
    if length & 0x80:
        count = length & 0x7F
        length = int.from_bytes(data[start : start + count], "big")
        start += count
    end = start + length
    if end > len(data):
        raise ValueError("invalid public key")
    return tag, data[start:end], end


def main():
    """Sign the authenticated external subject and write a protected transient JWT."""
    directory = pathlib.Path(sys.argv[1])
    subject = sys.argv[2] if len(sys.argv) > 2 else "sdk-example-user-1"
    if not subject.strip():
        raise ValueError("missing subject")
    auth = os.environ.get("SMARTSEARCH_AUTH_BASE_URL", "").rstrip("/")
    realm = os.environ.get("SMARTSEARCH_REALM", "")
    if not auth or not realm:
        raise ValueError("auth URL and realm are required for the audience")
    key = directory / ("key-" + secrets.token_hex(8) + ".pem")
    configured = os.environ.get("SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM")
    if configured:
        protected_file(key, configured.encode())
    else:
        # OpenSSL writes inside run.sh's private directory. Set mode explicitly afterwards.
        openssl(
            "genpkey",
            "-algorithm",
            "RSA",
            "-pkeyopt",
            "rsa_keygen_bits:2048",
            "-out",
            str(key),
        )
        key.chmod(0o600)
        print(
            "Throw-away demonstration key; register your stable public JWKS before sign-in."
        )
    public = openssl("rsa", "-in", str(key), "-RSAPublicKey_out", "-outform", "DER")
    tag, sequence, _ = item(public, 0)
    if tag != 0x30:
        raise ValueError("invalid RSA key")
    tag_n, modulus, next_offset = item(sequence, 0)
    tag_e, exponent, _ = item(sequence, next_offset)
    modulus = modulus.lstrip(b"\0")
    exponent = exponent.lstrip(b"\0")
    if tag_n != 2 or tag_e != 2 or int.from_bytes(modulus, "big").bit_length() < 2048:
        raise ValueError("require RSA key of at least 2048 bits")
    kid = os.environ.get("SMARTSEARCH_ASSERTION_KEY_ID") or "example-key-1"
    now = int(time.time())
    header = {"alg": "RS256", "typ": "JWT", "kid": kid}
    claims = {
        "iss": os.environ.get("SMARTSEARCH_ASSERTION_ISSUER")
        or "https://login.your-company.example.com",
        "sub": subject,
        "aud": auth + "/realms/" + realm,
        "iat": now,
        "exp": now + 120,
        "jti": str(uuid.uuid4()),
    }
    unsigned = encode(header) + "." + encode(claims)
    signature = openssl("dgst", "-sha256", "-sign", str(key), data=unsigned.encode())
    assertion = unsigned + "." + encode(signature)
    protected_file(directory / "assertion.jwt", assertion.encode())
    key.unlink()
    print("header:", json.dumps(header, indent=2))
    print("claims:", json.dumps(claims, indent=2))
    print(
        "Public JWKS:",
        json.dumps(
            {
                "keys": [
                    {
                        "kty": "RSA",
                        "use": "sig",
                        "alg": "RS256",
                        "kid": kid,
                        "n": encode(modulus),
                        "e": encode(exponent),
                    }
                ]
            },
            indent=2,
        ),
    )


if __name__ == "__main__":
    try:
        main()
    except Exception:
        print(
            "ERROR Assertion creation failed; check OpenSSL, RSA PEM and auth/realm settings",
            file=sys.stderr,
        )
        sys.exit(1)
