package smartgrid;

/**
 * AlertService.java
 *
 * Interface for sending alerts to homeowners and system operators.
 * Alerts are triggered by system events such as low battery, sensor failure, or threshold breaches.
 *
 * Requirement: FR-005 - System shall send alert when battery charge falls below 20%.
 *
 * In tests, this interface is mocked with Mockito to capture alert calls
 * without sending real notifications.
 *
 * MSCS3254 - Group 4
 */
public interface AlertService {

    /**
     * Sends a low battery alert to the homeowner.
     *
     * @param currentChargePercent  The current battery charge level that triggered the alert
     */
    void sendLowBatteryAlert(double currentChargePercent);

    /**
     * Sends a sensor failure alert when an IoT sensor stops reporting.
     *
     * @param sensorId  Identifier of the failed sensor
     */
    void sendSensorFailureAlert(String sensorId);

    /**
     * Sends a general system alert with a custom message.
     *
     * @param message  The alert message to send
     */
    void sendSystemAlert(String message);

    /**
     * Returns the number of alerts sent since the service was initialized.
     * Useful for testing that the correct number of alerts were triggered.
     *
     * @return total alert count
     */
    int getAlertCount();
}
