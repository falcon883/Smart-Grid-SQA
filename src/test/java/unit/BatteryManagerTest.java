package unit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartgrid.AlertService;
import smartgrid.BatteryManager;
import smartgrid.SolarSensorAPI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BatteryManagerTest.java
 *
 * Unit tests for BatteryManager using JUnit 5 and Mockito.
 * SolarSensorAPI and AlertService are mocked to isolate BatteryManager logic.
 *
 * Test Cases Covered: UT-001, TC-008
 * Requirement: FR-001, FR-005
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BatteryManager Unit Tests")
class BatteryManagerTest {

    @Mock
    private SolarSensorAPI mockSolarSensor;

    @Mock
    private AlertService mockAlertService;

    private BatteryManager batteryManager;

    @BeforeEach
    void setUp() {
        batteryManager = new BatteryManager(mockSolarSensor, mockAlertService);
        // Reset to full charge before each test
        batteryManager.setCurrentChargeLevel(100.0);
    }

    @AfterEach
    void tearDown() {
        // Ensure mocks are reset after each test to prevent test pollution
        reset(mockSolarSensor, mockAlertService);
    }

    // =========================================================================
    // calculateOptimalDischarge() Tests
    // =========================================================================

    @Test
    @DisplayName("UT-001a: Solar fully covers demand - no discharge needed")
    void testNoDischargeWhenSolarCoversDemand() {
        // Arrange: solar output (5.0 kWh) >= demand (4.0 kWh)
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(5.0);

        // Act
        double discharge = batteryManager.calculateOptimalDischarge(4.0);

        // Assert
        assertEquals(0.0, discharge, 0.001,
                "Expected no discharge when solar output covers demand");
        verify(mockAlertService, never()).sendLowBatteryAlert(anyDouble());
    }

    @Test
    @DisplayName("UT-001b: Solar partially covers demand - discharge shortfall")
    void testDischargeShortfallWhenSolarInsufficient() {
        // Arrange: solar = 3.0 kWh, demand = 7.0 kWh → shortfall = 4.0 kWh
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(3.0);

        // Act
        double discharge = batteryManager.calculateOptimalDischarge(7.0);

        // Assert
        assertEquals(4.0, discharge, 0.001,
                "Expected discharge to equal the shortfall (demand - solar)");
    }

    @Test
    @DisplayName("UT-001c: Discharge capped at MAX_DISCHARGE_RATE when shortfall is large")
    void testDischargeCappedAtMaxRate() {
        // Arrange: solar = 0, demand = 20 kWh → shortfall = 20, but max is 5 kWh
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(0.0);

        // Act
        double discharge = batteryManager.calculateOptimalDischarge(20.0);

        // Assert
        assertEquals(BatteryManager.MAX_DISCHARGE_RATE, discharge, 0.001,
                "Discharge should be capped at MAX_DISCHARGE_RATE");
    }

    @Test
    @DisplayName("UT-001d: Battery at LOW threshold - alert fired, no discharge")
    void testNoDischargeBelowLowBatteryThreshold() {
        // Arrange: battery at threshold, any demand should trigger alert and return 0
        batteryManager.setCurrentChargeLevel(BatteryManager.LOW_BATTERY_THRESHOLD);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(0.0);

        // Act
        double discharge = batteryManager.calculateOptimalDischarge(5.0);

        // Assert
        assertEquals(0.0, discharge, 0.001,
                "No discharge should occur when battery is at or below threshold");
        verify(mockAlertService, times(1))
                .sendLowBatteryAlert(BatteryManager.LOW_BATTERY_THRESHOLD);
    }

    @Test
    @DisplayName("UT-001e: Negative demand throws IllegalArgumentException")
    void testNegativeDemandThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> batteryManager.calculateOptimalDischarge(-1.0),
                "Negative demand should throw IllegalArgumentException");
    }

    // =========================================================================
    // Equivalence Partitioning: Demand Boundary Values
    // =========================================================================

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 0.5, 1.0, 4.99, 5.0})
    @DisplayName("TC-001: Valid demand values within MAX_DISCHARGE_RATE produce valid results")
    void testValidDemandValues(double demand) {
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(0.0);
        double result = batteryManager.calculateOptimalDischarge(demand);
        assertTrue(result >= 0 && result <= BatteryManager.MAX_DISCHARGE_RATE,
                "Result must be between 0 and MAX_DISCHARGE_RATE for demand=" + demand);
    }

    // =========================================================================
    // charge() and discharge() Tests
    // =========================================================================

    @Test
    @DisplayName("Charge increases battery level, capped at 100%")
    void testChargeCappedAt100() {
        batteryManager.setCurrentChargeLevel(95.0);
        batteryManager.charge(10.0);
        assertEquals(100.0, batteryManager.getCurrentChargeLevel(), 0.001,
                "Battery charge should not exceed 100%");
    }

    @Test
    @DisplayName("Discharge below threshold triggers low battery alert (FR-005 / TC-008)")
    void testDischargeTriggersAlertBelowThreshold() {
        // Arrange: charge at 25%, discharging 10% will drop below threshold
        batteryManager.setCurrentChargeLevel(25.0);

        // Act
        batteryManager.discharge(10.0);

        // Assert
        verify(mockAlertService, atLeastOnce()).sendLowBatteryAlert(anyDouble());
        assertTrue(batteryManager.getCurrentChargeLevel() < BatteryManager.LOW_BATTERY_THRESHOLD);
    }

    @Test
    @DisplayName("Discharge does not go below 0%")
    void testDischargeFloorAtZero() {
        batteryManager.setCurrentChargeLevel(5.0);
        batteryManager.discharge(100.0);
        assertEquals(0.0, batteryManager.getCurrentChargeLevel(), 0.001,
                "Battery level should floor at 0%");
    }
}
