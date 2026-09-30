package examples;

import co.smartsearchai.SmartSearchAi;
import com.fasterxml.jackson.databind.JsonNode;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningClient;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.EnsureItem;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.ExternalIdentity;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.Grants;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.JobKind;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.Membership;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.Profile;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.SourceGrant;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.SubmitJob;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 03 - Register a user and give them workspace access in one job (ONBOARD_USERS).
 *
 * <p>Preconditions: everything from example 02, plus an integration policy that allows granting
 * SMARTSEARCH_WORKSPACE_ID and SMARTSEARCH_SOURCE_ID.
 *
 * <p>The membership adds the user to the workspace; the source grant decides which documents of
 * that source they can see (by group, role or security key). A user only ever sees documents
 * allowed by both.
 */
public final class Ex03OnboardWithAccess {

    private static final String IDEMPOTENCY_KEY = "sdk-example-onboard-users-v1";

    public static void main(String[] args) {
        Console.run(() -> {
            String workspaceId = Config.workspaceId();
            String sourceId = Config.require("SMARTSEARCH_SOURCE_ID");

            // Grant one group on one source (SMARTSEARCH_GRANT_GROUP). Revision 0 = no grant exists yet.
            String group = Config.optional("SMARTSEARCH_GRANT_GROUP", "everyone");
            SourceGrant grant = new SourceGrant(sourceId, 0, new Grants(List.of(group), List.of(), List.of()));
            Membership membership = new Membership(workspaceId, null, List.of(grant));

            EnsureItem user = new EnsureItem(
                    "user-1", "sdk-example-user-1",
                    new Profile("sdk-example-user-1@example.com", "Ada", "Example", "Ada Example"),
                    "GUEST", Set.of(), List.of(membership),
                    new ExternalIdentity("sdk-example-user-1", "sdk-example-user-1"));

            try (SmartSearchAi ss = Config.connect()) {
                ProvisioningClient provisioning = ss.users();
                SubmitJob job = new SubmitJob(Provisioning.tenantScope(), Config.integrationId(), JobKind.ONBOARD_USERS, List.of(user));

                String jobId = provisioning.submitJob(job, IDEMPOTENCY_KEY).getBody().path("job_id").asText();
                System.out.println("Submitted onboarding job " + jobId);

                JsonNode done = Provisioning.waitForJob(provisioning, jobId, Duration.ofSeconds(60));
                System.out.println("Job finished: state=" + done.path("state").asText());
                Provisioning.printItems(provisioning, jobId);
            }
        });
    }
}
