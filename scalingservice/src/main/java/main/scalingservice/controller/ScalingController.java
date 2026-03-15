package main.scalingservice.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import main.scalingservice.service.ScalingManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/scaling")
public class ScalingController {
    @Autowired
    private ScalingManagerImpl manager;

    @PostMapping("/evaluate")
    public String evaluate(@RequestParam String type,
                           @RequestParam String state) {

        manager.evaluate(type, state);

        return "evaluated";
    }

    @GetMapping("/cluster")
    public Map<String, ?> cluster() {
        return manager.getCluster();
    }
}