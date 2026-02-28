package smartgrid;

/**
 * WeatherAPI.java
 *
 * Interface for the external weather data service used by the AI Demand Predictor.
 * Mocked in tests to simulate normal, cloudy, and extreme weather conditions.
 *
 * MSCS3254 - Group 4
 */
public interface WeatherAPI {

    /**
     * Returns the current temperature in Celsius.
     */
    double getCurrentTemperatureCelsius();

    /**
     * Returns the current cloud coverage as a percentage (0–100).
     */
    double getCloudCoveragePercent();

    /**
     * Returns the forecasted solar irradiance for the next 24 hours (W/m²).
     */
    double getForecastedIrradiance();

    /**
     * Returns true if the weather API is available and responding.
     */
    boolean isAvailable();
}
