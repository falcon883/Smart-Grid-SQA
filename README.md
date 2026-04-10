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

The test suite covers **Phases 2, 3, and 4** of the SQA project:
- Unit tests (JUnit 5 + Mockito)
- Integration tests
- Selenium UI automation (Page Object Model)
- Cross-browser compatibility tests
- Prometheus metrics instrumentation (Phase 3)
- Live Grafana Cloud dashboard (Phase 3)
- Code review documentation
- Test results and evidence

**Live Metrics Endpoint:** https://smart-grid-sqa.onrender.com/metrics  
**Live Dashboard:** https://smartgridneighborhood.grafana.net/public-dashboards/868c7b9f4bef45df95eddd6c2705e857  
**CI Pipeline:** https://github.com/falcon883/Smart-Grid-SQA/actions

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
│   │   ├── WeatherAPI.java              # Weather API interface (mocked in tests)
│   │   └── MetricsServer.java           # Prometheus metrics endpoint (Phase 3)
│   │
│   └── test/java/
│       ├── unit/
│       │   ├── BatteryManagerTest.java  # UT-001 – battery logic unit tests
│       │   ├── DemandPredictorTest.java # UT-002 – AI prediction unit tests
│       │   ├── AlertServiceTest.java    # UT-003 – alert service unit tests
│       │   └── AuditLoggerTest.java     # UT-004 – audit logger unit tests
│       │
│       ├── integration/
│       │   ├── SensorToRoutingIntegrationTest.java   # IT-001 – sensor to routing
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
├── test-evidence/                       # Surefire test output files (Phase 3 evidence)
│   ├── TEST-unit.AlertServiceTest.xml
│   ├── TEST-unit.AuditLoggerTest.xml
│   ├── TEST-unit.BatteryManagerTest.xml
│   ├── TEST-unit.DemandPredictorTest.xml
│   ├── TEST-integration.DashboardDataFlowIntegrationTest.xml
│   ├── TEST-integration.SensorToRoutingIntegrationTest.xml
│   └── *.txt                            # Human-readable pass/fail summaries
│
├── test-results/
│   └── TestResultsReport.md            # Pass/Fail report for all test suites
│
├── docs/
│   ├── CodeReviewNotes.md              # Formal code review findings and resolutions
│   └── grafana-dashboard.json          # Grafana dashboard export (Phase 3)
│
├── webapp/                             # Energy Monitor dashboard (Selenium target)
│   ├── dashboard.html
│   ├── login.html
│   ├── alerts.html
│   ├── settings.html
│   └── serve.py                        # Local dev server (python serve.py)
│
├── Dockerfile                          # Docker build for Render deployment
├── pom.xml                             # Maven build with JUnit 5, Mockito, Selenium, Prometheus
└── README.md                           # This file
```

---

## Prerequisites

| Tool          | Version  | Purpose                              |
|---------------|----------|--------------------------------------|
| Java JDK      | 17+      | Compile and run tests                |
| Maven         | 3.8+     | Build and dependency management      |
| Chrome        | Latest   | Selenium Chrome tests                |
| Firefox       | Latest   | Selenium Firefox tests               |
| Safari        | 17+      | Selenium Safari tests (macOS only)   |
| Docker        | Latest   | Container build for Render           |

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

### Run Selenium Tests (requires dashboard running at localhost:3000)
```bash
# Start the webapp first
cd webapp
python serve.py

# Then in a separate terminal
mvn test -P selenium-tests
```

### Run a specific test class
```bash
mvn test -Dtest=BatteryManagerTest
mvn test -Dtest=SensorToRoutingIntegrationTest
mvn test -Dtest=CrossBrowserTest
```

---

## Phase 3: Prometheus Metrics

The `MetricsServer` class exposes real-time metrics on a `/metrics` endpoint.

### Run locally
```bash
mvn compile exec:java -Dexec.mainClass="smartgrid.MetricsServer"
```

Open http://localhost:8080/metrics to verify.

### Deployed on Render (always on)
The metrics server is deployed as a Docker container on Render and is publicly accessible:

```
https://smart-grid-sqa.onrender.com/metrics
```

No local setup needed — the endpoint is always available.

### Metrics exposed

| Metric | Type | Description |
|--------|------|-------------|
| `smartgrid_routing_requests_total` | Counter | Total routing decisions made |
| `smartgrid_routing_errors_total` | Counter | Total routing errors |
| `smartgrid_routing_duration_seconds` | Histogram | Routing decision latency |
| `smartgrid_battery_level_percent` | Gauge | Battery charge level (0-100%) |
| `smartgrid_solar_output_kwh` | Gauge | Solar panel output in kWh |
| `smartgrid_edge_cpu_percent` | Gauge | Edge node CPU usage |

JVM metrics (heap, threads, GC) are also exposed automatically via `simpleclient_hotspot`.

---

## Phase 3: Grafana Cloud Dashboard

Metrics are scraped from the public Render endpoint by Grafana Alloy and forwarded to Grafana Cloud. The pipeline is managed remotely via Grafana Fleet Management — no local config changes needed.

**Live dashboard:** https://smartgridneighborhood.grafana.net/public-dashboards/868c7b9f4bef45df95eddd6c2705e857

### Architecture
```
Render (always on)                  Grafana Cloud
smart-grid-sqa.onrender.com/metrics  <-- Alloy scrapes every 60s
        |                                     |
        |                            Prometheus remote write
        |                                     |
        +-------------------------------------> Dashboard panels
