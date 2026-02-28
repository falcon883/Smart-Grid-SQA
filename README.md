# Smart-Grid Neighborhood Optimizer – SQA Test Suite

**Course:** MSCS3254 – Software Quality Assurance and Testing | Spring 2026  
**Project:** Project 4 – Smart-Grid Neighborhood Optimizer  
**Group:** Group 4  
**Team:** Durvank Deorukhkar | Percival Tapera | Harsha Vardhan Varma Kopanathi

---

## Project Overview

This repository contains the complete SQA test suite for the Smart-Grid Neighborhood Optimizer,
a system that uses IoT sensors, AI, and edge computing to balance energy consumption across
residential neighborhoods using solar panels and battery storage.

The test suite covers **Phase 2** of the SQA project:
- Unit tests (JUnit 5 + Mockito)
- Integration tests
- Selenium UI automation (Page Object Model)
- Cross-browser compatibility tests
- Code review documentation
- Test results report

---

## Repository Structure

```
smart-grid-sqa/
│
├── src/
│   ├── main/java/smartgrid/
│   │   ├── BatteryManager.java          # Battery charge/discharge logic
│   │   ├── DemandPredictor.java         # AI energy demand prediction engine
│   │   ├── RouteOptimizer.java          # Energy routing decision logic
│   │   ├── AlertService.java            # Alert service interface
│   │   ├── AlertServiceImpl.java        # Alert service implementation
│   │   ├── AuditLogger.java             # Routing decision audit log
│   │   ├── SolarSensorAPI.java          # Solar sensor interface (mocked in tests)
│   │   └── WeatherAPI.java              # Weather API interface (mocked in tests)
│   │
│   └── test/java/
│       ├── unit/
│       │   ├── BatteryManagerTest.java  # UT-001 – battery logic unit tests
│       │   ├── DemandPredictorTest.java # UT-002 – AI prediction unit tests
│       │   ├── AlertServiceTest.java    # UT-003 – alert service unit tests
│       │   └── AuditLoggerTest.java     # UT-004 – audit logger unit tests
│       │
│       ├── integration/
│       │   ├── SensorToRoutingIntegrationTest.java   # IT-001 – sensor → routing
│       │   └── DashboardDataFlowIntegrationTest.java # IT-002/003/004 – full pipeline
│       │
│       └── automation/
│           ├── pages/
│           │   ├── LoginPage.java       # POM: login page
│           │   ├── DashboardPage.java   # POM: main dashboard
│           │   ├── AlertsPage.java      # POM: alerts section
│           │   └── SettingsPage.java    # POM: settings/OTA page
│           └── tests/
│               ├── BaseTest.java        # WebDriver setup/teardown base class
│               ├── LoginTest.java       # TC-011: login flow tests
│               ├── DashboardTest.java   # TC-006/007/008: dashboard tests
│               └── CrossBrowserTest.java# TC-011/012/013: cross-browser tests
│
├── test-results/
│   └── TestResultsReport.md            # Pass/Fail report for all test suites
│
├── docs/
│   └── CodeReviewNotes.md              # Formal code review findings and resolutions
│
├── pom.xml                             # Maven build with JUnit 5, Mockito, Selenium
└── README.md                           # This file
```

---

## Prerequisites

| Tool          | Version  | Purpose                          |
|---------------|----------|----------------------------------|
| Java JDK      | 17+      | Compile and run tests            |
| Maven         | 3.8+     | Build and dependency management  |
| Chrome        | Latest   | Selenium Chrome tests            |
| Firefox       | Latest   | Selenium Firefox tests           |
| Safari        | 17+      | Selenium Safari tests (macOS only)|

---

## Running the Tests

### Run Unit + Integration Tests Only
```bash
mvn test -P unit-tests
```

### Run All Tests (Unit + Integration)
```bash
mvn test
```

### Run Selenium Tests (requires running dashboard at localhost:3000)
```bash
mvn test -P selenium-tests
```

### Run a specific test class
```bash
mvn test -Dtest=BatteryManagerTest
mvn test -Dtest=SensorToRoutingIntegrationTest
mvn test -Dtest=CrossBrowserTest
```

---

## Key Design Decisions

### Why Mockito?
`SolarSensorAPI` and `WeatherAPI` are interfaces representing physical IoT hardware and
external cloud services. Mocking them in unit tests allows us to:
- Test in isolation without hardware
- Simulate edge cases (sensor failure, extreme weather) repeatably
- Run tests in CI without network access

### Why Page Object Model?
POM separates page structure (locators, interactions) from test logic. Benefits:
- Single place to update locators when UI changes
- Tests read like plain English
- Reduces code duplication across test classes

### Why Explicit Waits?
The Energy Monitor Dashboard loads data asynchronously from IoT sensor APIs.
`Thread.sleep()` is non-deterministic and wastes time. Explicit waits with
`WebDriverWait` + `ExpectedConditions` wait exactly as long as needed.

---

## Test Coverage by Requirement

| Requirement | Description                         | Test Coverage       |
|-------------|-------------------------------------|---------------------|
| FR-001      | Sensor data ≤ 5 second interval     | TC-001, TC-002      |
| FR-002      | AI prediction MAE < 10%             | TC-003, UT-002      |
| FR-003      | Routing decision within 500ms       | TC-005, IT-001      |
| FR-004      | Dashboard 3 real-time metrics       | TC-006, DashboardTest |
| FR-005      | Low battery alert < 20%             | TC-008, UT-003      |
| FR-007      | Chrome + Firefox + Safari           | CrossBrowserTest    |
| FR-008      | Audit log with timestamps           | UT-004, IT tests    |
| NFR-004     | Cyclomatic Complexity ≤ 15          | Code Review CR2-01  |
| NFR-005     | Responsive 320px–1920px             | CrossBrowserTest    |

---

## Team Contributions

| Team Member                        | Contributions                                              |
|------------------------------------|------------------------------------------------------------|
| Durvank Deorukhkar                 | Integration tests, DashboardDataFlowIntegrationTest, RTM   |
| Percival Tapera                    | DemandPredictorTest, AlertsPage, SettingsPage, CrossBrowserTest |
| Harsha Vardhan Varma Kopanathi     | BatteryManagerTest, AuditLoggerTest, LoginPage, DashboardPage, LoginTest, DashboardTest |

---

*Last updated: February 26, 2026*
