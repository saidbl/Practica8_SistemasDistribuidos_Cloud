package main.scalingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScalingEvent {
    private String type;
    private String action;
    private String reason;
    private int total;
    private long timestamp;

    

}