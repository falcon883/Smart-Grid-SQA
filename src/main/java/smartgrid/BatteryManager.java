package smartgrid;

/**
 * BatteryManager.java
 *
 * Manages battery charge/discharge operations for the Smart-Grid Neighborhood Optimizer.
 * Determines the optimal discharge rate based on current solar output and energy demand.
 *
 * MSCS3254 - Group 4
 */
public class BatteryManager {

    // Constants
    public static final double LOW_BATTERY_THRESHOLD = 20.0;   // percent
    public static final double MAX_CHARGE_LEVEL      = 100.0;  // percent
    public static final double MAX_DISCHARGE_RATE    = 5.0;    // kWh per cycle

    private final SolarSensorAPI solarSensor;
    private final AlertService   alertService;

    private double currentChargeLevel; // percent (0–100)

    /**
     * Constructor with injected dependencies (enables mocking in tests).
     */
    public BatteryManager(SolarSensorAPI solarSensor, AlertService alertService) {
        this.solarSensor       = solarSensor;
        this.alertService      = alertService;
        this.currentChargeLevel = 100.0;
    }

    /**
     * Calculates the optimal discharge rate (kWh) given current solar output and demand.
     *
     * Logic:
     *   - If solar output covers demand fully → no discharge needed
     *   - If solar is insufficient → discharge = demand - solarOutput, capped at MAX_DISCHARGE_RATE
     *   - If battery is at or below LOW_BATTERY_THRESHOLD → trigger alert, return 0 (protect battery)
     *
     * @param demandKwh  Current energy demand in kWh
     * @return           Optimal discharge amount in kWh
     */
    public double calculateOptimalDischarge(double demandKwh) {
        if (demandKwh < 0) {
            throw new IllegalArgumentException("Demand cannot be negative: " + demandKwh);
        }

        double solarOutput = solarSensor.getCurrentOutputKwh();

        // Battery protection: if charge is critically low, do not discharge
        if (currentChargeLevel <= LOW_BATTERY_THRESHOLD) {
            alertService.sendLowBatteryAlert(currentChargeLevel);
            return 0.0;
        }

        // Solar fully covers demand
        if (solarOutput >= demandKwh) {
            return 0.0;
        }

        // Calculate shortfall
        double shortfall = demandKwh - solarOutput;

        // Cap discharge at maximum safe discharge rate
        return Math.min(shortfall, MAX_DISCHARGE_RATE);
    }

    /**
     * Charges the battery by the given amount (kWh converted to percent for simulation).
     * In the real system this would interface with the BMS hardware.
     *
     * @param chargeKwh  Energy to add in kWh
     */
    public void charge(double chargeKwh) {
        if (chargeKwh < 0) {
            throw new IllegalArgumentException("Charge amount cannot be negative.");
        }
        // Simplified: 1 kWh ≈ 1% charge for simulation purposes
        currentChargeLevel = Math.min(MAX_CHARGE_LEVEL, currentChargeLevel + chargeKwh);
    }

    /**
     * Discharges the battery by the given amount.
     *
     * @param dischargeKwh  Energy to remove in kWh
     */
    public void discharge(double dischargeKwh) {
        if (dischargeKwh < 0) {
            throw new IllegalArgumentException("Discharge amount cannot be negative.");
        }
        currentChargeLevel = Math.max(0.0, currentChargeLevel - dischargeKwh);

        if (currentChargeLevel <= LOW_BATTERY_THRESHOLD) {
            alertService.sendLowBatteryAlert(currentChargeLevel);
        }
    }

    public double getCurrentChargeLevel() {
        return currentChargeLevel;
    }

    public void setCurrentChargeLevel(double level) {
        if (level < 0 || level > MAX_CHARGE_LEVEL) {
            throw new IllegalArgumentException("Charge level must be between 0 and 100.");
        }
        this.currentChargeLevel = level;
    }
}
