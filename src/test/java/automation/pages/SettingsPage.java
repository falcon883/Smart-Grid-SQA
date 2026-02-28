package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * SettingsPage.java
 *
 * Page Object Model for the Settings / Configuration page.
 * Covers alert threshold configuration and OTA firmware update initiation.
 *
 * Requirement: FR-006 - OTA firmware update with <= 2 minute downtime
 *
 * MSCS3254 - Group 4
 * Contributor: Percival Tapera
 */
public class SettingsPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // =========================================================================
    // Element Locators
    // =========================================================================
    private static final By SETTINGS_HEADER          = By.cssSelector("h1.settings-title");
    private static final By LOW_BATTERY_THRESHOLD_INPUT = By.id("low-battery-threshold");
    private static final By SAVE_SETTINGS_BUTTON     = By.id("save-settings-btn");
    private static final By SUCCESS_TOAST            = By.cssSelector(".toast.success");
    private static final By OTA_UPDATE_BUTTON        = By.id("initiate-ota-btn");
    private static final By OTA_STATUS_LABEL         = By.id("ota-status");
    private static final By FIRMWARE_VERSION_LABEL   = By.id("firmware-version");

    public SettingsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Waits for the settings page to fully load.
     */
    public SettingsPage waitForLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(SETTINGS_HEADER));
        return this;
    }

    /**
     * Sets the low battery alert threshold value.
     *
     * @param threshold  Percentage value for the low battery threshold
     */
    public SettingsPage setLowBatteryThreshold(int threshold) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(LOW_BATTERY_THRESHOLD_INPUT));
        input.clear();
        input.sendKeys(String.valueOf(threshold));
        return this;
    }

    /**
     * Saves the settings and waits for the success toast notification.
     */
    public SettingsPage saveSettings() {
        WebElement saveBtn = wait.until(
                ExpectedConditions.elementToBeClickable(SAVE_SETTINGS_BUTTON));
        saveBtn.click();
        // Wait for success confirmation
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_TOAST));
        return this;
    }

    /**
     * Returns true if the success toast is displayed after saving.
     */
    public boolean isSuccessToastDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(SUCCESS_TOAST)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initiates an OTA firmware update.
     * Returns the OTA status text after initiation.
     */
    public String initiateOTAUpdate() {
        WebElement otaBtn = wait.until(
                ExpectedConditions.elementToBeClickable(OTA_UPDATE_BUTTON));
        otaBtn.click();
        // Wait for OTA status to update
        WebElement status = wait.until(
                ExpectedConditions.visibilityOfElementLocated(OTA_STATUS_LABEL));
        return status.getText();
    }

    /**
     * Returns the current firmware version displayed on the settings page.
     */
    public String getFirmwareVersion() {
        WebElement versionLabel = wait.until(
                ExpectedConditions.visibilityOfElementLocated(FIRMWARE_VERSION_LABEL));
        return versionLabel.getText();
    }
}
