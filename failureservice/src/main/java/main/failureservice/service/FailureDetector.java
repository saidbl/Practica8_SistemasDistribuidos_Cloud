package main.failureservice.service;

import main.failureservice.client.RegistryClient;
import main.failureservice.model.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class FailureDetector {

    @Autowired
    private RegistryClient registryClient;

    @Scheduled(fixedDelay = 5000, initialDelay = 30000)
    public void checkNodes() {
        try {
            var nodes = registryClient.getNodes();

            if (nodes == null || nodes.isEmpty()) {
                System.out.println("[FAILURE] No nodes registered yet");
                return;
            }

            long now = System.currentTimeMillis();

            for (NodeInfo node : nodes) {
                long diff = now - node.getLastHeartbeat();

                if (diff > 10000) {
                    System.out.println("[FAILURE DETECTED] node=" + node.getNodeId()
                            + " type=" + node.getType()
                            + " lastHeartbeat=" + diff + "ms");
                }
            }

        } catch (Exception e) {
            System.out.println("[FAILURE] Registry not ready yet, retrying...");
        }
    }
}