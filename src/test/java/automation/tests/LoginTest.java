package automation.tests;

import automation.pages.DashboardPage;
import automation.pages.LoginPage;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginTest.java
 *
 * Selenium automated tests for the Energy Monitor Dashboard login page.
 * Tests valid login, invalid credentials, and empty field validation.
 *
 * Test Cases Covered: TC-011, TC-012, TC-013 (cross-browser)
 * Requirement: FR-004 (dashboard access after login)
 *
 * NOTE: These tests require the Energy Monitor Dashboard to be running at BASE_URL.
 * Run with Maven profile: mvn test -P selenium-tests
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
@DisplayName("Login Page - Selenium Tests (Chrome)")
class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        loginPage = new LoginPage(driver);
        loginPage.navigateTo(BASE_URL);
    }

    // =========================================================================
    // Valid Login Tests
    // =========================================================================

    @Test
    @DisplayName("TC-011a: Valid credentials redirect to dashboard")
    void testValidLoginRedirectsToDashboard() {
        // Act
        DashboardPage dashboard = loginPage.loginAs("admin", "password123");
        dashboard.waitForLoad();

        // Assert: URL should contain /dashboard
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/dashboard"),
                "Should redirect to dashboard after valid login. URL: " + currentUrl);
    }

    @Test
    @DisplayName("TC-011b: Dashboard title is correct after login")
    void testDashboardTitleAfterLogin() {
        DashboardPage dashboard = loginPage.loginAs("admin", "password123");
        dashboard.waitForLoad();

        String title = dashboard.getPageTitle();
        assertTrue(title.contains("Energy Monitor") || title.contains("Smart-Grid"),
                "Dashboard title should contain 'Energy Monitor' or 'Smart-Grid'. Actual: " + title);
    }

    // =========================================================================
    // Invalid Login Tests (Equivalence Partitioning - Invalid Class)
    // =========================================================================

    @Test
    @DisplayName("TC-011c: Invalid password shows error message")
    void testInvalidPasswordShowsError() {
        // Act
        loginPage.enterUsername("admin");
        loginPage.enterPassword("wrongpassword");
        loginPage.clickLoginButton();

        // Assert: error message should be visible
        assertTrue(loginPage.isErrorMessageDisplayed(),
                "Error message should be displayed for invalid credentials");
        String errorText = loginPage.getErrorMessage();
        assertFalse(errorText.isBlank(),
                "Error message text should not be blank");
    }

    @Test
    @DisplayName("TC-011d: Non-existent username shows error")
    void testNonExistentUsernameShowsError() {
        loginPage.enterUsername("nonexistent_user_xyz");
        loginPage.enterPassword("anypassword");
        loginPage.clickLoginButton();

        assertTrue(loginPage.isErrorMessageDisplayed(),
                "Error message should be displayed for non-existent user");
    }

    @Test
    @DisplayName("TC-011e: Empty username and password shows validation error")
    void testEmptyFieldsShowsError() {
        // Act: click login without entering credentials
        loginPage.clickLoginButton();

        // Assert: validation error should appear
        assertTrue(loginPage.isErrorMessageDisplayed(),
                "Validation error should appear when credentials are empty");
    }
}
