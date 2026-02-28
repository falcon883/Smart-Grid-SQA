package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AlertsPage.java
 *
 * Page Object Model for the Alerts section of the Energy Monitor Dashboard.
 * Handles visibility checks and retrieval of system alerts.
 *
 * Requirement: FR-005 - Alert when battery charge falls below 20%
 *
 * MSCS3254 - Group 4
 * Contributor: Percival Tapera
 */
public class AlertsPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // =========================================================================
    // Element Locators
    // =========================================================================
    private static final By ALERTS_PAGE_HEADER    = By.cssSelector("h1.alerts-title");
    private static final By ALERT_LIST            = By.cssSelector(".alert-item");
    private static final By LOW_BATTERY_ALERT     = By.cssSelector(".alert-item.low-battery");
    private static final By SENSOR_FAILURE_ALERT  = By.cssSelector(".alert-item.sensor-failure");
    private static final By SYSTEM_ALERT          = By.cssSelector(".alert-item.system");
    private static final By NO_ALERTS_MESSAGE     = By.cssSelector(".no-alerts-message");
    private static final By DISMISS_ALL_BUTTON    = By.id("dismiss-all-alerts");
    private static final By ALERT_TIMESTAMP       = By.cssSelector(".alert-item .timestamp");

    public AlertsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Waits for the alerts page to load.
     */
    public AlertsPage waitForLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(ALERTS_PAGE_HEADER));
        return this;
    }

    /**
     * Returns true if a low battery alert is currently displayed (FR-005 / TC-008).
     */
    public boolean isLowBatteryAlertDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(LOW_BATTERY_ALERT))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the text of the low battery alert.
     */
    public String getLowBatteryAlertText() {
        WebElement alert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(LOW_BATTERY_ALERT));
        return alert.getText();
    }

    /**
     * Returns true if a sensor failure alert is displayed.
     */
    public boolean isSensorFailureAlertDisplayed() {
        try {
            return driver.findElements(SENSOR_FAILURE_ALERT).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the total number of active alerts displayed.
     */
    public int getAlertCount() {
        try {
            List<WebElement> alerts = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(ALERT_LIST));
            return alerts.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Returns all alert text messages as a list.
     */
    public List<String> getAllAlertMessages() {
        List<WebElement> alerts = driver.findElements(ALERT_LIST);
        return alerts.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the "no alerts" message is displayed (empty state).
     */
    public boolean isNoAlertsMessageDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(NO_ALERTS_MESSAGE))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clicks the "Dismiss All" button to clear all alerts.
     */
    public AlertsPage dismissAllAlerts() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(DISMISS_ALL_BUTTON));
        btn.click();
        return this;
    }
}
