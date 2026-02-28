package smartgrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RouteOptimizer.java
 *
 * Core energy routing logic for the Smart-Grid Neighborhood Optimizer.
 * Determines how energy flows between solar panels, battery, and the grid
 * based on the AI demand prediction and current system state.
 *
 * Requirement: FR-003 - Routing decision must execute within 500ms.
 * Requirement: NFR-004 - Cyclomatic Complexity per function must not exceed 15.
 *
 * NOTE: During code review, the original routeEnergy() function was found to have
 * a Cyclomatic Complexity of 18. It has been refactored into smaller strategy methods
 * to comply with NFR-004.
 *
 * MSCS3254 - Group 4
 */
public class RouteOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(RouteOptimizer.class);

    public enum RoutingDecision {
        SOLAR_ONLY,          // Solar fully covers demand
        SOLAR_AND_BATTERY,   // Solar + battery discharge required
        SOLAR_AND_GRID,      // Solar insufficient, pull from grid (battery protected)
        BATTERY_ONLY,        // No solar available, use battery
        GRID_ONLY,           // Emergency: solar + battery unavailable
        EXPORT_TO_GRID       // Excess solar → export back to grid
    }

    private final BatteryManager  batteryManager;
    private final SolarSensorAPI  solarSensor;
    private final AuditLogger     auditLogger;

    public RouteOptimizer(BatteryManager batteryManager,
                          SolarSensorAPI solarSensor,
                          AuditLogger auditLogger) {
        this.batteryManager = batteryManager;
        this.solarSensor    = solarSensor;
        this.auditLogger    = auditLogger;
    }

    /**
     * Main routing entry point. Determines the optimal energy routing decision
     * given the current predicted demand.
     *
     * Refactored to delegate to strategy methods to reduce cyclomatic complexity.
     * Each strategy method has a complexity score of <= 5.
     *
     * @param predictedDemandKwh  Demand predicted by the AI engine
     * @return                    The routing decision taken
     */
    public RoutingDecision routeEnergy(double predictedDemandKwh) {
        long startTime = System.currentTimeMillis();

        double solarOutput    = solarSensor.getCurrentOutputKwh();
        double batteryCharge  = batteryManager.getCurrentChargeLevel();
        RoutingDecision decision;

        if (!solarSensor.isAvailable()) {
            decision = handleNoSolarScenario(batteryCharge);
        } else if (solarOutput >= predictedDemandKwh) {
            decision = handleSolarSurplusScenario(solarOutput, predictedDemandKwh);
        } else {
            decision = handleSolarDeficitScenario(batteryCharge);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        auditLogger.logRoutingDecision(decision.name(), predictedDemandKwh, solarOutput, elapsed);

        // Warn if routing decision exceeded 500ms SLA (FR-003)
        if (elapsed > 500) {
            logger.warn("Routing decision took {}ms, exceeding 500ms SLA (FR-003).", elapsed);
        }

        return decision;
    }

    /**
     * Handles scenario where solar sensor is unavailable.
     * Cyclomatic Complexity: 3
     */
    private RoutingDecision handleNoSolarScenario(double batteryCharge) {
        if (batteryCharge > BatteryManager.LOW_BATTERY_THRESHOLD) {
            return RoutingDecision.BATTERY_ONLY;
        }
        return RoutingDecision.GRID_ONLY;
    }

    /**
     * Handles scenario where solar output meets or exceeds demand.
     * Cyclomatic Complexity: 2
     */
    private RoutingDecision handleSolarSurplusScenario(double solarOutput, double demand) {
        if (solarOutput > demand * 1.2) {
            // Significant surplus - export excess to grid
            return RoutingDecision.EXPORT_TO_GRID;
        }
        return RoutingDecision.SOLAR_ONLY;
    }

    /**
     * Handles scenario where solar output is insufficient to meet demand.
     * Cyclomatic Complexity: 3
     */
    private RoutingDecision handleSolarDeficitScenario(double batteryCharge) {
        if (batteryCharge > BatteryManager.LOW_BATTERY_THRESHOLD) {
            return RoutingDecision.SOLAR_AND_BATTERY;
        }
        return RoutingDecision.SOLAR_AND_GRID;
    }
}
