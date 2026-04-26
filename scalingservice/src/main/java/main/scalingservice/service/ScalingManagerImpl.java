package main.scalingservice.service;

import main.scalingservice.model.ScalingEvent;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScalingManagerImpl {

    private static final int MIN_INSTANCES = 1;
    private static final int MAX_INSTANCES = 6;
    private static final int EVENT_LIMIT = 100;

    private final Map<String, Integer> cluster = new HashMap<>();
    private final Deque<ScalingEvent> events = new ArrayDeque<>();

    public synchronized void evaluate(String type, String state, int avg) {
        cluster.putIfAbsent(type, MIN_INSTANCES);

        int current = cluster.get(type);

        if ("CRITICAL".equals(state)) {
            if (current < MAX_INSTANCES) {
                current++;
                cluster.put(type, current);

                addEvent(type, "SCALE_UP", "Carga crítica detectada. Promedio=" + avg, current);

                System.out.println("[SCALE-UP] created " + type + "-AUTO | total(" + type + ")=" + current);
            } else {
                addEvent(type, "SCALE_UP_BLOCKED", "Máximo alcanzado. Promedio=" + avg, current);

                System.out.println("[SCALE-UP BLOCKED] max reached for " + type);
            }
        }

        if ("LOW".equals(state)) {
            if (current > MIN_INSTANCES) {
                current--;
                cluster.put(type, current);

                addEvent(type, "SCALE_DOWN", "Carga baja detectada. Promedio=" + avg, current);

                System.out.println("[SCALE-DOWN] removed " + type + "-AUTO | total(" + type + ")=" + current);
            } else {
                addEvent(type, "SCALE_DOWN_BLOCKED", "Mínimo alcanzado. Promedio=" + avg, current);

                System.out.println("[SCALE-DOWN BLOCKED] min reached for " + type);
            }
        }
    }

    public synchronized Map<String, Integer> getCluster() {
        return cluster;
    }

    public synchronized List<ScalingEvent> getEvents() {
        return new ArrayList<>(events);
    }

    private void addEvent(String type, String action, String reason, int total) {
        events.addLast(new ScalingEvent(
                type,
                action,
                reason,
                total,
                System.currentTimeMillis()
        ));

        if (events.size() > EVENT_LIMIT) {
            events.removeFirst();
        }
    }
}