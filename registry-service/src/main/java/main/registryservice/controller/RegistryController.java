package main.registryservice.controller;

import main.registryservice.dto.HearthBeatRequest;
import main.registryservice.dto.RegisterRequest;
import main.registryservice.dto.UpdateStatusRequest;
import main.registryservice.model.NodeInfo;
import main.registryservice.service.NodeRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/registry")
public class RegistryController {

    @Autowired 
    private NodeRegistryService service;

    @PostMapping("/register")
    public ResponseEntity<NodeInfo> register(@RequestBody RegisterRequest request) {
        NodeInfo node = service.registerNode(request);
        return ResponseEntity.ok(node);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<String> heartbeat(@RequestBody HearthBeatRequest request) {
        NodeInfo node = service.heartbeat(request.getNodeId());
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("heartbeat received");
    }

    @PostMapping("/status")
    public ResponseEntity<String> updateStatus(@RequestBody UpdateStatusRequest request) {
        NodeInfo node = service.updateStatus(request.getNodeId(), request.getStatus());
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("status updated");
    }

    @GetMapping("/nodes")
    public ResponseEntity<Collection<NodeInfo>> getAllNodes() {
        return ResponseEntity.ok(service.getAllNodes());
    }

    @GetMapping("/nodes/{nodeId}")
    public ResponseEntity<NodeInfo> getNode(@PathVariable String nodeId) {
        NodeInfo node = service.getNode(nodeId);
        if (node == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(node);
    }
}