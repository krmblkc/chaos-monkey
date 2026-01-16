package com.demo.chaos.controller;

import de.codecentric.spring.boot.chaos.monkey.configuration.ChaosMonkeySettings;
import de.codecentric.spring.boot.chaos.monkey.configuration.AssaultProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Demo Kontrol Controller
 * 
 * Sunum sırasında senaryolar arası geçiş için kullanılır.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private static final Logger log = LoggerFactory.getLogger(DemoController.class);

    private final ChaosMonkeySettings chaosMonkeySettings;
    private final AssaultProperties assaultProperties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private String currentScenario = "1 - Normal";

    public DemoController(
            ChaosMonkeySettings chaosMonkeySettings,
            AssaultProperties assaultProperties,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.chaosMonkeySettings = chaosMonkeySettings;
        this.assaultProperties = assaultProperties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    /**
     * Senaryo değiştirme endpoint'i
     * 
     * @param scenarioNumber 1-4 arası senaryo numarası
     */
    @GetMapping("/scenario/{scenarioNumber}")
    public ResponseEntity<String> setScenario(@PathVariable int scenarioNumber) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🎬 SENARYO DEĞİŞTİRİLİYOR: {}", scenarioNumber);
        log.info("═══════════════════════════════════════════════════════════");

        String message;

        switch (scenarioNumber) {
            case 1 -> {
                // Normal durum - Chaos Monkey kapalı
                chaosMonkeySettings.getChaosMonkeyProperties().setEnabled(false);
                currentScenario = "1 - Normal Durum";
                message = """
                        ╔══════════════════════════════════════════════════╗
                        ║  1️⃣ SENARYO: NORMAL DURUM                         ║
                        ╠══════════════════════════════════════════════════╣
                        ║  🐵 Chaos Monkey: KAPALI                          ║
                        ║  🛡️ Resilience4j: PASIF                           ║
                        ║                                                  ║
                        ║  Beklenen: Hızlı response (~200ms)               ║
                        ║  Test: GET /api/order                            ║
                        ╚══════════════════════════════════════════════════╝
                        """;
                log.info("🐵 Chaos Monkey: KAPALI");
            }

            case 2 -> {
                // Chaos Monkey aktif - Latency injection
                chaosMonkeySettings.getChaosMonkeyProperties().setEnabled(true);
                assaultProperties.setLatencyActive(true);
                assaultProperties.setLatencyRangeStart(3000);
                assaultProperties.setLatencyRangeEnd(5000);
                assaultProperties.setExceptionsActive(false);
                currentScenario = "2 - Chaos Monkey Aktif";
                message = """
                        ╔══════════════════════════════════════════════════╗
                        ║  2️⃣ SENARYO: CHAOS MONKEY AKTİF                   ║
                        ╠══════════════════════════════════════════════════╣
                        ║  🐵 Chaos Monkey: AÇIK                            ║
                        ║  ⚡ Latency: 3-5 saniye                           ║
                        ║  🛡️ Resilience4j: PASIF                           ║
                        ║                                                  ║
                        ║  Beklenen: YAVAŞ response (3-5 saniye)           ║
                        ║  Problem: Thread bloklanıyor!                    ║
                        ║  Test: GET /api/order                            ║
                        ╚══════════════════════════════════════════════════╝
                        """;
                log.info("🐵 Chaos Monkey: AÇIK - Latency 3-5sn");
            }

            case 3 -> {
                // Chaos Monkey + Exception
                chaosMonkeySettings.getChaosMonkeyProperties().setEnabled(true);
                assaultProperties.setLatencyActive(true);
                assaultProperties.setLatencyRangeStart(3000);
                assaultProperties.setLatencyRangeEnd(5000);
                assaultProperties.setExceptionsActive(true);
                currentScenario = "3 - Chaos + Exceptions";
                message = """
                        ╔══════════════════════════════════════════════════╗
                        ║  3️⃣ SENARYO: CHAOS + EXCEPTIONS                   ║
                        ╠══════════════════════════════════════════════════╣
                        ║  🐵 Chaos Monkey: AÇIK                            ║
                        ║  ⚡ Latency: 3-5 saniye                           ║
                        ║  💥 Exceptions: AÇIK                              ║
                        ║  🛡️ Resilience4j: PASIF                           ║
                        ║                                                  ║
                        ║  Beklenen: Hatalar ve gecikmeler                 ║
                        ║  Test: GET /api/order                            ║
                        ╚══════════════════════════════════════════════════╝
                        """;
                log.info("🐵 Chaos Monkey: AÇIK - Latency + Exceptions");
            }

            case 4 -> {
                // Chaos Monkey + Resilience4j
                chaosMonkeySettings.getChaosMonkeyProperties().setEnabled(true);
                assaultProperties.setLatencyActive(true);
                assaultProperties.setLatencyRangeStart(3000);
                assaultProperties.setLatencyRangeEnd(5000);
                assaultProperties.setExceptionsActive(false);

                // Circuit breaker'ı resetle
                try {
                    CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("externalService");
                    cb.reset();
                    log.info("🔄 Circuit Breaker reset edildi");
                } catch (Exception e) {
                    log.warn("Circuit breaker reset failed: {}", e.getMessage());
                }

                currentScenario = "4 - Yönetilen Kaos (Resilience4j)";
                message = """
                        ╔══════════════════════════════════════════════════╗
                        ║  4️⃣ SENARYO: YÖNETİLEN KAOS                       ║
                        ╠══════════════════════════════════════════════════╣
                        ║  🐵 Chaos Monkey: AÇIK                            ║
                        ║  ⚡ Latency: 3-5 saniye                           ║
                        ║  🛡️ Resilience4j: AKTİF                           ║
                        ║     ⏱️ Timeout: 2 saniye                          ║
                        ║     🔌 Circuit Breaker: READY                     ║
                        ║                                                  ║
                        ║  Beklenen: HIZLI fallback response               ║
                        ║  Test: GET /api/order/protected                  ║
                        ╚══════════════════════════════════════════════════╝
                        """;
                log.info("🛡️ Resilience4j AKTİF - Timeout: 2s, Circuit Breaker: Ready");
            }

            default -> {
                return ResponseEntity.badRequest().body("""
                        {
                            "error": "Geçersiz senaryo numarası",
                            "validScenarios": [1, 2, 3, 4]
                        }
                        """);
            }
        }

        log.info("✅ Senaryo değiştirildi: {}", currentScenario);
        return ResponseEntity.ok(message);
    }

    /**
     * Mevcut durumu gösterir
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getStatus() {
        boolean chaosEnabled = chaosMonkeySettings.getChaosMonkeyProperties().isEnabled();
        boolean latencyActive = assaultProperties.isLatencyActive();
        boolean exceptionsActive = assaultProperties.isExceptionsActive();

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("externalService");
        String cbState = cb.getState().name();

        String status = String.format("""
                {
                    "currentScenario": "%s",
                    "chaosMonkey": {
                        "enabled": %s,
                        "latencyActive": %s,
                        "latencyRange": "%d-%dms",
                        "exceptionsActive": %s
                    },
                    "resilience4j": {
                        "circuitBreakerState": "%s",
                        "timeoutDuration": "2s"
                    },
                    "endpoints": {
                        "unprotected": "/api/order",
                        "protected": "/api/order/protected"
                    }
                }
                """,
                currentScenario,
                chaosEnabled,
                latencyActive,
                assaultProperties.getLatencyRangeStart(),
                assaultProperties.getLatencyRangeEnd(),
                exceptionsActive,
                cbState);

        return ResponseEntity.ok(status);
    }

    /**
     * Circuit Breaker'ı resetler
     */
    @PostMapping("/reset-circuit-breaker")
    public ResponseEntity<String> resetCircuitBreaker() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("externalService");
        cb.reset();
        log.info("🔄 Circuit Breaker manuel olarak reset edildi");

        return ResponseEntity.ok("""
                {
                    "message": "Circuit Breaker reset edildi",
                    "newState": "CLOSED"
                }
                """);
    }
}
