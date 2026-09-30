package examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningClient;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.Scope;
import com.weblinktechs.smartsearch.client4j.provisioning.ProvisioningModels.ScopeKind;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/** Helpers shared by the provisioning examples (02 and 03). */
public final class Provisioning {

    private static final Set<String> TERMINAL =
            Set.of("SUCCEEDED", "PARTIAL", "FAILED", "RECONCILIATION_REQUIRED", "CANCELLED");

    private Provisioning() {
    }

    /** The tenant your integration provisions into (SMARTSEARCH_TENANT_ID). */
    public static Scope tenantScope() {
        return new Scope(ScopeKind.TENANT, Config.require("SMARTSEARCH_TENANT_ID"));
    }

    /** Polls a job until it reaches a terminal state (or the timeout passes) and returns the last view. */
    public static JsonNode waitForJob(ProvisioningClient users, String jobId, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        JsonNode job = users.getJob(jobId).getBody();
        while (!TERMINAL.contains(job.path("state").asText()) && Instant.now().isBefore(deadline)) {
            Thread.sleep(1_000);
            job = users.getJob(jobId).getBody();
        }
        return job;
    }

    /** Prints one line per job item: item key, state and the principal it resolved to. */
    public static void printItems(ProvisioningClient users, String jobId) {
        JsonNode page = users.listJobItems(jobId, null, 100, null).getBody();
        for (JsonNode item : page.path("values")) {
            System.out.println("  item " + item.path("item_key").asText() + ": " + item.path("state").asText()
                    + (item.hasNonNull("principal_id") ? " principal=" + item.path("principal_id").asText() : "")
                    + (item.hasNonNull("error_code") ? " error=" + item.path("error_code").asText() : ""));
        }
    }
}
