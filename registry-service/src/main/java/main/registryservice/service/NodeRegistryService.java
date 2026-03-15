package main.registryservice.service;

import main.registryservice.dto.RegisterRequest;
import main.registryservice.model.NodeInfo;

import java.util.Collection;

public interface NodeRegistryService {

    public NodeInfo registerNode(RegisterRequest request);

    public NodeInfo heartbeat(String nodeId);

    public NodeInfo updateStatus(String nodeId, String status);

    public NodeInfo getNode(String nodeId);

    public Collection<NodeInfo> getAllNodes();
}