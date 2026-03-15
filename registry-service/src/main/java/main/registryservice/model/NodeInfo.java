package main.registryservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeInfo {

    private String nodeId;
    private String type;
    private String role;
    private long lastHeartbeat;
    private String status;

}

