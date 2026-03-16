package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class HelloController {

    @Value("${app.message:Hello from Spring Boot on K8s!}")
    private String appMessage;
    
    private final AtomicLong visitCounter = new AtomicLong(0);

    @GetMapping("/")
    public Map<String, String> home() throws UnknownHostException {
        Map<String, String> response = new HashMap<>();
        response.put("message", appMessage);
        response.put("hostname", InetAddress.getLocalHost().getHostName());
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("visits", String.valueOf(visitCounter.incrementAndGet()));
        return response;
    }
    
    @GetMapping("/info")
    public Map<String, Object> getSystemInfo() throws UnknownHostException {
        Map<String, Object> info = new HashMap<>();
        
        // Application info
        info.put("app", "Spring Boot K8s Demo");
        info.put("version", "1.0.0");
        info.put("message", appMessage);
        
        // System info
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> systemInfo = new HashMap<>();
        systemInfo.put("hostname", InetAddress.getLocalHost().getHostName());
        systemInfo.put("ip", InetAddress.getLocalHost().getHostAddress());
        systemInfo.put("os", System.getProperty("os.name"));
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("totalMemory", formatBytes(runtime.totalMemory()));
        systemInfo.put("freeMemory", formatBytes(runtime.freeMemory()));
        systemInfo.put("processors", String.valueOf(runtime.availableProcessors()));
        info.put("system", systemInfo);
        
        // Stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", visitCounter.get());
        stats.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("stats", stats);
        
        return info;
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }
    
    @GetMapping("/visits")
    public Map<String, Object> getVisits() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalVisits", visitCounter.get());
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }
    
    private String formatBytes(long bytes) {
        long mb = bytes / (1024 * 1024);
        return mb + " MB";
    }
}
