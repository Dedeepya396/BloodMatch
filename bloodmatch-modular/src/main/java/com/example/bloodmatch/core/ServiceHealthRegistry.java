package com.example.bloodmatch.core;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceHealthRegistry {

    private final Map<String, String> status = new ConcurrentHashMap<>();

    public void markUp(String service) {
        status.put(service, "UP");
    }

    public void markDown(String service) {
        status.put(service, "DOWN");
    }

    public Map<String, String> getStatus() {
        return status;
    }
}