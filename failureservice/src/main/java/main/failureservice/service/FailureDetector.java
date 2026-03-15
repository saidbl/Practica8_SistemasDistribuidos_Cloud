package main.failureservice.service;

import main.failureservice.client.RegistryClient;
import main.failureservice.model.NodeInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class FailureDetector {

    private static final long HEARTBEAT_TIMEOUT = 8000;

    @Autowired
    private RegistryClient registryClient;

    @Scheduled(fixedDelay = 4000)
    public void checkNodes() {
        var nodes = registryClient.getNodes();
        long now = System.currentTimeMillis();
        for (NodeInfo node : nodes) {
            long diff = now - node.getLastHeartbeat();
            String newStatus = "ALIVE";
            if (diff > HEARTBEAT_TIMEOUT * 2) {
                newStatus = "DEAD";
            }
            else if (diff > HEARTBEAT_TIMEOUT) {
                newStatus = "SUSPECT";
            }
            if (!newStatus.equals(node.getStatus())) {
                var body = new HashMap<String,String>();
                body.put("nodeId", node.getNodeId());
                body.put("status", newStatus);
                registryClient.updateStatus(body);
                System.out.println(
                        "[FAILURE] node=" +
                        node.getNodeId() +
                        " status=" +
                        newStatus
                );
            }
        }
    }
}