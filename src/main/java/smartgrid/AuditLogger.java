package smartgrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AuditLogger.java
 *
 * Records all energy routing decisions with timestamps for audit purposes.
 *
 * Requirement: FR-008 - System shall log all energy routing decisions with timestamps.
 *
 * MSCS3254 - Group 4
 */
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogger.class);

    /**
     * Immutable record of a single routing decision.
     */
    public static class AuditEntry {
        public final String    decision;
        public final double    demandKwh;
        public final double    solarOutputKwh;
        public final long      decisionTimeMs;
        public final String    timestamp;

        public AuditEntry(String decision, double demandKwh,
                          double solarOutputKwh, long decisionTimeMs) {
            this.decision        = decision;
            this.demandKwh       = demandKwh;
            this.solarOutputKwh  = solarOutputKwh;
            this.decisionTimeMs  = decisionTimeMs;
            this.timestamp       = Instant.now().toString(); // ISO 8601
        }

        @Override
        public String toString() {
            return String.format("[%s] Decision=%s | Demand=%.2fkWh | Solar=%.2fkWh | Time=%dms",
                    timestamp, decision, demandKwh, solarOutputKwh, decisionTimeMs);
        }
    }

    private final List<AuditEntry> log = new ArrayList<>();

    /**
     * Records a routing decision to the audit log.
     *
     * @param decision        The routing decision made (e.g., "SOLAR_ONLY")
     * @param demandKwh       The demand value at time of decision
     * @param solarOutputKwh  The solar output at time of decision
     * @param decisionTimeMs  How long the routing decision took in milliseconds
     */
    public void logRoutingDecision(String decision, double demandKwh,
                                   double solarOutputKwh, long decisionTimeMs) {
        AuditEntry entry = new AuditEntry(decision, demandKwh, solarOutputKwh, decisionTimeMs);
        log.add(entry);
        logger.info("AUDIT: {}", entry);
    }

    /**
     * Returns an unmodifiable view of all audit log entries.
     */
    public List<AuditEntry> getLog() {
        return Collections.unmodifiableList(log);
    }

    /**
     * Returns the total number of routing decisions logged.
     */
    public int getEntryCount() {
        return log.size();
    }

    /**
     * Clears the audit log. Use with caution – only for testing purposes.
     */
    public void clearLog() {
        log.clear();
    }
}
