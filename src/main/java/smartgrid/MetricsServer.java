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

        // Set initial metric values
        batteryLevel.set(72.5);
        solarOutput.set(4.3);
        edgeCpuUsage.set(54.2);
        routingRequests.inc(150);
        routingErrors.inc(1);

        // Seed initial latency observations
        double[] simulatedLatencies = {
                0.12, 0.08, 0.15, 0.19, 0.11, 0.09, 0.18, 0.13, 0.21, 0.10,
                0.14, 0.16, 0.07, 0.20, 0.12, 0.09, 0.17, 0.13, 0.11, 0.18
        };
        for (double latency : simulatedLatencies) {
            routingDuration.observe(latency);
        }

        // Use PORT env variable from Render, fallback to 8080 for local runs
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 8080;

        HTTPServer server = new HTTPServer(port);
        System.out.println("Metrics server running on port " + port);
        System.out.println("Metrics available at http://localhost:" + port + "/metrics");

        // Background thread: simulate realistic metric fluctuations every 15 seconds
        // so Grafana dashboard shows live movement during demos
        java.util.Random rand = new java.util.Random();
        Thread simulator = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(15_000);

                    // Battery drifts slowly (simulates charge/discharge cycles)
                    double battery = batteryLevel.get() + (rand.nextGaussian() * 0.5);
                    batteryLevel.set(Math.max(15, Math.min(95, battery)));

                    // Solar output varies (simulates cloud cover changes)
                    double solar = 4.0 + rand.nextGaussian() * 1.2;
                    solarOutput.set(Math.max(0.5, Math.min(9.0, solar)));

                    // CPU fluctuates under varying routing load
                    double cpu = 50 + rand.nextGaussian() * 8;
                    edgeCpuUsage.set(Math.max(20, Math.min(85, cpu)));

                    // Routing requests tick up (counters only increase)
                    routingRequests.inc(rand.nextInt(5) + 1);

                    // Occasional error (~5% chance per cycle)
                    if (rand.nextInt(20) == 0) {
                        routingErrors.inc();
                    }

                    // Observe a new latency sample (stays well under 500ms SLA)
                    double latency = 0.10 + rand.nextGaussian() * 0.05;
                    routingDuration.observe(Math.max(0.02, Math.min(0.45, latency)));

                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "metrics-simulator");
        simulator.setDaemon(true);
        simulator.start();

        // Keep main thread alive
        Thread.currentThread().join();
    }
}
