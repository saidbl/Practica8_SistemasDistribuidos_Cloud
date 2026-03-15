package main.registryservice.service;

import main.registryservice.model.NodeInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import main.registryservice.dto.RegisterRequest;

@Service
public class NodeRegistryServiceImpl implements NodeRegistryService{

    private final Map<String, NodeInfo> nodes = new ConcurrentHashMap<>();

    @Override
    public NodeInfo registerNode(RegisterRequest request) {
        NodeInfo node = new NodeInfo(
                request.getNodeId(),
                request.getType(),
                request.getRole(),
                System.currentTimeMillis(),
                "ALIVE"
        );

        nodes.put(request.getNodeId(), node);

        System.out.println("[REGISTER] node=" + request.getNodeId()
                + " type=" + request.getType()
                + " role=" + request.getRole());

        return node;
    }

    @Override
    public NodeInfo heartbeat(String nodeId) {
        NodeInfo node = nodes.get(nodeId);

        if (node != null) {
            node.setLastHeartbeat(System.currentTimeMillis());
            node.setStatus("ALIVE");
            System.out.println("[HEARTBEAT] node=" + nodeId);
        }

        return node;
    }

    @Override
    public NodeInfo updateStatus(String nodeId, String status) {
        NodeInfo node = nodes.get(nodeId);

        if (node != null) {
            node.setStatus(status);
            System.out.println("[STATUS] node=" + nodeId + " status=" + status);
        }

        return node;
    }

    @Override
    public NodeInfo getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public Collection<NodeInfo> getAllNodes() {
        return nodes.values();
    }

}

