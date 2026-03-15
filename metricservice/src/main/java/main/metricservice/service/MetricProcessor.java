package main.metricservice.service;

import main.metricservice.client.ScalingClient;
import main.metricservice.model.MetricReport;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MetricProcessor {

    private static final int WINDOW_SIZE = 5;

    private final Map<String, Deque<Integer>> windows = new ConcurrentHashMap<>();
    
    @Autowired
    private ScalingClient scalingClient;

    public void process(MetricReport report) {

        windows.putIfAbsent(report.getNodeId(), new ArrayDeque<>());

        Deque<Integer> window = windows.get(report.getNodeId());

        window.addLast(report.getValue());

        if (window.size() > WINDOW_SIZE) {
            window.removeFirst();
        }

        int avg = (int) window.stream().mapToInt(i -> i).average().orElse(0);

        String state = calculateState(report.getType(), avg);

        System.out.println(
                "[METRIC] " +
                report.getType() + " " +
                report.getNodeId() +
                " value=" + report.getValue() +
                " avg=" + avg +
                " state=" + state
        );

        if (!"NORMAL".equals(state)) {

            scalingClient.evaluate(report.getType(), state);

        }
    }

    private String calculateState(String type, int v) {

        return switch (type) {

            case "VM" ->
                    (v > 80) ? "CRITICAL" :
                    (v < 30) ? "LOW" : "NORMAL";

            case "CONTAINER" ->
                    (v > 70) ? "CRITICAL" :
                    (v < 20) ? "LOW" : "NORMAL";

            case "DATABASE" ->
                    (v > 100) ? "CRITICAL" : "NORMAL";

            default -> "NORMAL";
        };
    }
}
