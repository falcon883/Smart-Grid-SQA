package smartgrid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AlertServiceImpl.java
 *
 * Concrete implementation of AlertService.
 * In production, this would send push notifications, emails, or SMS.
 * For the SQA project, it logs alerts and tracks alert counts.
 *
 * MSCS3254 - Group 4
 */
public class AlertServiceImpl implements AlertService {

    private static final Logger logger     = LoggerFactory.getLogger(AlertServiceImpl.class);
    private final AtomicInteger alertCount = new AtomicInteger(0);

    @Override
    public void sendLowBatteryAlert(double currentChargePercent) {
        alertCount.incrementAndGet();
        logger.warn("[ALERT] Low battery: current charge is {}%. Threshold is {}%.",
                currentChargePercent, BatteryManager.LOW_BATTERY_THRESHOLD);
    }

    @Override
    public void sendSensorFailureAlert(String sensorId) {
        alertCount.incrementAndGet();
        logger.error("[ALERT] Sensor failure detected for sensor ID: {}", sensorId);
    }

    @Override
    public void sendSystemAlert(String message) {
        alertCount.incrementAndGet();
        logger.warn("[ALERT] System alert: {}", message);
    }

    @Override
    public int getAlertCount() {
        return alertCount.get();
    }
}
