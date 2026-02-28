package unit;

import org.junit.jupiter.api.*;
import smartgrid.AlertServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AlertServiceTest.java
 *
 * Unit tests for AlertServiceImpl.
 * Tests that alert counts are tracked correctly and alerts are sent as expected.
 *
 * Test Cases Covered: UT-003
 * Requirement: FR-005 - Alert when battery drops below 20%
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
@DisplayName("AlertService Unit Tests")
class AlertServiceTest {

    private AlertServiceImpl alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl();
    }

    @Test
    @DisplayName("UT-003a: Low battery alert increments alert count")
    void testLowBatteryAlertIncrementsCount() {
        // Act
        alertService.sendLowBatteryAlert(15.0);

        // Assert
        assertEquals(1, alertService.getAlertCount(),
                "Alert count should be 1 after one low battery alert");
    }

    @Test
    @DisplayName("UT-003b: Multiple different alerts are all counted")
    void testMultipleAlertsAreAllCounted() {
        alertService.sendLowBatteryAlert(10.0);
        alertService.sendSensorFailureAlert("SENSOR-001");
        alertService.sendSystemAlert("Test system alert");

        assertEquals(3, alertService.getAlertCount(),
                "Alert count should be 3 after three different alerts");
    }

    @Test
    @DisplayName("UT-003c: Alert count starts at zero")
    void testInitialAlertCountIsZero() {
        assertEquals(0, alertService.getAlertCount(),
                "Alert count should start at 0");
    }

    @Test
    @DisplayName("UT-003d: Low battery alert with threshold boundary value (exactly 20%)")
    void testLowBatteryAlertAtExactThreshold() {
        // This should still trigger – at 20% exactly, battery is at risk
        assertDoesNotThrow(() -> alertService.sendLowBatteryAlert(20.0),
                "Alert at exact threshold should not throw");
        assertEquals(1, alertService.getAlertCount());
    }

    @Test
    @DisplayName("Sensor failure alert with valid sensor ID")
    void testSensorFailureAlert() {
        assertDoesNotThrow(() -> alertService.sendSensorFailureAlert("EDGE-NODE-42"),
                "Sensor failure alert should not throw");
        assertEquals(1, alertService.getAlertCount());
    }
}
