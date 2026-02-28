# Test Results Report
## Smart-Grid Neighborhood Optimizer – SQA Project
### MSCS3254 | Spring 2026 | Group 4

---

## Summary

| Category              | Total | Passed | Failed | Skipped | Pass Rate |
|-----------------------|-------|--------|--------|---------|-----------|
| Unit Tests            | 18    | 18     | 0      | 0       | **100%**  |
| Integration Tests     | 12    | 12     | 0      | 0       | **100%**  |
| Selenium (Chrome)     | 10    | 10     | 0      | 0       | **100%**  |
| Selenium (Firefox)    | 10    | 10     | 0      | 0       | **100%**  |
| Selenium (Safari)     | 10    | N/A    | N/A    | 10      | **N/A***  |
| **Total**             | **60**| **50** | **0**  | **10**  | **100%**  |

> *Safari tests are skipped in automated CI. They require manual execution on macOS with
> `safaridriver` enabled. See Cross-Browser Notes below.

---

## Unit Test Results

### BatteryManagerTest

| Test ID | Test Name                                                  | Status  | Duration |
|---------|------------------------------------------------------------|---------|----------|
| UT-001a | Solar fully covers demand – no discharge needed            | ✅ PASS | 12ms     |
| UT-001b | Solar partially covers demand – discharge shortfall        | ✅ PASS | 8ms      |
| UT-001c | Discharge capped at MAX_DISCHARGE_RATE                     | ✅ PASS | 6ms      |
| UT-001d | Battery at LOW threshold – alert fired, no discharge       | ✅ PASS | 9ms      |
| UT-001e | Negative demand throws IllegalArgumentException            | ✅ PASS | 5ms      |
| TC-001  | Valid demand values within MAX_DISCHARGE_RATE (parameterized, 5 cases) | ✅ PASS | 22ms |
| –       | Charge capped at 100%                                      | ✅ PASS | 5ms      |
| TC-008  | Discharge below threshold triggers low battery alert       | ✅ PASS | 8ms      |
| –       | Discharge floors at 0%                                     | ✅ PASS | 5ms      |

### DemandPredictorTest

| Test ID | Test Name                                                  | Status  | Duration |
|---------|------------------------------------------------------------|---------|----------|
| TC-003  | Prediction with normal weather data returns valid result   | ✅ PASS | 15ms     |
| TC-003b | MAE verification – within 10% threshold (FR-002)           | ✅ PASS | 10ms     |
| TC-004  | Extreme high temperature clamped to MAX                    | ✅ PASS | 7ms      |
| TC-004b | Storm conditions produce bounded prediction                | ✅ PASS | 7ms      |
| TC-007  | Weather API unavailable – falls back to baseline demand    | ✅ PASS | 6ms      |
| –       | MAE calculation returns correct percentage                 | ✅ PASS | 4ms      |
| –       | MAE throws exception when actual is zero                   | ✅ PASS | 5ms      |

### AlertServiceTest

| Test ID | Test Name                                                  | Status  | Duration |
|---------|------------------------------------------------------------|---------|----------|
| UT-003a | Low battery alert increments alert count                   | ✅ PASS | 5ms      |
| UT-003b | Multiple different alerts are all counted                  | ✅ PASS | 4ms      |
| UT-003c | Alert count starts at zero                                 | ✅ PASS | 3ms      |
| UT-003d | Alert at exact threshold (20%) does not throw              | ✅ PASS | 4ms      |
| –       | Sensor failure alert with valid sensor ID                  | ✅ PASS | 3ms      |

### AuditLoggerTest

| Test ID | Test Name                                                  | Status  | Duration |
|---------|------------------------------------------------------------|---------|----------|
| UT-004a | Routing decision is logged and retrievable                 | ✅ PASS | 6ms      |
| UT-004b | Log entry contains correct routing decision                | ✅ PASS | 4ms      |
| UT-004c | Log entry contains correct demand and solar values         | ✅ PASS | 4ms      |
| UT-004d | Log entry timestamp is ISO 8601 format (FR-008)            | ✅ PASS | 5ms      |
| –       | Multiple log entries stored in order                       | ✅ PASS | 5ms      |
| –       | clearLog() resets entry count to zero                      | ✅ PASS | 3ms      |
| –       | getLog() returns unmodifiable list                         | ✅ PASS | 4ms      |

---

## Integration Test Results

### SensorToRoutingIntegrationTest

| Test ID | Test Name                                                  | Status  | Duration |
|---------|------------------------------------------------------------|---------|----------|
| IT-001a | Solar surplus → EXPORT_TO_GRID decision                    | ✅ PASS | 18ms     |
| IT-001b | Solar matches demand → SOLAR_ONLY                          | ✅ PASS | 12ms     |
| IT-001c | Solar deficit with healthy battery → SOLAR_AND_BATTERY     | ✅ PASS | 14ms     |
| IT-001d | Solar deficit with low battery → SOLAR_AND_GRID            | ✅ PASS | 11ms     |
| TC-005  | Routing decision within 500ms SLA (FR-003)                 | ✅ PASS | 8ms      |
| IT-001f | Sensor unavailable with healthy battery → BATTERY_ONLY     | ✅ PASS | 10ms     |
| IT-001g | Sensor unavailable with low battery → GRID_ONLY            | ✅ PASS | 9ms      |
| –       | Routing decision creates audit log entry (FR-008)          | ✅ PASS | 12ms     |

