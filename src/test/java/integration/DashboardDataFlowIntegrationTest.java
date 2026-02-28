package integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smartgrid.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DashboardDataFlowIntegrationTest.java
 *
 * Integration tests verifying the full data pipeline:
 * Sensor Data → Demand Prediction → Routing → Audit Log → Dashboard readiness
 *
 * Simulates the end-to-end flow that feeds the Energy Monitor Dashboard (FR-004).
 *
 * Test Cases Covered: IT-002, IT-003, IT-004
 * Requirements: FR-002, FR-003, FR-004, FR-006, NFR-001
 *
 * MSCS3254 - Group 4
 * Contributor: Durvank Deorukhkar
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Integration: Full Data Pipeline → Dashboard")
class DashboardDataFlowIntegrationTest {

    @Mock
    private SolarSensorAPI mockSolarSensor;

    @Mock
    private WeatherAPI mockWeatherAPI;

    @Mock
    private AlertService mockAlertService;

    private BatteryManager  batteryManager;
    private DemandPredictor demandPredictor;
    private AuditLogger     auditLogger;
    private RouteOptimizer  routeOptimizer;

    @BeforeEach
    void setUp() {
        auditLogger     = new AuditLogger();
        batteryManager  = new BatteryManager(mockSolarSensor, mockAlertService);
        demandPredictor = new DemandPredictor(mockWeatherAPI);
        routeOptimizer  = new RouteOptimizer(batteryManager, mockSolarSensor, auditLogger);
        batteryManager.setCurrentChargeLevel(75.0);
    }

    @AfterEach
    void tearDown() {
        auditLogger.clearLog();
        reset(mockSolarSensor, mockWeatherAPI, mockAlertService);
    }

    // =========================================================================
    // IT-002: AI Prediction Engine → Routing Engine
    // =========================================================================

    @Test
    @DisplayName("IT-002: AI prediction feeds routing engine - solar+battery scenario")
    void testPredictionFeedsRoutingEngine() {
        // Arrange: weather indicates moderate demand, solar is insufficient
        when(mockWeatherAPI.isAvailable()).thenReturn(true);
        when(mockWeatherAPI.getCurrentTemperatureCelsius()).thenReturn(28.0);
        when(mockWeatherAPI.getCloudCoveragePercent()).thenReturn(60.0);
        when(mockWeatherAPI.getForecastedIrradiance()).thenReturn(400.0);
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(5.0);

        // Act: run full prediction → routing pipeline
        double predictedDemand = demandPredictor.predict();
        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(predictedDemand);

        // Assert: prediction is valid and routing produces a valid decision
        assertTrue(predictedDemand > 0, "Predicted demand should be positive");
        assertNotNull(decision, "Routing decision should not be null");

        // Since solar (5.0) < predicted demand and battery healthy (75%) → SOLAR_AND_BATTERY
        assertEquals(RouteOptimizer.RoutingDecision.SOLAR_AND_BATTERY, decision,
                "With high demand, low solar, and healthy battery → SOLAR_AND_BATTERY expected");
    }

    // =========================================================================
    // IT-003: Sensor Data → Dashboard Display readiness
    // =========================================================================

    @Test
    @DisplayName("IT-003: All dashboard data points are available after pipeline run")
    void testDashboardDataPointsAvailable() {
        // Arrange
        when(mockWeatherAPI.isAvailable()).thenReturn(true);
        when(mockWeatherAPI.getCurrentTemperatureCelsius()).thenReturn(22.0);
        when(mockWeatherAPI.getCloudCoveragePercent()).thenReturn(20.0);
        when(mockWeatherAPI.getForecastedIrradiance()).thenReturn(700.0);
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(9.5);

        // Act: run full pipeline
        double predictedDemand = demandPredictor.predict();
        RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(predictedDemand);
        double batteryLevel    = batteryManager.getCurrentChargeLevel();
        double solarOutput     = mockSolarSensor.getCurrentOutputKwh();

        // Assert: all three dashboard data points (FR-004) are populated
        assertTrue(predictedDemand > 0, "Dashboard: demand data is available");
        assertTrue(solarOutput >= 0,    "Dashboard: solar generation data is available");
        assertTrue(batteryLevel >= 0,   "Dashboard: battery charge level is available");
        assertNotNull(decision,         "Dashboard: routing decision is available");
        assertEquals(1, auditLogger.getEntryCount(), "Audit log should have one entry");
    }

    // =========================================================================
    // IT-004: OTA Firmware Update → System Availability
    // =========================================================================

    @Test
    @DisplayName("IT-004: System continues routing during simulated OTA update window")
    void testSystemAvailabilityDuringOTAUpdate() {
        // Simulate OTA update window: system continues routing
        // In a real test this would interact with FirmwareUpdateManager
        // For this integration test, we verify routing continues without interruption

        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(8.0);

        long startTime = System.currentTimeMillis();

        // Simulate 3 routing cycles during OTA window
        for (int i = 0; i < 3; i++) {
            RouteOptimizer.RoutingDecision decision = routeOptimizer.routeEnergy(10.0);
            assertNotNull(decision, "Routing should succeed during OTA update cycle " + i);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        // All 3 cycles should complete well within any OTA downtime window
        assertEquals(3, auditLogger.getEntryCount(),
                "All 3 routing cycles should be logged during OTA simulation");
        assertTrue(totalTime < 500,
                "All routing cycles should complete within acceptable time during OTA");
    }

    // =========================================================================
    // End-to-End: Low Battery Alert propagation through pipeline
    // =========================================================================

    @Test
    @DisplayName("End-to-end: Low battery triggers alert through full pipeline (TC-008)")
    void testLowBatteryAlertPropagation() {
        // Arrange: battery near threshold
        batteryManager.setCurrentChargeLevel(25.0);
        when(mockSolarSensor.isAvailable()).thenReturn(true);
        when(mockSolarSensor.getCurrentOutputKwh()).thenReturn(0.0);

        // Act: discharge enough to cross the threshold
        batteryManager.discharge(10.0);

        // Assert: alert was triggered (FR-005)
        verify(mockAlertService, atLeastOnce())
                .sendLowBatteryAlert(anyDouble());
        assertTrue(batteryManager.getCurrentChargeLevel() < BatteryManager.LOW_BATTERY_THRESHOLD,
                "Battery should be below threshold after discharge");
    }
}
