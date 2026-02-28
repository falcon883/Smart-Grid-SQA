package automation.tests;

import automation.pages.DashboardPage;
import automation.pages.LoginPage;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DashboardTest.java
 *
 * Selenium automated tests for the Energy Monitor Dashboard main page.
 * Verifies real-time data display, connection error handling, and alert visibility.
 *
 * Test Cases Covered: TC-006, TC-007, TC-008
 * Requirements: FR-004, FR-005
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
@DisplayName("Dashboard Page - Selenium Tests")
class DashboardTest extends BaseTest {

    private DashboardPage dashboardPage;

    @BeforeEach
    void loginAndOpenDashboard() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateTo(BASE_URL);
        dashboardPage = loginPage.loginAs("admin", "password123");
        dashboardPage.waitForLoad();
    }

    // =========================================================================
    // TC-006: Dashboard displays all three real-time data widgets (FR-004)
    // =========================================================================

    @Test
    @DisplayName("TC-006: All three real-time data widgets are visible (FR-004)")
    void testAllDataWidgetsAreDisplayed() {
        // Assert: solar generation, battery level, and grid usage must all be visible
        assertTrue(dashboardPage.areAllDataWidgetsDisplayed(),
                "All three data widgets (solar, battery, grid) must be displayed (FR-004)");
    }

    @Test
    @DisplayName("TC-006b: Solar output widget displays a non-empty value")
    void testSolarOutputWidgetHasValue() {
        String solarValue = dashboardPage.getSolarOutputValue();
        assertNotNull(solarValue, "Solar output value should not be null");
        assertFalse(solarValue.isBlank(), "Solar output value should not be blank");
    }

    @Test
    @DisplayName("TC-006c: Battery level widget displays a non-empty value")
    void testBatteryLevelWidgetHasValue() {
        String batteryValue = dashboardPage.getBatteryLevelValue();
        assertNotNull(batteryValue, "Battery level value should not be null");
        assertFalse(batteryValue.isBlank(), "Battery level value should not be blank");
    }

    @Test
    @DisplayName("TC-006d: Grid usage widget displays a non-empty value")
    void testGridUsageWidgetHasValue() {
        String gridValue = dashboardPage.getGridUsageValue();
        assertNotNull(gridValue, "Grid usage value should not be null");
        assertFalse(gridValue.isBlank(), "Grid usage value should not be blank");
    }

    // =========================================================================
    // TC-007: Dashboard error handling when sensor connection is lost
    // =========================================================================

    @Test
    @DisplayName("TC-007: Error banner displayed when sensor connection unavailable")
    void testErrorBannerWhenConnectionLost() {
        // Navigate to a URL simulating sensor disconnection
        // In a real environment, the backend would expose a test endpoint to simulate this
        driver.get(BASE_URL + "/dashboard?simulate=sensor_offline");
        dashboardPage.waitForLoad();

        assertTrue(dashboardPage.isErrorBannerDisplayed(),
                "Error banner should be visible when sensor data is unavailable");
    }

    // =========================================================================
    // TC-008: Low battery alert displayed in dashboard (FR-005)
    // =========================================================================

    @Test
    @DisplayName("TC-008: Low battery alert is visible when battery below 20% (FR-005)")
    void testLowBatteryAlertVisible() {
        // Navigate with simulated low battery state
        driver.get(BASE_URL + "/dashboard?simulate=low_battery");
        dashboardPage.waitForLoad();

        assertTrue(dashboardPage.isLowBatteryAlertVisible(),
                "Low battery alert should be visible when charge is below 20% (FR-005)");
    }

    // =========================================================================
    // Logout test
    // =========================================================================

    @Test
    @DisplayName("Logout redirects to login page")
    void testLogoutRedirectsToLogin() {
        LoginPage loginPage = dashboardPage.logout();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/login"),
                "Should redirect to login page after logout. URL: " + currentUrl);
    }
}
