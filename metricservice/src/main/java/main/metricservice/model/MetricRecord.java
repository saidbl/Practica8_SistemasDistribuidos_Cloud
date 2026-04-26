package main.metricservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricRecord {
    private String nodeId;
    private String type;
    private int value;
    private String state;
    private long timestamp;
}