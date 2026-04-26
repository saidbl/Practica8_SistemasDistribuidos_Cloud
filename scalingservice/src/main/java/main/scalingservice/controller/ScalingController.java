package main.scalingservice.controller;

import main.scalingservice.model.ScalingEvent;
import main.scalingservice.service.ScalingManagerImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scaling")
public class ScalingController {

    private final ScalingManagerImpl manager;

    public ScalingController(ScalingManagerImpl manager) {
        this.manager = manager;
    }

    @PostMapping("/evaluate")
    public String evaluate(
            @RequestParam String type,
            @RequestParam String state,
            @RequestParam int avg
    ) {
        manager.evaluate(type, state, avg);
        return "evaluated";
    }

    @GetMapping("/cluster")
    public Map<String, Integer> cluster() {
        return manager.getCluster();
    }

    @GetMapping("/events")
    public List<ScalingEvent> events() {
        return manager.getEvents();
    }
}