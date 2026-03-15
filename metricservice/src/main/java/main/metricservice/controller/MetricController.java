package main.metricservice.controller;

import main.metricservice.model.MetricReport;
import main.metricservice.service.MetricProcessor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metrics")
public class MetricController {

    private final MetricProcessor processor;

    public MetricController(MetricProcessor processor) {
        this.processor = processor;
    }

    @PostMapping("/report")
    public String report(@RequestBody MetricReport report) {
        processor.process(report);
        return "metric processed";
    }
}