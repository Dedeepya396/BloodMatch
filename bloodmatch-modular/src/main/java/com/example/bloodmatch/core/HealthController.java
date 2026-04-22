package com.example.bloodmatch.core;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final ServiceHealthRegistry registry;

    public HealthController(ServiceHealthRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public Map<String, String> getHealth() {
        return registry.getStatus();
    }
}