package main.metricservice.service;

import main.metricservice.client.ScalingClient;
import main.metricservice.model.MetricRecord;
import main.metricservice.model.MetricReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MetricProcessor {

    private static final int WINDOW_SIZE = 5;
    private static final int HISTORY_LIMIT = 100;

    private final Map<String, Deque<Integer>> windows = new ConcurrentHashMap<>();
    private final Deque<MetricRecord> history = new ArrayDeque<>();

    @Autowired
    private ScalingClient scalingClient;

    public synchronized void process(MetricReport report) {
        windows.putIfAbsent(report.getNodeId(), new ArrayDeque<>());

        Deque<Integer> window = windows.get(report.getNodeId());
        window.addLast(report.getValue());

        if (window.size() > WINDOW_SIZE) {
            window.removeFirst();
        }

        int avg = (int) window.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(report.getValue());

        String state = calculateState(report.getType(), avg);

        MetricRecord record = new MetricRecord(
                report.getNodeId(),
                report.getType(),
                report.getValue(),
                state,
                System.currentTimeMillis()
        );

        history.addLast(record);

        if (history.size() > HISTORY_LIMIT) {
            history.removeFirst();
        }

        System.out.println("[METRIC] " +
                report.getType() + " " +
                report.getNodeId() +
                " value=" + report.getValue() +
                " avg=" + avg +
                " state=" + state);

        if ("CRITICAL".equals(state) || "LOW".equals(state)) {
            scalingClient.evaluate(report.getType(), state, avg);
        }
    }

    public synchronized List<MetricRecord> getHistory() {
        return new ArrayList<>(history);
    }

    private String calculateState(String type, int value) {
    String normalizedType = type.toUpperCase().replace("_", "-");

    return switch (normalizedType) {
        case "VM" -> value > 80 ? "CRITICAL" : value < 30 ? "LOW" : "NORMAL";
        case "CONTAINER" -> value > 70 ? "CRITICAL" : value < 20 ? "LOW" : "NORMAL";
        case "DATABASE" -> value > 100 ? "CRITICAL" : value < 40 ? "LOW" : "NORMAL";
        case "API-GATEWAY" -> value > 200 ? "CRITICAL" : value < 80 ? "LOW" : "NORMAL";
        case "STORAGE-NODE", "STORAGE" -> value > 90 ? "CRITICAL" : value < 25 ? "LOW" : "NORMAL";
        default -> "NORMAL";
    };
}
}