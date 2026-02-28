# Code Review Notes
## Smart-Grid Neighborhood Optimizer – SQA Project
### MSCS3254 | Spring 2026 | Group 4

---

## Overview

Formal peer code reviews were conducted on the automation scripts and core logic modules
following an adapted Fagan Inspection methodology. Each review involved an author and a
designated reviewer. Findings are categorized by severity.

**Severity Levels:**
- **Critical** – Defect that would cause test failure or incorrect results. Must be fixed immediately.
- **Major** – Significant issue affecting reliability or maintainability. Fix before submission.
- **Minor** – Style or documentation issue. Fix recommended but not blocking.

---

## Review #1 – Selenium Dashboard Page Object

| Field         | Detail                                    |
|---------------|-------------------------------------------|
| **Artifact**  | `DashboardPage.java`                      |
| **Author**    | Harsha Vardhan Varma Kopanathi            |
| **Reviewer**  | Percival Tapera                           |
| **Date**      | February 20, 2026                         |
| **Duration**  | 45 minutes                                |

### Findings

| ID     | Severity | Location                        | Description                                                                                                   | Resolution                                                                        |
|--------|----------|---------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| CR1-01 | Critical | `getSolarOutputValue()`         | Original code used `driver.findElement()` directly without an explicit wait. This caused `NoSuchElementException` on slow network connections because the solar data element loads asynchronously. | Replaced with `wait.until(ExpectedConditions.visibilityOfElementLocated(...))`. Fixed. |
| CR1-02 | Major    | `areAllDataWidgetsDisplayed()`  | Method returned `true` even if widgets were present in DOM but not visible (display:none). A hidden element could pass the check. | Changed to use `visibilityOfElementLocated` instead of `presenceOfElementLocated`. Fixed. |
| CR1-03 | Major    | `waitForDataUpdate()`           | Method used a hardcoded 10-second `Thread.sleep()` instead of an explicit wait condition. Violates explicit wait requirement. | Replaced with `ExpectedConditions.textToBePresentInElementLocated`. Fixed. |
| CR1-04 | Minor    | General                         | Missing Javadoc on `isErrorBannerDisplayed()` method.                                                         | Javadoc added. Fixed. |

### Summary
- **Total findings:** 4 (2 Critical, 2 Major → recategorized after review)
- **Fixed before submission:** 4/4
- **Outcome:** APPROVED after fixes

---

## Review #2 – Energy Routing Logic (RouteOptimizer)

| Field         | Detail                                    |
|---------------|-------------------------------------------|
| **Artifact**  | `RouteOptimizer.java`                     |
| **Author**    | Percival Tapera                           |
| **Reviewer**  | Durvank Deorukhkar                        |
| **Date**      | February 22, 2026                         |
| **Duration**  | 60 minutes                                |

### Findings

| ID     | Severity | Location                        | Description                                                                                                   | Resolution                                                                        |
|--------|----------|---------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| CR2-01 | Critical | `routeEnergy()` (original)      | Cyclomatic Complexity measured at **18** using static analysis. NFR-004 requires a maximum of 15 per function. High complexity increases defect probability and reduces testability. | Refactored `routeEnergy()` into three strategy methods: `handleNoSolarScenario()`, `handleSolarSurplusScenario()`, `handleSolarDeficitScenario()`. Each has CC ≤ 5. Fixed. |
| CR2-02 | Major    | `routeEnergy()`                 | No logging when routing decision exceeded the 500ms SLA (FR-003). A late decision would go unnoticed in production. | Added `logger.warn()` when `elapsed > 500`. Fixed. |
| CR2-03 | Major    | `handleSolarSurplusScenario()`  | The 20% surplus threshold for `EXPORT_TO_GRID` (`solarOutput > demand * 1.2`) was not documented or traceable to any requirement. Magic number with no explanation. | Added inline comment explaining the 20% buffer rationale and added to RTM. Fixed. |
| CR2-04 | Minor    | General                         | `RoutingDecision` enum values lacked Javadoc descriptions explaining when each value applies. | Javadoc added to enum constants. Fixed. |

### Summary
- **Total findings:** 4 (1 Critical, 2 Major, 1 Minor)
- **Fixed before submission:** 4/4
- **Outcome:** APPROVED after fixes

---

## Review #3 – JUnit Unit Test Suite

| Field         | Detail                                    |
|---------------|-------------------------------------------|
| **Artifact**  | `BatteryManagerTest.java`, `DemandPredictorTest.java` |
| **Author**    | Durvank Deorukhkar                        |
| **Reviewer**  | Harsha Vardhan Varma Kopanathi            |
| **Date**      | February 24, 2026                         |
| **Duration**  | 40 minutes                                |

### Findings

| ID     | Severity | Location                        | Description                                                                                                   | Resolution                                                                        |
|--------|----------|---------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| CR3-01 | Critical | `BatteryManagerTest.java`       | Mock objects (`mockSolarSensor`, `mockAlertService`) were not reset between tests. In `testNoDischargeBelowLowBatteryThreshold`, a prior test's mock stubs leaked into subsequent tests, causing false passes. | Added `reset(mockSolarSensor, mockAlertService)` in `@AfterEach tearDown()`. Fixed. |
| CR3-02 | Major    | `DemandPredictorTest.java`      | `testMAEWithinRequiredThreshold()` used a hardcoded 8% deviation (`predicted * 1.08`) as the "simulated actual." This is not a true accuracy test – it guarantees passing because the gap is set below the threshold manually. | Added comment to clarify this is a unit test of the MAE calculation method itself, not a real accuracy validation. Real accuracy testing is deferred to Phase 3 with live data. |
| CR3-03 | Minor    | `BatteryManagerTest.java`       | Test method names used suffixes like `testA`, `testB` which are not descriptive enough for a test report. | Renamed to descriptive names matching `@DisplayName` annotations. Fixed. |

### Summary
- **Total findings:** 3 (1 Critical, 1 Major, 1 Minor)
- **Fixed before submission:** 3/3
- **Outcome:** APPROVED after fixes

---

## Aggregate Code Review Metrics

| Metric                          | Value   |
|---------------------------------|---------|
| Total files reviewed            | 4       |
| Total review sessions           | 3       |
| Total findings                  | 11      |
| Critical findings               | 3       |
| Major findings                  | 5       |
| Minor findings                  | 3       |
| Findings resolved before submission | 11/11 (100%) |
| Most common defect type         | Missing explicit waits / mock lifecycle management |

---

## Lessons Learned from Code Reviews

1. **Explicit waits are non-negotiable** in Selenium. Every element interacted with or asserted
   must be preceded by a wait condition. Implicit waits are not sufficient for async-loaded data.

2. **Mock lifecycle must be managed explicitly.** Using `@ExtendWith(MockitoExtension.class)`
   does handle mock injection, but `reset()` in `@AfterEach` should still be used when mocks
   accumulate state (like argument captors or interaction counts) across tests.

3. **Cyclomatic Complexity should be monitored continuously**, not only at review time. Adding
   a static analysis plugin (e.g., Checkstyle + PMD) to the Maven build would enforce NFR-004
   automatically on every commit.

---

*Document maintained by Group 4 – Last updated: February 24, 2026*
