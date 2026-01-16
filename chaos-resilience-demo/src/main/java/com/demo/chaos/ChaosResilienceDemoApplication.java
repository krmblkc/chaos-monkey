package com.demo.chaos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Chaos Monkey + Resilience4j Demo Application
 * 
 * Bu uygulama, sistemlerin arızalara karşı nasıl dayanıklı hale getirileceğini
 * göstermek için tasarlanmıştır.
 * 
 * Demo Senaryoları:
 * 1. Normal çalışma (hızlı response)
 * 2. Chaos Monkey ile kaos (yavaş response)
 * 3. Resilience4j ile yönetilen kaos (fallback response)
 */
@SpringBootApplication
public class ChaosResilienceDemoApplication {

    public static void main(String[] args) {
        System.out.println("""

                ╔══════════════════════════════════════════════════════════════╗
                ║     🐵 CHAOS MONKEY + 🛡️ RESILIENCE4J DEMO                   ║
                ╠══════════════════════════════════════════════════════════════╣
                ║                                                              ║
                ║  Endpoints:                                                  ║
                ║  • GET /api/order           → Normal sipariş                 ║
                ║  • GET /api/order/protected → Resilience4j korumalı          ║
                ║  • GET /api/demo/scenario/1 → Normal mod                     ║
                ║  • GET /api/demo/scenario/2 → Chaos Monkey aktif             ║
                ║  • GET /api/demo/scenario/4 → Resilience4j + Chaos           ║
                ║  • GET /api/demo/status     → Mevcut durum                   ║
                ║                                                              ║
                ╚══════════════════════════════════════════════════════════════╝

                """);
        SpringApplication.run(ChaosResilienceDemoApplication.class, args);
    }
}
