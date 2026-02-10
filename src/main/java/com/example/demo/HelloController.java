package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @Value("${app.message:Hello from Spring Boot on K8s!}")
    private String appMessage;

    @GetMapping("/")
    public Map<String, String> home() throws UnknownHostException {
        Map<String, String> response = new HashMap<>();
        response.put("message", appMessage);
        response.put("hostname", InetAddress.getLocalHost().getHostName());
        response.put("timestamp", LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        ));
        response.put("version", "1.0.0");
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "spring-boot-k8s-demo");
        return response;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        Map<String, String> response = new HashMap<>();
        response.put("app", "Spring Boot K8s Demo");
        response.put("version", "1.0.0");
        response.put("java", System.getProperty("java.version"));
        response.put("os", System.getProperty("os.name"));
        return response;
    }
}
