package com.demo.chaos.controller;

import com.demo.chaos.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

/**
 * Sipariş API Controller
 * 
 * Demo endpoint'leri:
 * - /api/order → Korumasız sipariş
 * - /api/order/protected → Resilience4j korumalı
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 🔴 KORUMASIZ Sipariş Endpoint
     * 
     * Chaos Monkey aktifken bu endpoint yavaşlar.
     * Thread bloklanır ve sistem kilitlenebilir.
     */
    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createOrder() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📥 /api/order - KORUMASIZ endpoint çağrıldı");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long startTime = System.currentTimeMillis();
        String result = orderService.processOrder();
        long duration = System.currentTimeMillis() - startTime;

        log.info("📤 Response süresi: {}ms", duration);
        return ResponseEntity.ok(result);
    }

    /**
     * 🟢 KORUMALI Sipariş Endpoint
     * 
     * Resilience4j ile korumalı:
     * - 2 saniye timeout
     * - Circuit breaker
     * - Fallback response
     */
    @GetMapping(value = "/order/protected", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createOrderProtected() {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🛡️ /api/order/protected - KORUMALI endpoint çağrıldı");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        long startTime = System.currentTimeMillis();

        try {
            String result = orderService.processOrderProtected().get();
            long duration = System.currentTimeMillis() - startTime;
            log.info("📤 Response süresi: {}ms", duration);
            return ResponseEntity.ok(result);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error processing protected order", e);
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("""
                {
                    "status": "UP",
                    "message": "Chaos Resilience Demo is running!"
                }
                """);
    }
}
