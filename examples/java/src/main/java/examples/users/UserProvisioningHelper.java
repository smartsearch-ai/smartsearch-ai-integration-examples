package examples.users;

import co.smartsearchai.provisioning.ProvisioningClient;
import co.smartsearchai.provisioning.ProvisioningModels.Scope;
import co.smartsearchai.provisioning.ProvisioningModels.ScopeKind;
import com.fasterxml.jackson.databind.JsonNode;
import examples.SmartSearchConnectionConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Shared steps of the user-registration examples: the tenant to register into, waiting for a
 * provisioning job to finish, and printing what happened to each user.
 *
 * <p><i>Example plumbing, not part of the SDK.</i> Copy or replace it in your own code.
 */
final class UserProvisioningHelper {

    /** States after which a job no longer changes. */
    private static final Set<String> FINISHED =
            Set.of("SUCCEEDED", "PARTIAL", "FAILED", "RECONCILIATION_REQUIRED", "CANCELLED");

    private UserProvisioningHelper() {
    }

    /**
     * The tenant your integration registers users into. A <i>tenant</i> is your organisation's
     * account in SmartSearch AI; your administrator gives you its ID (SMARTSEARCH_TENANT_ID).
     */
    static Scope tenantScope() {
        return new Scope(ScopeKind.TENANT, SmartSearchConnectionConfig.require("SMARTSEARCH_TENANT_ID"));
    }

    /**
     * Polls a job once a second until it finishes or the timeout passes, and returns the last view.
     * GET {adminUrl}/search-admin/api/provisioning/v1/jobs/{jobId}
     */
    static JsonNode waitForJob(ProvisioningClient users, String jobId, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        JsonNode job = users.getJob(jobId).getBody();
        while (!FINISHED.contains(job.path("state").asText()) && Instant.now().isBefore(deadline)) {
            Thread.sleep(1_000);
            job = users.getJob(jobId).getBody();
        }
        return job;
    }

    /**
     * Prints one line per user in the job: your item key, its state, and the principal it became.
     * GET {adminUrl}/search-admin/api/provisioning/v1/jobs/{jobId}/items?limit=100
     */
    static void printItems(ProvisioningClient users, String jobId) {
        JsonNode page = users.listJobItems(jobId, null, 100, null).getBody();
        for (JsonNode item : page.path("values")) {
            System.out.println("  item " + item.path("item_key").asText() + ": " + item.path("state").asText()
                    + (item.hasNonNull("principal_id") ? " principal=" + item.path("principal_id").asText() : "")
                    + (item.hasNonNull("error_code") ? " error=" + item.path("error_code").asText() : ""));
        }
    }
}