### DashboardDataFlowIntegrationTest

| Test ID | Test Name                                                  | Status  | Duration |
|---------|------------------------------------------------------------|---------|----------|
| IT-002  | AI prediction feeds routing engine                         | ✅ PASS | 25ms     |
| IT-003  | All dashboard data points available after pipeline run     | ✅ PASS | 20ms     |
| IT-004  | System routing continues during simulated OTA update window| ✅ PASS | 15ms     |
| TC-008  | Low battery alert propagation through full pipeline        | ✅ PASS | 18ms     |

---

## Selenium Test Results

### Chrome (TC-011)

| Test ID  | Test Name                                              | Status  | Duration |
|----------|--------------------------------------------------------|---------|----------|
| TC-011a  | Valid credentials redirect to dashboard                | ✅ PASS | 2.1s     |
| TC-011b  | Dashboard title correct after login                    | ✅ PASS | 1.8s     |
| TC-011c  | Invalid password shows error message                   | ✅ PASS | 1.5s     |
| TC-011d  | Non-existent username shows error                      | ✅ PASS | 1.6s     |
| TC-011e  | Empty fields show validation error                     | ✅ PASS | 1.2s     |
| TC-006   | All three real-time data widgets visible (FR-004)      | ✅ PASS | 2.3s     |
| TC-006b  | Solar output widget has non-empty value                | ✅ PASS | 2.0s     |
| TC-007   | Error banner when sensor connection lost               | ✅ PASS | 1.9s     |
| TC-008   | Low battery alert visible when battery < 20% (FR-005) | ✅ PASS | 2.1s     |
| NFR-005  | Dashboard renders at 320px mobile width                | ✅ PASS | 1.7s     |

### Firefox (TC-012)

| Test ID  | Test Name                                              | Status  | Duration |
|----------|--------------------------------------------------------|---------|----------|
| TC-012   | Dashboard critical path on Firefox (parameterized)     | ✅ PASS | 2.4s     |
| NFR-005  | Dashboard renders at 1920px desktop width (Firefox)    | ✅ PASS | 2.1s     |
| *(+8 tests mirroring Chrome suite)* | All passed | ✅ PASS | ~2.0s avg |

### Safari (TC-013)

| Test ID  | Test Name                                              | Status   | Notes |
|----------|--------------------------------------------------------|----------|-------|
| TC-013   | All critical path tests                                | ⏭ SKIP  | Requires macOS + safaridriver. Run manually. |

---

## Cross-Browser Notes

Safari tests are excluded from the automated Maven test run because:
1. Safari's `safaridriver` requires manual enablement in macOS Safari Develop menu.
2. Safari does not support headless mode, requiring a visible browser instance.
3. CI/CD pipelines typically run on Linux where Safari is unavailable.

**Manual Safari Test Protocol:**
1. On macOS: Enable Safari → Preferences → Develop → Allow Remote Automation
2. Run: `mvn test -P selenium-tests -Dbrowser=safari`
3. Observe test execution in Safari window

Safari manual test results (last run: Feb 24, 2026): **10/10 PASSED**

---

## Defect Log

No defects found during final test run. All pre-submission defects were identified
and resolved during code reviews (see `docs/CodeReviewNotes.md`).

| Defect ID | Found By   | Description                                        | Status   |
|-----------|------------|----------------------------------------------------|----------|
| DEF-001   | Review #1  | Missing explicit wait on dashboard data elements   | ✅ Fixed |
| DEF-002   | Review #2  | Cyclomatic Complexity of RouteOptimizer = 18       | ✅ Fixed |
| DEF-003   | Review #3  | Mock not reset between unit tests                  | ✅ Fixed |

---

## Requirements Coverage Summary

| Requirement | Description                          | Test(s)             | Status     |
|-------------|--------------------------------------|---------------------|------------|
| FR-001      | Sensor data interval ≤ 5 seconds     | TC-001, TC-002      | ✅ Covered |
| FR-002      | AI prediction MAE < 10%              | TC-003, TC-003b     | ✅ Covered |
| FR-003      | Routing decision within 500ms        | TC-005              | ✅ Covered |
| FR-004      | Dashboard shows 3 real-time metrics  | TC-006              | ✅ Covered |
| FR-005      | Low battery alert at < 20%           | TC-008              | ✅ Covered |
| FR-007      | Cross-browser compatibility          | TC-011/012/013      | ✅ Covered |
| FR-008      | Audit log with timestamps            | UT-004d, IT routing | ✅ Covered |
| NFR-003     | TLS 1.3 encryption                   | TC-018              | 🔲 Phase 3 |
| NFR-004     | Cyclomatic Complexity ≤ 15           | TC-019, CR2-01      | ✅ Covered |
| NFR-005     | Responsive 320px–1920px              | NFR-005 tests       | ✅ Covered |

---

*Generated by Group 4 – Last updated: February 26, 2026*
