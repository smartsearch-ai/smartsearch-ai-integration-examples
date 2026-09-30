package examples;

import co.smartsearchai.SmartSearchAi;
import com.fasterxml.jackson.databind.JsonNode;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningClient;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.EnsureItem;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.ExternalIdentity;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.JobKind;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.Profile;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.SubmitJob;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 02 - Register users from your system (Principal Exchange, UPSERT_USERS).
 *
 * <p>Preconditions: an integration policy that lists your service key and allows creating users
 * (SMARTSEARCH_INTEGRATION_ID, SMARTSEARCH_TENANT_ID), and your identity provider registered with
 * SmartSearch AI. See "00 Administrator setup" in the README.
 *
 * <p>Each user is linked to your identity provider through {@link ExternalIdentity} (the user's
 * subject and username there), so SmartSearch AI never holds a password for them. The email is
 * profile data only: it never links to an existing account. Jobs are asynchronous and idempotent:
 * submitting the same Idempotency-Key again returns the same job, so this example is safe to rerun.
 */
public final class Ex02RegisterUsers {

    // Stable keys: rerunning the example updates the same two users instead of creating new ones.
    private static final String IDEMPOTENCY_KEY = "sdk-example-upsert-users-v1";

    public static void main(String[] args) {
        Console.run(() -> {
            List<EnsureItem> users = List.of(user(1, "Ada", "Example"), user(2, "Grace", "Example"));

            try (SmartSearchAi ss = Config.connect()) {
                ProvisioningClient provisioning = ss.users();
                SubmitJob job = new SubmitJob(Provisioning.tenantScope(), Config.integrationId(), JobKind.UPSERT_USERS, users);

                // 1. Submit the job (HTTP 202). The Idempotency-Key makes retries safe.
                JsonNode submitted = provisioning.submitJob(job, IDEMPOTENCY_KEY).getBody();
                String jobId = submitted.path("job_id").asText();
                System.out.println("Submitted job " + jobId + " state=" + submitted.path("state").asText());

                // 2. Wait for it to finish, then show what happened to each user.
                JsonNode done = Provisioning.waitForJob(provisioning, jobId, Duration.ofSeconds(60));
                System.out.println("Job finished: state=" + done.path("state").asText());
                Provisioning.printItems(provisioning, jobId);

                // 3. Map your external user IDs to SmartSearch AI principal IDs.
                JsonNode principals = provisioning.listPrincipals(Config.integrationId(), Provisioning.tenantScope(), null, 100).getBody();
                System.out.println("Principals known to this integration:");
                for (JsonNode p : principals.path("values")) {
                    System.out.println("  " + p.path("external_user_id").asText() + " -> " + p.path("principal_id").asText());
                }
            }
        });
    }

    /** One user as your system knows them. The external IDs here are demo values: use your own. */
    static EnsureItem user(int n, String firstName, String lastName) {
        String externalId = "sdk-example-user-" + n;
        return new EnsureItem(
                "user-" + n,                                          // item key, unique within the job
                externalId,                                           // your stable user ID
                new Profile(externalId + "@example.com", firstName, lastName, firstName + " " + lastName),
                "GUEST",                                              // platform role for provisioned users
                Set.of(),                                             // no extra permissions
                List.of(),                                            // no workspace access yet (see example 03)
                new ExternalIdentity(externalId, externalId));        // subject + username in YOUR identity provider
    }
}
