package main.metricservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "scalingservice")
public interface ScalingClient {

    @PostMapping("/scaling/evaluate")
    String evaluate(@RequestParam String type,
                    @RequestParam String state);

}