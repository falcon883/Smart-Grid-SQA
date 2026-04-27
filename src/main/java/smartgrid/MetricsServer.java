package smartgrid;

import io.prometheus.client.*;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import java.util.Random;

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
        .buckets(0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4, 0.5, 1.0).register();

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

        // Set initial gauge values
        batteryLevel.set(72.5);
        solarOutput.set(4.3);
        edgeCpuUsage.set(54.2);

        // Background simulation thread — runs continuously
        Thread simulator = new Thread(() -> {
            Random random = new Random();
            while (true) {
                try {
                    // Simulate 1-3 routing requests every 5 seconds
                    int requests = 1 + random.nextInt(3);
                    routingRequests.inc(requests);

                    // ~0.5% error rate — stays well under the 1% Go/No-Go threshold
                    for (int i = 0; i < requests; i++) {
                        if (random.nextDouble() < 0.005) {
                            routingErrors.inc(1);
                        }
                    }

                    // Simulate routing latency between 80ms and 220ms
                    for (int i = 0; i < requests; i++) {
                        double latency = 0.08 + (random.nextDouble() * 0.14);
                        routingDuration.observe(latency);
                    }

                    // Vary gauges to simulate realistic sensor readings
                    batteryLevel.set(65.0 + (random.nextDouble() * 20.0));  // 65-85%
                    solarOutput.set(3.5 + (random.nextDouble() * 2.0));     // 3.5-5.5 kWh
                    edgeCpuUsage.set(45.0 + (random.nextDouble() * 22.0));  // 45-67%

                    Thread.sleep(5000); // tick every 5 seconds

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        simulator.setDaemon(true);
        simulator.start();

        // Use PORT env variable from Render, fallback to 8080 locally
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 8080;

        HTTPServer server = new HTTPServer(port);
        System.out.println("Metrics server running on port " + port);
        System.out.println("Metrics available at http://localhost:" + port + "/metrics");

        // Keep running
        Thread.currentThread().join();
    }
}