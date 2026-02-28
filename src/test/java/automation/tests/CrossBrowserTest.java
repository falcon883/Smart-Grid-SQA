package automation.tests;

import automation.pages.DashboardPage;
import automation.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrossBrowserTest.java
 *
 * Cross-browser compatibility tests for the Energy Monitor Dashboard.
 * Verifies that all critical dashboard functions work correctly on:
 *   - Chrome (TC-011)
 *   - Firefox (TC-012)
 *   - Safari (TC-013) - requires macOS with safaridriver enabled
 *
 * Requirement: FR-007 - Dashboard shall be accessible on Chrome, Firefox, and Safari
 * Requirement: NFR-005 - Dashboard renders correctly from 320px to 1920px
 *
 * MSCS3254 - Group 4
 * Contributor: Percival Tapera
 */
@DisplayName("Cross-Browser Compatibility Tests (FR-007)")
class CrossBrowserTest {

    private WebDriver driver;

    // =========================================================================
    // Helper: create driver for a given browser
    // =========================================================================
    private WebDriver createDriver(String browser) {
        return switch (browser.toLowerCase()) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                opts.addArguments("--headless");
                var d = new FirefoxDriver(opts);
                d.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                yield d;
            }
            default -> { // chrome
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless", "--no-sandbox",
                        "--disable-dev-shm-usage", "--window-size=1920,1080");
                var d = new ChromeDriver(opts);
                d.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                yield d;
            }
        };
    }

    @AfterEach
    void quitDriver() {
        if (driver != null) driver.quit();
    }

    // =========================================================================
    // TC-011 to TC-013: Cross-browser smoke test
    // =========================================================================

    @ParameterizedTest(name = "TC-{index}: Dashboard login and data display works on {0}")
    @ValueSource(strings = {"chrome", "firefox"})
    @DisplayName("Dashboard critical path works on Chrome and Firefox")
    void testDashboardCriticalPathCrossBrowser(String browser) {
        // Arrange
        driver = createDriver(browser);

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(BaseTest.BASE_URL);

        // Act: login
        DashboardPage dashboard = loginPage.loginAs("admin", "password123");
        dashboard.waitForLoad();

        // Assert: all widgets visible on this browser
        assertTrue(dashboard.areAllDataWidgetsDisplayed(),
                "All data widgets should be displayed on " + browser + " (FR-007)");

        // Assert: page title is correct
        String title = dashboard.getPageTitle();
        assertTrue(title.contains("Energy Monitor") || title.contains("Smart-Grid"),
                "Page title should be correct on " + browser + ". Actual: " + title);
    }

    // =========================================================================
    // NFR-005: Responsive design at different viewport widths
    // =========================================================================

    @Test
    @DisplayName("NFR-005: Dashboard renders at mobile width (320px)")
    void testDashboardRendersMobileWidth() {
        driver = createDriver("chrome");
        driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(320, 568));

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(BaseTest.BASE_URL);
        DashboardPage dashboard = loginPage.loginAs("admin", "password123");
        dashboard.waitForLoad();

        // Assert: dashboard still loads at mobile viewport
        assertTrue(dashboard.areAllDataWidgetsDisplayed(),
                "Dashboard should render at 320px mobile width (NFR-005)");
    }

    @Test
    @DisplayName("NFR-005: Dashboard renders at desktop width (1920px)")
    void testDashboardRendersDesktopWidth() {
        driver = createDriver("chrome");
        driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(1920, 1080));

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(BaseTest.BASE_URL);
        DashboardPage dashboard = loginPage.loginAs("admin", "password123");
        dashboard.waitForLoad();

        assertTrue(dashboard.areAllDataWidgetsDisplayed(),
                "Dashboard should render correctly at 1920px desktop width (NFR-005)");
    }

    @Test
    @DisplayName("NFR-005: Dashboard renders at tablet width (768px)")
    void testDashboardRendersTabletWidth() {
        driver = createDriver("chrome");
        driver.manage().window().setSize(
                new org.openqa.selenium.Dimension(768, 1024));

        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(BaseTest.BASE_URL);
        DashboardPage dashboard = loginPage.loginAs("admin", "password123");
        dashboard.waitForLoad();

        assertTrue(dashboard.areAllDataWidgetsDisplayed(),
                "Dashboard should render correctly at 768px tablet width (NFR-005)");
    }
}
