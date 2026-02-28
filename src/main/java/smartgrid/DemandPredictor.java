package smartgrid;

/**
 * DemandPredictor.java
 *
 * AI-powered energy demand prediction engine for the Smart-Grid Neighborhood Optimizer.
 * Predicts energy demand for the next 24 hours based on weather data and historical usage.
 *
 * Requirement: FR-002 - MAE of prediction must be less than 10% of actual demand.
 *
 * MSCS3254 - Group 4
 */
public class DemandPredictor {

    // Prediction bounds (kWh) - prevents runaway AI predictions
    public static final double MIN_PREDICTED_DEMAND = 0.5;
    public static final double MAX_PREDICTED_DEMAND = 50.0;

    // Baseline demand per hour for the neighborhood (kWh)
    private static final double BASELINE_DEMAND_KWH = 10.0;

    // Weighting factors for the simplified prediction model
    private static final double TEMP_WEIGHT           = 0.3;
    private static final double CLOUD_WEIGHT          = 0.2;
    private static final double IRRADIANCE_WEIGHT     = 0.5;

    private final WeatherAPI weatherAPI;

    /**
     * Constructor with injected WeatherAPI dependency.
     * Injection enables Mockito mocking in unit tests.
     */
    public DemandPredictor(WeatherAPI weatherAPI) {
        this.weatherAPI = weatherAPI;
    }

    /**
     * Predicts the energy demand for the next 24 hours in kWh.
     *
     * If weather API is unavailable, returns the baseline demand as a safe fallback.
     * Predicted value is always clamped between MIN and MAX bounds.
     *
     * @return predicted demand in kWh
     */
    public double predict() {
        if (!weatherAPI.isAvailable()) {
            // Graceful degradation: return baseline demand when API is unavailable
            return BASELINE_DEMAND_KWH;
        }

        double temperature  = weatherAPI.getCurrentTemperatureCelsius();
        double cloudCover   = weatherAPI.getCloudCoveragePercent();
        double irradiance   = weatherAPI.getForecastedIrradiance();

        // Simplified linear prediction model
        // Higher temp → more AC usage → higher demand
        // Higher cloud cover → less solar → more grid/battery demand
        // Lower irradiance → less renewable supply → higher net demand
        double tempFactor       = (temperature / 30.0) * TEMP_WEIGHT;
        double cloudFactor      = (cloudCover / 100.0) * CLOUD_WEIGHT;
        double irradianceFactor = (1.0 - (irradiance / 1000.0)) * IRRADIANCE_WEIGHT;

        double adjustmentFactor = tempFactor + cloudFactor + irradianceFactor;
        double predicted        = BASELINE_DEMAND_KWH * (1.0 + adjustmentFactor);

        // Clamp to safe bounds to prevent invalid predictions
        return clamp(predicted, MIN_PREDICTED_DEMAND, MAX_PREDICTED_DEMAND);
    }

    /**
     * Calculates the Mean Absolute Error (MAE) of a prediction against actual demand.
     * Used to verify FR-002: MAE must be < 10% of actual demand.
     *
     * @param predicted  Predicted demand in kWh
     * @param actual     Actual measured demand in kWh
     * @return           MAE as a percentage of actual demand
     */
    public double calculateMAEPercent(double predicted, double actual) {
        if (actual == 0) {
            throw new IllegalArgumentException("Actual demand cannot be zero.");
        }
        return (Math.abs(predicted - actual) / actual) * 100.0;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
