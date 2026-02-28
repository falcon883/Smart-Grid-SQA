package integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartgrid.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SensorToRoutingIntegrationTest.java
 *
 * Integration tests verifying data flow from Solar Sensor → Route Optimizer.
 * Tests that sensor data is correctly consumed by the routing engine and produces
 * valid routing decisions within the 500ms SLA (FR-003).
 *
 * Test Cases Covered: IT-001, IT-002, TC-005
 * Requirements: FR-001, FR-002, FR-003
 *
 * MSCS3254 - Group 4
 * Contributor: Percival Tapera
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: Solar Sensor → Routing Engine")
class SensorToRoutingIntegrationTest {

    @Mock
    private SolarSensorAPI mockSolarSensor;

    @Mock
    private AlertService mockAlertService;

    private BatteryManager batteryManager;
    private AuditLogger    auditLogger;
    private RouteOptimizer routeOptimizer;

    @BeforeEach
    void setUp() {
        auditLogger    = new AuditLogger();
        batteryManager = new BatteryManager(mockSolarSensor, mockAlertService);
        batteryManager.setCurrentChargeLevel(80.0);
        routeOptimizer = new RouteOptimizer(batteryManager, mockSolarSensor, auditLogger);
    }

    @AfterEach
    void tearDown() {
        auditLogger.clearLog();
        reset(mockSolarSensor, mockAlertService);
    }

    // =========================================================================
    // IT-001: Solar Sensor → Edge Routing Engine
    // =========================================================================

    @Test
    @DisplayName("IT-001a: Solar surplus → EXPORT_TO_GRID decision")
    void testSolarSurplusLeadsToExport() {
        // Arrange: solar output significantly exceeds demand
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(15.0);

        // Act
        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(10.0);

        // Assert
        assertEquals(RouteOptimizer.RoutingDecision.EXPORT_TO_GRID, decision,
                "Should export to grid when solar output significantly exceeds demand");
    }

    @Test
    @DisplayName("IT-001b: Solar matches demand → SOLAR_ONLY decision")
    void testSolarMatchesDemandSolarOnly() {
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(10.0);

        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(10.0);

        assertEquals(RouteOptimizer.RoutingDecision.SOLAR_ONLY, decision);
    }

    @Test
    @DisplayName("IT-001c: Solar deficit with healthy battery → SOLAR_AND_BATTERY")
    void testSolarDeficitWithHealthyBattery() {
        // Arrange: solar insufficient, battery healthy
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(5.0);
        batteryManager.setCurrentChargeLevel(60.0);

        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(12.0);

        assertEquals(RouteOptimizer.RoutingDecision.SOLAR_AND_BATTERY, decision,
                "Should use solar + battery when solar is insufficient and battery is healthy");
    }

    @Test
    @DisplayName("IT-001d: Solar deficit with low battery → SOLAR_AND_GRID")
    void testSolarDeficitWithLowBattery() {
        // Arrange: solar insufficient, battery below threshold
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(3.0);
        batteryManager.setCurrentChargeLevel(15.0); // below 20% threshold

        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(12.0);

        assertEquals(RouteOptimizer.RoutingDecision.SOLAR_AND_GRID, decision,
                "Should use solar + grid to protect low battery");
    }

    // =========================================================================
    // IT-001e: Routing decision SLA verification (FR-003: < 500ms)
    // =========================================================================

    @Test
    @DisplayName("TC-005: Routing decision executes within 500ms SLA (FR-003)")
    void testRoutingDecisionWithin500ms() {
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(8.0);

        long startTime = System.currentTimeMillis();
        routeOptimizer.routeEnergy(10.0);
        long elapsed = System.currentTimeMillis() - startTime;

        assertTrue(elapsed < 500,
                "Routing decision must complete within 500ms. Actual: " + elapsed + "ms");
    }

    // =========================================================================
    // IT-001f: Sensor unavailability handling
    // =========================================================================

    @Test
    @DisplayName("IT-001f: Sensor unavailable with healthy battery → BATTERY_ONLY")
    void testSensorUnavailableHealthyBattery() {
        when(mockSolarSensor.isAvailable()).thenReturn(false);
        batteryManager.setCurrentChargeLevel(80.0);

        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(10.0);

        assertEquals(RouteOptimizer.RoutingDecision.BATTERY_ONLY, decision,
                "Should fall back to battery when sensor is unavailable");
    }

    @Test
    @DisplayName("IT-001g: Sensor unavailable with low battery → GRID_ONLY")
    void testSensorUnavailableLowBattery() {
        when(mockSolarSensor.isAvailable()).thenReturn(false);
        batteryManager.setCurrentChargeLevel(10.0);

        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(10.0);

        assertEquals(RouteOptimizer.RoutingDecision.GRID_ONLY, decision,
                "Should fall back to grid-only when sensor unavailable and battery is low");
    }

    // =========================================================================
    // IT-001h: Audit log populated after routing
    // =========================================================================

    @Test
    @DisplayName("Routing decision creates audit log entry (FR-008)")
    void testRoutingCreatesAuditEntry() {
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(8.0);

        routeOptimizer.routeEnergy(10.0);

        assertEquals(1, auditLogger.getEntryCount(),
                "One audit log entry should be created per routing call");
    }
}
