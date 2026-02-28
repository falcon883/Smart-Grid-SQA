package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * DashboardPage.java
 *
 * Page Object Model for the Energy Monitor Dashboard main page.
 * Covers real-time data display for solar generation, battery level, and grid usage.
 *
 * Requirement: FR-004 - Dashboard shall display battery charge, solar generation,
 * and grid usage in real time.
 *
 * All data elements use visibilityOfElementLocated waits because they load
 * asynchronously from the IoT sensor API.
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
public class DashboardPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // =========================================================================
    // Element Locators
    // =========================================================================
    // Real-time data widgets
    private static final By SOLAR_OUTPUT_WIDGET   = By.id("solar-output-value");
    private static final By BATTERY_LEVEL_WIDGET  = By.id("battery-level-value");
    private static final By GRID_USAGE_WIDGET     = By.id("grid-usage-value");
    private static final By DEMAND_FORECAST_WIDGET = By.id("demand-forecast-value");

    // Navigation and status
    private static final By DASHBOARD_HEADER      = By.cssSelector("h1.dashboard-title");
    private static final By CONNECTION_STATUS     = By.id("connection-status");
    private static final By LAST_UPDATED_LABEL    = By.id("last-updated");
    private static final By ERROR_BANNER          = By.cssSelector(".error-banner");

    // Alerts section
    private static final By ALERTS_PANEL          = By.id("alerts-panel");
    private static final By LOW_BATTERY_ALERT     = By.cssSelector(".alert.low-battery");

    // Settings link
    private static final By SETTINGS_LINK         = By.id("settings-nav-link");
    private static final By LOGOUT_BUTTON         = By.id("logout-btn");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Waits for the dashboard to fully load by checking the header is visible.
     */
    public DashboardPage waitForLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(DASHBOARD_HEADER));
        return this;
    }

    /**
     * Returns the solar output value displayed on the dashboard.
     * Uses visibilityOfElementLocated because data loads asynchronously from sensor API.
     *
     * @return solar output text (e.g., "8.5 kWh")
     */
    public String getSolarOutputValue() {
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(SOLAR_OUTPUT_WIDGET));
        return element.getText();
    }

    /**
     * Returns the battery charge level displayed on the dashboard.
     *
     * @return battery level text (e.g., "75%")
     */
    public String getBatteryLevelValue() {
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(BATTERY_LEVEL_WIDGET));
        return element.getText();
    }

    /**
     * Returns the grid usage value displayed on the dashboard.
     *
     * @return grid usage text (e.g., "2.3 kWh")
     */
    public String getGridUsageValue() {
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(GRID_USAGE_WIDGET));
        return element.getText();
    }

    /**
     * Returns the demand forecast value displayed on the dashboard.
     */
    public String getDemandForecastValue() {
        WebElement element = wait.until(
                ExpectedConditions.visibilityOfElementLocated(DEMAND_FORECAST_WIDGET));
        return element.getText();
    }

    /**
     * Returns true if all three required real-time data widgets are displayed (FR-004).
     */
    public boolean areAllDataWidgetsDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(SOLAR_OUTPUT_WIDGET));
            wait.until(ExpectedConditions.visibilityOfElementLocated(BATTERY_LEVEL_WIDGET));
            wait.until(ExpectedConditions.visibilityOfElementLocated(GRID_USAGE_WIDGET));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the error banner is displayed (e.g., sensor connection lost).
     */
    public boolean isErrorBannerDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(ERROR_BANNER)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the low battery alert is visible in the alerts panel (FR-005).
     */
    public boolean isLowBatteryAlertVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(LOW_BATTERY_ALERT)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Waits for the data to be updated by checking textToBePresentInElement.
     * Used to verify live data refresh without manual page reload.
     *
     * @param expectedPartialText  Partial text expected in the solar widget after update
     */
    public boolean waitForDataUpdate(String expectedPartialText) {
        try {
            return wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    SOLAR_OUTPUT_WIDGET, expectedPartialText));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the connection status indicator text.
     */
    public String getConnectionStatus() {
        WebElement status = wait.until(
                ExpectedConditions.visibilityOfElementLocated(CONNECTION_STATUS));
        return status.getText();
    }

    /**
     * Navigates to the Settings page.
     */
    public SettingsPage goToSettings() {
        WebElement settingsLink = wait.until(
                ExpectedConditions.elementToBeClickable(SETTINGS_LINK));
        settingsLink.click();
        return new SettingsPage(driver);
    }

    /**
     * Logs out of the dashboard.
     */
    public LoginPage logout() {
        WebElement logoutBtn = wait.until(
                ExpectedConditions.elementToBeClickable(LOGOUT_BUTTON));
        logoutBtn.click();
        return new LoginPage(driver);
    }

    /**
     * Returns the page title.
     */
    public String getPageTitle() {
        return driver.getTitle();
    }
}
