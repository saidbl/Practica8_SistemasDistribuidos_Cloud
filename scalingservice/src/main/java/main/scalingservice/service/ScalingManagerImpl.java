package main.scalingservice.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScalingManagerImpl {

    private static final int MAX_VM = 5;
    private static final int MAX_CONTAINER = 8;
    private static final int MAX_DATABASE = 3;

    private static final long COOLDOWN = 5000;

    private final Map<String, List<String>> cluster = new ConcurrentHashMap<>();
    private final Map<String, Long> lastScale = new ConcurrentHashMap<>();

    public ScalingManagerImpl() {
        cluster.put("VM", new ArrayList<>());
        cluster.put("CONTAINER", new ArrayList<>());
        cluster.put("DATABASE", new ArrayList<>());
    }

    public void evaluate(String type, String state) {

        long now = System.currentTimeMillis();
        long last = lastScale.getOrDefault(type, 0L);

        if (now - last < COOLDOWN) {
            return;
        }

        if ("CRITICAL".equals(state)) {
            scaleUp(type, 1);
            lastScale.put(type, now);
        }

        if ("LOW".equals(state)) {
            scaleDown(type, 1);
            lastScale.put(type, now);
        }
    }

    private void scaleUp(String type, int count) {

        List<String> list = cluster.get(type);

        int max = switch (type) {
            case "VM" -> MAX_VM;
            case "CONTAINER" -> MAX_CONTAINER;
            case "DATABASE" -> MAX_DATABASE;
            default -> 5;
        };

        if (list.size() >= max) {
            System.out.println("[SCALE-UP BLOCKED] max reached for " + type);
            return;
        }

        for (int i = 0; i < count; i++) {

            if (list.size() >= max) break;

            String replica = type + "-AUTO-" + UUID.randomUUID().toString().substring(0,4);

            list.add(replica);

            System.out.println("[SCALE-UP] created " + replica +
                    " | total(" + type + ")=" + list.size());
        }
    }

    private void scaleDown(String type, int count) {

        List<String> list = cluster.get(type);

        for (int i = 0; i < count; i++) {

            if (list.size() <= 1) return;

            String removed = list.remove(list.size() - 1);

            System.out.println("[SCALE-DOWN] removed " + removed +
                    " | total(" + type + ")=" + list.size());
        }
    }

    public Map<String, List<String>> getCluster() {
        return cluster;
    }
}