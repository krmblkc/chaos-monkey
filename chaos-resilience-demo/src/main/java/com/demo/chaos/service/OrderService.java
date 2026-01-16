package com.demo.chaos.service;

import com.demo.chaos.external.FakeExternalService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Sipariş işleme servisi.
 * 
 * İki versiyon sunar:
 * 1. processOrder() - Korumasız, kaos durumunda yavaşlar
 * 2. processOrderProtected() - Resilience4j ile korumalı
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final FakeExternalService externalService;

    public OrderService(FakeExternalService externalService) {
        this.externalService = externalService;
    }

    /**
     * 🔴 KORUMASIZ sipariş işleme
     * 
     * Chaos Monkey aktifken bu metod yavaşlar ve
     * thread'ler bloklanır.
     */
    public String processOrder() {
        log.info("📦 Sipariş işleniyor (KORUMASIZ)...");
        long startTime = System.currentTimeMillis();

        // External servisi çağır - burada Chaos Monkey devreye girebilir
        String externalResponse = externalService.callExternalApi();

        long duration = System.currentTimeMillis() - startTime;
        log.info("📦 Sipariş tamamlandı: {}ms", duration);

        return String.format("""
                {
                    "orderId": "ORD-%d",
                    "status": "COMPLETED",
                    "protected": false,
                    "processingTime": "%dms",
                    "externalService": %s,
                    "timestamp": "%s"
                }
                """,
                System.currentTimeMillis() % 10000,
                duration,
                externalResponse.trim(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * 🟢 KORUMALI sipariş işleme - Resilience4j ile
     * 
     * @CircuitBreaker: Hata oranı %50'yi geçerse devre açılır
     * @TimeLimiter: 2 saniye timeout (CompletableFuture gerektirir)
     */
    @CircuitBreaker(name = "externalService", fallbackMethod = "processOrderFallback")
    @TimeLimiter(name = "externalService", fallbackMethod = "processOrderTimeoutFallback")
    public CompletableFuture<String> processOrderProtected() {
        return CompletableFuture.supplyAsync(() -> {
            log.info("🛡️ Sipariş işleniyor (KORUMALI)...");
            long startTime = System.currentTimeMillis();

            // External servisi çağır
            String externalResponse = externalService.callExternalApi();

            long duration = System.currentTimeMillis() - startTime;
            log.info("🛡️ Sipariş tamamlandı: {}ms", duration);

            return String.format("""
                    {
                        "orderId": "ORD-%d",
                        "status": "COMPLETED",
                        "protected": true,
                        "processingTime": "%dms",
                        "externalService": %s,
                        "timestamp": "%s"
                    }
                    """,
                    System.currentTimeMillis() % 10000,
                    duration,
                    externalResponse.trim(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        });
    }

    /**
     * 🔶 FALLBACK - Circuit Breaker açıldığında
     */
    public CompletableFuture<String> processOrderFallback(Throwable t) {
        log.warn("⚡ Circuit Breaker FALLBACK! Sebep: {}", t.getMessage());

        return CompletableFuture.completedFuture(String.format("""
                {
                    "orderId": "ORD-FALLBACK-%d",
                    "status": "DEGRADED",
                    "protected": true,
                    "fallbackReason": "Circuit Breaker Open",
                    "message": "Sipariş kuyruğa alındı, daha sonra işlenecek",
                    "originalError": "%s",
                    "timestamp": "%s"
                }
                """,
                System.currentTimeMillis() % 10000,
                t.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

    /**
     * 🔶 FALLBACK - Timeout durumunda
     */
    public CompletableFuture<String> processOrderTimeoutFallback(Throwable t) {
        log.warn("⏱️ TIMEOUT FALLBACK! Sebep: {}", t.getMessage());

        return CompletableFuture.completedFuture(String.format("""
                {
                    "orderId": "ORD-TIMEOUT-%d",
                    "status": "DEGRADED",
                    "protected": true,
                    "fallbackReason": "Timeout (>2s)",
                    "message": "Servis yavaş, fallback response döndürülüyor",
                    "timestamp": "%s"
                }
                """,
                System.currentTimeMillis() % 10000,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }
}
