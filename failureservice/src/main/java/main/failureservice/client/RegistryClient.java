package main.failureservice.client;

import main.failureservice.model.NodeInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@FeignClient(name = "registryservice")
public interface RegistryClient {

    @GetMapping("/registry/nodes")
    Collection<NodeInfo> getNodes();

    @PostMapping("/registry/status")
    void updateStatus(@RequestBody java.util.Map<String,String> body);

}