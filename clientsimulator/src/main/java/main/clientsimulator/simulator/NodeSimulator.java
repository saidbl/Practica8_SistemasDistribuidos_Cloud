package main.clientsimulator.simulator;

import main.clientsimulator.model.MetricReport;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class NodeSimulator {

    private final Random random = new Random();

    private final String gateway = "http://localhost:8080";
    
    @Autowired 
    private RestTemplate  rest;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    @PostConstruct
    public void startSimulation() {
        int nodes = 10;
        for (int i = 0; i < nodes; i++) {
            new Thread(this::simulateNode).start();
        }
    }

    private void simulateNode() {
        String[] types = {"VM","CONTAINER","DATABASE","API-GATEWAY","STORAGE-NODE"};
        String type = types[random.nextInt(types.length)];
        String nodeId = type + "-" + UUID.randomUUID().toString().substring(0,4);
        System.out.println("[NODE STARTED] " + nodeId);
        register(nodeId, type);
        scheduler.scheduleAtFixedRate(
        () -> sendHeartbeat(nodeId),
        0,
        2,
        TimeUnit.SECONDS
        );

        scheduler.scheduleAtFixedRate(
            () -> sendMetric(nodeId,type),
            0,
            3,
            TimeUnit.SECONDS
        );

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
        int baseLoad = switch(type){
            case "VM" -> random.nextInt(100);
            case "CONTAINER" -> random.nextInt(120);
            case "DATABASE" -> random.nextInt(150);
            case "API-GATEWAY" -> random.nextInt(300);
            case "STORAGE-NODE" -> random.nextInt(100);
            default -> random.nextInt(50);
        };
        long phase = System.currentTimeMillis() % 30000;
        if(phase < 15000){
            System.out.println("[LOAD SPIKE] type=" + type);
        }
        if(phase < 15000){
            baseLoad += switch(type){
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
    }

}