```

### Dashboard panels

| Panel | Query | Visualization |
|-------|-------|---------------|
| P95 Routing Latency (ms) | `histogram_quantile(0.95, ...) * 1000` | Gauge |
| JVM Heap Memory (MB) | `jvm_memory_bytes_used{area="heap"} / 1024 / 1024` | Time series |
| Error Rate % | `routing_errors / routing_requests * 100` | Stat |
| Total Routing Errors | `smartgrid_routing_errors_total` | Stat |
| Edge CPU % | `smartgrid_edge_cpu_percent` | Gauge |
| Total Routing Requests | `smartgrid_routing_requests_total` | Stat |
| Battery Level % | `smartgrid_battery_level_percent` | Gauge |
| Solar Output (kWh) | `smartgrid_solar_output_kwh` | Stat |

The dashboard JSON export is in `docs/grafana-dashboard.json`.

---

## Render Deployment

The metrics server is containerised using Docker and deployed on Render's free tier.

### Docker build
```bash
docker build -t smart-grid-sqa .
docker run -p 8080:8080 smart-grid-sqa
```

### Render settings
| Field | Value |
|-------|-------|
| Runtime | Docker |
| Branch | main |
| Health Check Path | /metrics |
| Auto-Deploy | On Commit |
| Instance Type | Free |

Every push to `main` triggers an automatic redeploy on Render.

---

## Key Design Decisions

### Why Mockito?
`SolarSensorAPI` and `WeatherAPI` are interfaces representing physical IoT hardware and
external cloud services. Mocking them in unit tests allows the team to:
- Test in isolation without hardware
- Simulate edge cases (sensor failure, extreme weather) repeatably
- Run tests in CI without network access

### Why Page Object Model?
POM separates page structure (locators, interactions) from test logic:
- Single place to update locators when UI changes
- Tests read like plain English
- Reduces code duplication across test classes

### Why Explicit Waits?
The Energy Monitor Dashboard loads data asynchronously from IoT sensor APIs.
`Thread.sleep()` is non-deterministic. Explicit waits with `WebDriverWait` and
`ExpectedConditions` wait exactly as long as needed.

### Why Render + Docker?
Deploying the metrics server to Render keeps the Grafana dashboard live 24/7
without requiring a local machine to run. The Docker container ensures consistent
behaviour between local development and production.

---

## Test Coverage by Requirement

| Requirement | Description | Test Coverage |
|-------------|-------------|---------------|
| FR-001 | Sensor data at 5 second interval | TC-001, TC-002 |
| FR-002 | AI prediction MAE < 10% | TC-003, UT-002 |
| FR-003 | Routing decision within 500ms | TC-005, IT-001 |
| FR-004 | Dashboard 3 real-time metrics | TC-006, DashboardTest |
| FR-005 | Low battery alert below 20% | TC-008, UT-003 |
| FR-007 | Chrome, Firefox, Safari | CrossBrowserTest |
| FR-008 | Audit log with timestamps | UT-004, IT tests |
| NFR-001 | 99.5% uptime | TC-015, TC-016 |
| NFR-002 | 500 concurrent edge nodes | TC-017 |
| NFR-003 | TLS 1.3 encryption | TC-018 |
| NFR-004 | Cyclomatic Complexity at most 15 | Code Review CR2-01 |
| NFR-005 | Responsive 320px to 1920px | CrossBrowserTest |

---

## Team Contributions

| Team Member | Contributions |
|-------------|---------------|
| Durvank Deorukhkar | Integration tests, DashboardDataFlowIntegrationTest, RTM, RED Metrics, Cost of Quality |
| Percival Tapera | DemandPredictorTest, AlertsPage, SettingsPage, CrossBrowserTest, Go/No-Go checklist |
| Harsha Vardhan Varma Kopanathi | BatteryManagerTest, AuditLoggerTest, LoginPage, DashboardPage, MetricsServer, Grafana dashboard, Render deployment |

---

*Last updated: April 2026*
