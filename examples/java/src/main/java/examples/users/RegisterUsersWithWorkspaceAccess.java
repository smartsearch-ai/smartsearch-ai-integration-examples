package examples.users;

import co.smartsearchai.SmartSearchAi;
import co.smartsearchai.provisioning.ProvisioningClient;
import co.smartsearchai.provisioning.ProvisioningModels.EnsureItem;
import co.smartsearchai.provisioning.ProvisioningModels.ExternalIdentity;
import co.smartsearchai.provisioning.ProvisioningModels.Grants;
import co.smartsearchai.provisioning.ProvisioningModels.JobKind;
import co.smartsearchai.provisioning.ProvisioningModels.Membership;
import co.smartsearchai.provisioning.ProvisioningModels.Profile;
import co.smartsearchai.provisioning.ProvisioningModels.SourceGrant;
import co.smartsearchai.provisioning.ProvisioningModels.SubmitJob;
import com.fasterxml.jackson.databind.JsonNode;
import examples.ExampleRunner;
import examples.SmartSearchConnectionConfig;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Register a user and give them Workplace access in one job (ONBOARD_USERS).
 *
 * <p><b>Concepts.</b> A <i>workspace</i> in Workplace is a shared space that searches and answers
 * over a set of <i>sources</i>: connected document collections such as a file share, a wiki or a
 * ticket system. Access has two layers:
 * <ul>
 *   <li>a <i>membership</i> lets the user into the workspace;</li>
 *   <li>a <i>source grant</i> decides which documents of one source they can see, by group, role
 *       or security key (the same values your documents' access rules use).</li>
 * </ul>
 * A user only ever sees documents allowed by both.
 *
 * <p>Grants carry a <i>revision</i> number to prevent lost updates: you send the revision you
 * expect (0 = no grant yet), and the server refuses the change if someone else changed it first.
 *
 * <p><b>Preconditions.</b> Everything {@code RegisterUsers} needs, plus an integration that may
 * grant SMARTSEARCH_WORKSPACE_ID and SMARTSEARCH_SOURCE_ID. SMARTSEARCH_GRANT_GROUP is the group
 * granted on the source (default {@code everyone}).
 *
 * <p>Run: {@code ./run.sh RegisterUsersWithWorkspaceAccess}
 *
 * <p>Learning checkpoint: predict the change, run this step, then inspect per-user job states; inspect PARTIAL/FAILED items.
 * Change one value and compare. If refused, verify the stated prerequisite with your Owner.
 */
public final class RegisterUsersWithWorkspaceAccess {

    private static final String IDEMPOTENCY_KEY =
        "sdk-example-onboard-users-v1";

    /**
     * Runs this teaching step using the settings and prerequisites described above.
     *
     * @param args query words or positional inputs shown in the run command
     */
    public static void main(String[] args) {
        ExampleRunner.run(() -> {
            String workspaceId = SmartSearchConnectionConfig.workspaceId();
            String sourceId = SmartSearchConnectionConfig.require(
                "SMARTSEARCH_SOURCE_ID"
            );
            String group = SmartSearchConnectionConfig.optional(
                "SMARTSEARCH_GRANT_GROUP",
                "everyone"
            );

            // Source grant: on this source, the user sees documents open to `group`.
            // Grants(groups, roles, securityKeys); expected revision 0 = the user has no grant yet.
            SourceGrant grant = new SourceGrant(
                sourceId,
                0,
                new Grants(List.of(group), List.of(), List.of())
            );
            // Membership: the user joins the workspace, with the grant above.
            // Expected membership revision null = not sent.
            Membership membership = new Membership(
                workspaceId,
                null,
                List.of(grant)
            );

            EnsureItem user = new EnsureItem(
                "user-1",
                "sdk-example-user-1",
                new Profile(
                    "sdk-example-user-1@example.com",
                    "Ada",
                    "Example",
                    "Ada Example"
                ),
                "GUEST",
                Set.of(),
                List.of(membership), // the access to give
                new ExternalIdentity("sdk-example-user-1", "sdk-example-user-1")
            );

            try (SmartSearchAi ss = SmartSearchConnectionConfig.connect()) {
                ProvisioningClient provisioning = ss.users();
                SubmitJob job = new SubmitJob(
                    UserProvisioningHelper.tenantScope(),
                    SmartSearchConnectionConfig.integrationId(),
                    JobKind.ONBOARD_USERS,
                    List.of(user)
                );

                // POST {adminUrl}/search-admin/api/provisioning/v1/jobs  (Idempotency-Key header)
                // Errors: as RegisterUsers. Each user's outcome is reported per item (printItems below).
                String jobId = provisioning
                    .submitJob(job, IDEMPOTENCY_KEY)
                    .getBody()
                    .path("job_id")
                    .asText();
                System.out.println("Submitted onboarding job " + jobId);

                JsonNode done = UserProvisioningHelper.waitForJob(
                    provisioning,
                    jobId,
                    Duration.ofSeconds(60)
                );
                System.out.println(
                    "Job finished: state=" + done.path("state").asText()
                );
                UserProvisioningHelper.printItems(provisioning, jobId);
            }
        });
    }
}
