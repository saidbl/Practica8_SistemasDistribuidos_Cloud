package main.clientsimulator.simulator;

import main.clientsimulator.model.MetricReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class NodeSimulator {

    private final Random random = new Random();

    private final String gateway =
            System.getenv().getOrDefault("GATEWAY_URL", "http://localhost:8080");

    @Autowired
    private RestTemplate rest;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    @PostConstruct
    public void startSimulation() {
        scheduler.schedule(() -> {
            String[] types = {"VM", "CONTAINER", "DATABASE", "API-GATEWAY", "STORAGE-NODE"};

            for (String type : types) {
                for (int i = 0; i < 2; i++) {
                    String selectedType = type;
                    new Thread(() -> simulateNode(selectedType)).start();
                }
            }
        }, 30, TimeUnit.SECONDS);
    }

    private void simulateNode(String type) {
        String nodeId = type + "-" + UUID.randomUUID().toString().substring(0, 4);

        System.out.println("[NODE STARTED] " + nodeId);

        register(nodeId, type);

        scheduler.scheduleAtFixedRate(
                () -> sendHeartbeat(nodeId),
                0,
                2,
                TimeUnit.SECONDS
        );

        scheduler.scheduleAtFixedRate(
                () -> sendMetric(nodeId, type),
                0,
                3,
                TimeUnit.SECONDS
        );
    }

    private void register(String nodeId, String type) {
        try {
            String url = gateway + "/registry/register";

            Map<String, String> body = new HashMap<>();
            body.put("nodeId", nodeId);
            body.put("type", type);
            body.put("role", "METRIC");

            rest.postForObject(url, body, String.class);

            System.out.println("[REGISTER] " + nodeId + " type=" + type);
        } catch (Exception e) {
            System.out.println("[REGISTER ERROR] gateway not ready for node=" + nodeId);
        }
    }

    private void sendHeartbeat(String nodeId) {
        try {
            String url = gateway + "/registry/heartbeat";

            Map<String, String> body = new HashMap<>();
            body.put("nodeId", nodeId);

            rest.postForObject(url, body, String.class);
        } catch (Exception e) {
            System.out.println("[HEARTBEAT ERROR] node=" + nodeId);
        }
    }

    private void sendMetric(String nodeId, String type) {
        try {
            String normalizedType = type.toUpperCase().replace("_", "-");

            int baseLoad = switch (normalizedType) {
                case "VM" -> random.nextInt(100);
                case "CONTAINER" -> random.nextInt(120);
                case "DATABASE" -> random.nextInt(150);
                case "API-GATEWAY" -> random.nextInt(300);
                case "STORAGE-NODE" -> random.nextInt(100);
                default -> random.nextInt(50);
            };

            long phase = System.currentTimeMillis() % 30000;

            if (phase < 15000) {
                System.out.println("[LOAD SPIKE] type=" + type);

                baseLoad += switch (normalizedType) {
                    case "VM" -> 40;
                    case "CONTAINER" -> 50;
                    case "DATABASE" -> 60;
                    case "API-GATEWAY" -> 120;
                    case "STORAGE-NODE" -> 30;
                    default -> 20;
                };
            }

            int value = baseLoad;

            MetricReport report = new MetricReport(nodeId, type, value);

            rest.postForObject(gateway + "/metrics/report", report, String.class);

            System.out.println("[METRIC] " + nodeId + " type=" + type + " value=" + value);
        } catch (Exception e) {
            System.out.println("[METRIC ERROR] node=" + nodeId + " type=" + type);
        }
    }
}