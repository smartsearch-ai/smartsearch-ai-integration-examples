package examples.users;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.provisioning.ProvisioningClient;
import co.smartsearchai.provisioning.ProvisioningModels.EnsureItem;
import co.smartsearchai.provisioning.ProvisioningModels.ExternalIdentity;
import co.smartsearchai.provisioning.ProvisioningModels.JobKind;
import co.smartsearchai.provisioning.ProvisioningModels.Profile;
import co.smartsearchai.provisioning.ProvisioningModels.SubmitJob;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;
import examples.ExampleRunner;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Register users from your own system, with no SmartSearch AI password (Principal Exchange).
 *
 * <p><b>Concepts.</b> A <i>principal</i> is anyone SmartSearch AI can identify: a person or a
 * service key. <i>Principal Exchange</i> is how your backend creates principals for your users
 * and links each one to the account it already has in YOUR identity provider (your single
 * sign-on), so users never get a SmartSearch AI password. Your backend does this with its service
 * key through a <i>provisioning integration</i>: a policy, set up once by an administrator, that
 * says which tenant the key may register users into and which access it may grant.
 *
 * <p>Registration runs as an asynchronous <i>job</i>: you submit a list of users, the server
 * processes them, and you poll for the outcome of each one. Jobs are idempotent: submitting the
 * same Idempotency-Key again returns the same job instead of creating users twice, so this
 * example is safe to rerun.
 *
 * <p><b>Preconditions.</b> A provisioning integration that lists your service key and allows
 * registering users (SMARTSEARCH_INTEGRATION_ID, SMARTSEARCH_TENANT_ID), and your identity
 * provider registered with SmartSearch AI. See "Administrator setup" in the README. Without an
 * integration for your key the server answers HTTP 404 INTEGRATION_NOT_FOUND.
 *
 * <p>Run: {@code ./run.sh RegisterUsers}
 */
public final class RegisterUsers {

    // Stable key: rerunning the example returns the same job instead of registering again.
    private static final String IDEMPOTENCY_KEY = "sdk-example-upsert-users-v1";

    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            List<EnsureItem> users = List.of(user(1, "Ada", "Example"), user(2, "Grace", "Example"));

            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                ProvisioningClient provisioning = ss.users();

                // What to do: UPSERT_USERS = create each user, or update them if they already exist.
                // tenantScope() = where; integrationId = under which policy; users = who.
                SubmitJob job = new SubmitJob(UserProvisioningHelper.tenantScope(), SmartSearchConnectionConfig.integrationId(), JobKind.UPSERT_USERS, users);

                // 1. Submit. POST {adminUrl}/search-admin/api/provisioning/v1/jobs  (Idempotency-Key header)
                //    Returns at once (HTTP 202) with the job ID and its first state; the work continues
                //    on the server. Errors: ProvisioningClientException, for example 404
                //    INTEGRATION_NOT_FOUND (no integration for this key) or a validation error code.
                JsonNode submitted = provisioning.submitJob(job, IDEMPOTENCY_KEY).getBody();
                String jobId = submitted.path("job_id").asText();
                System.out.println("Submitted job " + jobId + " state=" + submitted.path("state").asText());

                // 2. Wait for the job, then show what happened to each user. A job can finish as
                //    PARTIAL: some users registered, others refused; each item says why.
                JsonNode done = UserProvisioningHelper.waitForJob(provisioning, jobId, Duration.ofSeconds(60));
                System.out.println("Job finished: state=" + done.path("state").asText());
                UserProvisioningHelper.printItems(provisioning, jobId);

                // 3. Map your user IDs to SmartSearch AI principal IDs (store them next to your users).
                //    GET {adminUrl}/search-admin/api/provisioning/v1/integrations/{integrationId}/principals
                //        ?scope_kind=TENANT&scope_id={tenantId}&limit=100
                //    For more than 100 users, pass the next-page cursor as the third argument (after).
                JsonNode principals = provisioning.listPrincipals(SmartSearchConnectionConfig.integrationId(), UserProvisioningHelper.tenantScope(), null, 100).getBody();
                System.out.println("Principals known to this integration:");
                for (JsonNode p : principals.path("values")) {
                    System.out.println("  " + p.path("external_user_id").asText() + " -> " + p.path("principal_id").asText());
                }
            }
        });
    }

    /** One user as your system knows them. The IDs here are demo values: use your own. */
    static EnsureItem user(int n, String firstName, String lastName) {
        String externalId = "sdk-example-user-" + n;
        return new EnsureItem(
                "user-" + n,                                    // item key: names this entry in the job's results
                externalId,                                     // your stable, never-reused user ID
                new Profile(externalId + "@example.com", firstName, lastName, firstName + " " + lastName),
                                                                // profile data only: the email never links to an existing account
                "GUEST",                                        // the platform role registered users get (the only one allowed)
                Set.of(),                                       // no extra permissions
                List.of(),                                      // no workspace access yet (see RegisterUsersWithWorkspaceAccess)
                new ExternalIdentity(externalId, externalId));  // the user's subject and username in YOUR identity provider
    }
}
