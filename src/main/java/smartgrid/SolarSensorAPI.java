package smartgrid;

/**
 * SolarSensorAPI.java
 *
 * Interface for the solar panel sensor data feed.
 * In production, this connects to physical IoT sensor hardware.
 * In tests, this interface is mocked using Mockito.
 *
 * MSCS3254 - Group 4
 */
public interface SolarSensorAPI {

    /**
     * Returns the current solar panel energy output in kWh.
     * Data is expected to refresh every 5 seconds per FR-001.
     *
     * @return current solar output in kWh
     */
    double getCurrentOutputKwh();

    /**
     * Returns the timestamp (epoch ms) of the last sensor reading.
     *
     * @return epoch milliseconds of last reading
     */
    long getLastReadingTimestamp();

    /**
     * Returns true if the sensor is currently reachable and reporting.
     *
     * @return sensor availability status
     */
    boolean isAvailable();
}
