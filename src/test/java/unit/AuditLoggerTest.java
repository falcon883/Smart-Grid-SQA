package unit;

import org.junit.jupiter.api.*;
import smartgrid.AuditLogger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuditLoggerTest.java
 *
 * Unit tests for AuditLogger.
 * Verifies that routing decisions are logged with correct data and ISO 8601 timestamps.
 *
 * Test Cases Covered: UT-004
 * Requirement: FR-008 - Log all routing decisions with timestamps
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
@DisplayName("AuditLogger Unit Tests")
class AuditLoggerTest {

    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();
    }

    @Test
    @DisplayName("UT-004a: Routing decision is logged and retrievable")
    void testRoutingDecisionIsLogged() {
        // Act
        auditLogger.logRoutingDecision("SOLAR_ONLY", 8.0, 10.0, 45L);

        // Assert
        assertEquals(1, auditLogger.getEntryCount(),
                "Log should contain exactly one entry");
    }

    @Test
    @DisplayName("UT-004b: Log entry contains correct routing decision")
    void testLogEntryHasCorrectDecision() {
        auditLogger.logRoutingDecision("SOLAR_AND_BATTERY", 12.0, 8.0, 60L);

        AuditLogger.AuditEntry entry = auditLogger.getLog().get(0);
        assertEquals("SOLAR_AND_BATTERY", entry.decision);
    }

    @Test
    @DisplayName("UT-004c: Log entry contains correct demand and solar values")
    void testLogEntryHasCorrectValues() {
        auditLogger.logRoutingDecision("GRID_ONLY", 15.0, 0.0, 120L);

        AuditLogger.AuditEntry entry = auditLogger.getLog().get(0);
        assertEquals(15.0, entry.demandKwh, 0.001);
        assertEquals(0.0, entry.solarOutputKwh, 0.001);
        assertEquals(120L, entry.decisionTimeMs);
    }

    @Test
    @DisplayName("UT-004d: Log entry timestamp is in ISO 8601 format (FR-008)")
    void testLogEntryTimestampIsISO8601() {
        auditLogger.logRoutingDecision("EXPORT_TO_GRID", 5.0, 9.0, 30L);

        AuditLogger.AuditEntry entry = auditLogger.getLog().get(0);
        // ISO 8601 timestamps contain 'T' and 'Z'
        assertNotNull(entry.timestamp, "Timestamp should not be null");
        assertTrue(entry.timestamp.contains("T"),
                "Timestamp should be ISO 8601 format containing 'T': " + entry.timestamp);
        assertTrue(entry.timestamp.contains("Z"),
                "Timestamp should be ISO 8601 format ending with 'Z': " + entry.timestamp);
    }

    @Test
    @DisplayName("Multiple log entries are stored in order")
    void testMultipleEntriesStoredInOrder() {
        auditLogger.logRoutingDecision("SOLAR_ONLY", 8.0, 10.0, 30L);
        auditLogger.logRoutingDecision("SOLAR_AND_BATTERY", 12.0, 8.0, 45L);
        auditLogger.logRoutingDecision("GRID_ONLY", 20.0, 0.0, 55L);

        assertEquals(3, auditLogger.getEntryCount());
        assertEquals("SOLAR_ONLY", auditLogger.getLog().get(0).decision);
        assertEquals("GRID_ONLY", auditLogger.getLog().get(2).decision);
    }

    @Test
    @DisplayName("clearLog() resets entry count to zero")
    void testClearLogResetsCount() {
        auditLogger.logRoutingDecision("SOLAR_ONLY", 8.0, 10.0, 30L);
        auditLogger.clearLog();
        assertEquals(0, auditLogger.getEntryCount(),
                "Entry count should be 0 after clearLog()");
    }

    @Test
    @DisplayName("getLog() returns unmodifiable list")
    void testGetLogReturnsUnmodifiableList() {
        auditLogger.logRoutingDecision("SOLAR_ONLY", 8.0, 10.0, 30L);

        assertThrows(UnsupportedOperationException.class,
                () -> auditLogger.getLog().add(new AuditLogger.AuditEntry("TEST", 0, 0, 0)),
                "getLog() should return an unmodifiable list");
    }
}
