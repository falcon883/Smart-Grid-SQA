package automation.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * BaseTest.java
 *
 * Abstract base class for all Selenium automation tests.
 * Handles WebDriver initialization, configuration, and teardown.
 * Uses WebDriverManager to automatically manage browser driver binaries.
 *
 * Subclasses can override getBrowser() to run on a specific browser.
 * Default browser is Chrome (headless for CI environments).
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
public abstract class BaseTest {

    protected WebDriver     driver;
    protected WebDriverWait wait;

    // Base URL of the Energy Monitor Dashboard
    // Update this to your actual deployment URL when testing
    protected static final String BASE_URL     = "http://localhost:3000";
    protected static final int    TIMEOUT_SECS = 15;

    /**
     * Override in subclass to specify a different browser.
     * Supported values: "chrome", "firefox", "safari"
     */
    protected String getBrowser() {
        return "chrome";
    }

    @BeforeEach
    void initDriver() {
        switch (getBrowser().toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("--headless");
                driver = new FirefoxDriver(options);
            }
            case "safari" -> {
                // Safari uses built-in safaridriver - no WebDriverManager needed
                // Note: Safari requires "Allow Remote Automation" enabled in Develop menu
                // SafariDriver does not support headless mode
                // driver = new SafariDriver();
                // Skipped in CI: throw UnsupportedOperationException for automated runs
                throw new UnsupportedOperationException(
                        "Safari tests must be run on macOS with safaridriver enabled.");
            }
            default -> { // chrome
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--window-size=1920,1080");
                driver = new ChromeDriver(options);
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECS));
    }

    @AfterEach
    void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
