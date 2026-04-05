package smartgrid;

import io.prometheus.client.*;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

public class MetricsServer {

    // RED Metrics
    public static final Counter routingRequests = Counter.build()
        .name("smartgrid_routing_requests_total")
        .help("Total routing decisions made").register();

    public static final Counter routingErrors = Counter.build()
        .name("smartgrid_routing_errors_total")
        .help("Total routing errors").register();

    public static final Histogram routingDuration = Histogram.build()
        .name("smartgrid_routing_duration_seconds")
        .help("Routing decision latency")
        .buckets(0.05, 0.1, 0.2, 0.5, 1.0).register();

    // Saturation Metrics
    public static final Gauge batteryLevel = Gauge.build()
        .name("smartgrid_battery_level_percent")
        .help("Battery charge level 0-100").register();

    public static final Gauge solarOutput = Gauge.build()
        .name("smartgrid_solar_output_kwh")
        .help("Current solar output in kWh").register();

    public static final Gauge edgeCpuUsage = Gauge.build()
        .name("smartgrid_edge_cpu_percent")
        .help("Edge node CPU usage percent").register();

    public static void main(String[] args) throws Exception {
    DefaultExports.initialize(); // JVM metrics

    // Simulate some metric values for the dashboard
    batteryLevel.set(72.5);
    solarOutput.set(4.3);
    edgeCpuUsage.set(54.2);
    routingRequests.inc(150);
    routingErrors.inc(1);

    // Simulate routing latency observations
    double[] simulatedLatencies = {0.12, 0.08, 0.15, 0.19, 0.11, 0.09, 0.18, 0.13, 0.21, 0.10,
                                    0.14, 0.16, 0.07, 0.20, 0.12, 0.09, 0.17, 0.13, 0.11, 0.18};
    for (double latency : simulatedLatencies) {
        routingDuration.observe(latency);
    }

    HTTPServer server = new HTTPServer(8080);
    System.out.println("Metrics server running at http://localhost:8080/metrics");
    System.out.println("Press Ctrl+C to stop.");

    // Keep running
    Thread.currentThread().join();
}
}