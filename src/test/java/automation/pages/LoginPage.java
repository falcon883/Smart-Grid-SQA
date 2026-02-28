package automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * LoginPage.java
 *
 * Page Object Model for the Energy Monitor Dashboard Login Page.
 * Encapsulates all interactions with login page elements.
 *
 * Using Page Object Model (POM) pattern to separate page structure from test logic,
 * improving maintainability and reducing code duplication.
 *
 * Code Review Finding (Review #1): Added explicit wait on submit button
 * to prevent StaleElementReferenceException on slow network connections.
 *
 * MSCS3254 - Group 4
 * Contributor: Harsha Vardhan Varma Kopanathi
 */
public class LoginPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;

    // =========================================================================
    // Element Locators
    // =========================================================================
    private static final By USERNAME_INPUT    = By.id("username");
    private static final By PASSWORD_INPUT    = By.id("password");
    private static final By LOGIN_BUTTON      = By.id("login-btn");
    private static final By ERROR_MESSAGE     = By.cssSelector(".login-error");
    private static final By WELCOME_HEADING   = By.cssSelector("h1.welcome");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * Navigates to the login page.
     */
    public LoginPage navigateTo(String baseUrl) {
        driver.get(baseUrl + "/login");
        // Wait for the login form to be fully rendered
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        return this;
    }

    /**
     * Enters the username into the username field.
     */
    public LoginPage enterUsername(String username) {
        WebElement usernameField = wait.until(
                ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        usernameField.clear();
        usernameField.sendKeys(username);
        return this;
    }

    /**
     * Enters the password into the password field.
     */
    public LoginPage enterPassword(String password) {
        WebElement passwordField = wait.until(
                ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passwordField.clear();
        passwordField.sendKeys(password);
        return this;
    }

    /**
     * Clicks the login button.
     * Uses elementToBeClickable wait to prevent race conditions on slow loads.
     * (Code Review Fix #1: Added explicit wait here)
     *
     * @return DashboardPage if login succeeds
     */
    public DashboardPage clickLoginButton() {
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(LOGIN_BUTTON));
        loginBtn.click();
        return new DashboardPage(driver);
    }

    /**
     * Performs the full login sequence in one call.
     */
    public DashboardPage loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickLoginButton();
    }

    /**
     * Returns the error message text displayed after a failed login attempt.
     */
    public String getErrorMessage() {
        WebElement error = wait.until(
                ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE));
        return error.getText();
    }

    /**
     * Returns true if the error message element is visible on the page.
     */
    public boolean isErrorMessageDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the page title of the login page.
     */
    public String getPageTitle() {
        return driver.getTitle();
    }
}
