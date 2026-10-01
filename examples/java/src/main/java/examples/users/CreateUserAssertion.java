package examples.users;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.workplace.UserWorkplace;
import co.smartsearchai.workplace.WorkspaceQueryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;
import examples.workplace.WorkplaceResultPrinter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Create the signed user assertion that lets your backend act as one of your users.
 *
 * <p><b>Why this is needed.</b> A service key identifies your application, never a person, and it
 * can never turn itself into a user. To search as a user (so that user's access rules apply),
 * your backend must also prove WHICH user it is acting for. It does that with an
 * <i>assertion</i>: a short signed statement, "this is user {@code <subject>}", signed with a
 * private key only you hold. This is the one piece of the integration you build yourself.
 *
 * <p><b>The flow.</b>
 * <ol>
 *   <li><b>Register</b> each user with YOUR own user ID as their subject
 *       ({@code ExternalIdentity(subject, username)} in {@code RegisterUsers}).</li>
 *   <li><b>Sign</b>: when the user is signed in to your application, your backend creates an
 *       assertion for that subject, valid for a couple of minutes (this example).</li>
 *   <li><b>Exchange</b>: {@code ss.asUser(assertion)} sends the assertion together with your
 *       service key to the identity server and receives a token that acts as that SmartSearch AI
 *       user ({@code SignInWithYourIdentityProvider}).</li>
 *   <li><b>Trust</b>: SmartSearch AI accepts the signature because a SmartSearch AI Owner registered
 *       your issuer and the URL of your public keys once, in the Admin UI under
 *       <b>⋮ → Identity providers</b>, and allowed your service key to act as users
 *       (<b>Edit service account → Act as users</b>).</li>
 * </ol>
 *
 * <p><b>The assertion</b> is a JSON Web Token (JWT): {@code header.claims.signature}, each part
 * Base64url-encoded. Header: {@code alg} = RS256 (RSA signature with SHA-256), {@code typ} = JWT,
 * {@code kid} = the ID of the key that signed it. Claims, and what the server checks:
 * <pre>
 * iss  your issuer, e.g. https://login.your-company.example.com
 *      must equal the issuer registered under Identity providers
 * sub  your user ID, the subject you registered the user with
 *      must belong to a registered user linked to your identity provider
 * aud  the SmartSearch AI realm: {SMARTSEARCH_AUTH_BASE_URL}/realms/{SMARTSEARCH_REALM}
 *      the assertion must be addressed to SmartSearch AI
 * iat  issued at, in seconds since 1970
 * exp  expiry, at most 120 seconds after iat
 *      expired or long-lived assertions are refused
 * jti  a unique ID; each assertion is accepted only once
 *      a replayed assertion is refused
 * </pre>
 * The signature is checked against the public key with the same {@code kid}, read from your
 * public key set (a <i>JWKS</i>, JSON Web Key Set) at the URL the Owner registered.
 *
 * <p><b>What this example does.</b> It uses an RSA key from SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM
 * (PKCS#8 PEM), or generates a throw-away one. It builds and signs an assertion for a subject
 * (first argument), prints the decoded header and claims (never the token itself), and prints the
 * public JWKS you would host. Only the JDK is used: {@code java.security} signs, {@code Base64}
 * encodes. With {@code --sign-in} as the second argument it also passes the assertion to
 * {@code ss.asUser(...)} and searches as the user. That only succeeds once an Owner has
 * registered your issuer and JWKS URL; with an unregistered key the identity server refuses
 * ("Could not obtain a token"), which is the correct result.
 *
 * <p><b>Settings</b> (optional): SMARTSEARCH_ASSERTION_ISSUER (default
 * {@code https://login.your-company.example.com}), SMARTSEARCH_ASSERTION_KEY_ID (default
 * {@code example-key-1}), SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM. The JWKS URL you register is
 * where you publish the printed key set, e.g. {@code https://login.your-company.example.com/.well-known/jwks.json}.
 *
 * <p><b>Production notes.</b>
 * <ul>
 *   <li>Keep the private key in a key management service or hardware security module and sign
 *       there; never put it in source code, images or logs.</li>
 *   <li>Rotate keys by {@code kid}: publish the new public key in the JWKS first, start signing
 *       with it, and remove the old one once no assertion signed with it can still be valid.</li>
 *   <li>Keep server clocks synchronised (NTP): {@code iat} and {@code exp} are checked against the
 *       identity server's clock.</li>
 *   <li>Create one assertion per {@code asUser} call, only for a user who is signed in to your
 *       application, and never send it to a browser.</li>
 * </ul>
 *
 * <p>Run: {@code ./run.sh CreateUserAssertion sdk-example-user-1} or
 * {@code ./run.sh CreateUserAssertion sdk-example-user-1 --sign-in "travel policy"}
 */
public final class CreateUserAssertion {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Base64.Encoder BASE64URL = Base64.getUrlEncoder().withoutPadding();
    private static final long LIFETIME_SECONDS = 120;

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String subject = args.length > 0 ? args[0] : "sdk-example-user-1";
            boolean signIn = args.length > 1 && args[1].equals("--sign-in");
            String issuer = SmartSearchConnectionConfig.optional("SMARTSEARCH_ASSERTION_ISSUER", "https://login.your-company.example.com");
            String keyId = SmartSearchConnectionConfig.optional("SMARTSEARCH_ASSERTION_KEY_ID", "example-key-1");
            // The audience is the SmartSearch AI realm, built from the same settings the SDK uses.
            String audience = SmartSearchConnectionConfig.require("SMARTSEARCH_AUTH_BASE_URL").replaceAll("/+$", "")
                    + "/realms/" + SmartSearchConnectionConfig.require("SMARTSEARCH_REALM");

            KeyPair keys = loadOrGenerateKeys();

            // 1. Header: how it is signed, and with which key.
            ObjectNode header = JSON.createObjectNode().put("alg", "RS256").put("typ", "JWT").put("kid", keyId);

            // 2. Claims: who issued it, for which user, for whom, and for how long.
            long now = Instant.now().getEpochSecond();
            ObjectNode claims = JSON.createObjectNode()
                    .put("iss", issuer)                              // your registered issuer
                    .put("sub", subject)                             // your user ID, as registered
                    .put("aud", audience)                            // the SmartSearch AI realm
                    .put("iat", now)                                 // issued now
                    .put("exp", now + LIFETIME_SECONDS)              // valid for at most 120 s
                    .put("jti", UUID.randomUUID().toString());       // unique: usable once

            // 3. Sign: base64url(header) + "." + base64url(claims), signed with RS256.
            String signingInput = encode(header) + "." + encode(claims);
            Signature rs256 = Signature.getInstance("SHA256withRSA");
            rs256.initSign(keys.getPrivate());
            rs256.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            String assertion = signingInput + "." + BASE64URL.encodeToString(rs256.sign());

            // The assertion is a credential: print what is in it, never the token itself.
            System.out.println("header: " + header);
            System.out.println("claims: " + claims);
            System.out.println("assertion: " + assertion.length() + " characters, 3 parts (not printed)");

            // 4. The public key set to host at your JWKS URL (public data, safe to publish).
            RSAPublicKey publicKey = (RSAPublicKey) keys.getPublic();
            ObjectNode jwks = JSON.createObjectNode();
            jwks.putArray("keys").addObject()
                    .put("kty", "RSA").put("use", "sig").put("alg", "RS256").put("kid", keyId)
                    .put("n", BASE64URL.encodeToString(unsigned(publicKey.getModulus())))
                    .put("e", BASE64URL.encodeToString(unsigned(publicKey.getPublicExponent())));
            System.out.println("JWKS to publish (e.g. at https://login.your-company.example.com/.well-known/jwks.json):");
            System.out.println(JSON.writerWithDefaultPrettyPrinter().writeValueAsString(jwks));

            if (!signIn) return;

            // 5. Optional: act as the user with it (what SignInWithYourIdentityProvider does).
            // POST {authUrl}/realms/{realm}/protocol/openid-connect/token
            //   grant_type = urn:ietf:params:oauth:grant-type:jwt-bearer, assertion = the token above,
            //   authenticated with your service key. Refused (CredentialAcquisitionException) until an
            //   Owner has registered this issuer and JWKS URL, or when any check above fails.
            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect();
                 UserWorkplace user = ss.asUser(assertion)) {
                System.out.println("Signed in as " + subject + " until " + user.expiresAt());
                // POST {apiUrl}/workspace/v1/workspaces/{workspaceId}/search   (as the user)
                String text = args.length > 2 ? args[2] : "travel policy";
                WorkplaceResultPrinter.printDocuments(user.search(SmartSearchConnectionConfig.workspaceId(),
                        WorkspaceQueryRequest.builder(text).build()).getBody());
            }
        });
    }

    /** The private key from SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM (PKCS#8), or a new 2048-bit key. */
    private static KeyPair loadOrGenerateKeys() throws Exception {
        String pem = SmartSearchConnectionConfig.optional("SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM", null);
        if (pem == null) {
            System.out.println("No SMARTSEARCH_ASSERTION_PRIVATE_KEY_PEM: using a throw-away key (demo only).");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }
        String base64 = pem.replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "").replaceAll("\\s", "");
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = rsa.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
        // An RSA private key in PKCS#8 also carries the public modulus and exponent.
        RSAPrivateCrtKey crt = (RSAPrivateCrtKey) privateKey;
        return new KeyPair(rsa.generatePublic(new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent())), privateKey);
    }

    private static String encode(ObjectNode json) throws Exception {
        return BASE64URL.encodeToString(JSON.writeValueAsBytes(json));
    }

    /** Big-endian bytes without the sign byte Java adds, as JWK "n" and "e" require. */
    private static byte[] unsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            return trimmed;
        }
        return bytes;
    }
}
