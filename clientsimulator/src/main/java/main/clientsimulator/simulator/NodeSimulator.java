package main.clientsimulator.simulator;

import main.clientsimulator.model.MetricReport;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Random;
import java.util.UUID;

@Component
public class NodeSimulator {

    private final RestTemplate rest = new RestTemplate();
    private final Random random = new Random();

    private final String gateway = "http://localhost:8080";

    @PostConstruct
    public void startSimulation() {

        int nodes = 10;

        for (int i = 0; i < nodes; i++) {

            new Thread(this::simulateNode).start();

        }

    }

    private void simulateNode() {

        String[] types = {"VM","CONTAINER","DATABASE"};

        String type = types[random.nextInt(types.length)];

        String nodeId = type + "-" + UUID.randomUUID().toString().substring(0,4);

        System.out.println("[NODE STARTED] " + nodeId);

        register(nodeId, type);

        while (true) {

            try {

                sendHeartbeat(nodeId);

                sendMetric(nodeId, type);

                Thread.sleep(3000);

            } catch (Exception e) {

                System.out.println("[NODE ERROR] " + nodeId);

            }

        }

    }

    private void register(String nodeId, String type) {

        String url = gateway + "/registry/register";

        var body = new java.util.HashMap<String,String>();

        body.put("nodeId", nodeId);
        body.put("type", type);
        body.put("role", "METRIC");

        rest.postForObject(url, body, String.class);

    }

    private void sendHeartbeat(String nodeId) {

        String url = gateway + "/registry/heartbeat";

        var body = new java.util.HashMap<String,String>();

        body.put("nodeId", nodeId);

        rest.postForObject(url, body, String.class);

    }

    private void sendMetric(String nodeId, String type) {

        int value = random.nextInt(120);

        MetricReport report = new MetricReport(nodeId, type, value);

        rest.postForObject(gateway + "/metrics/report", report, String.class);

        System.out.println("[METRIC] " + nodeId + " value=" + value);

    }

}