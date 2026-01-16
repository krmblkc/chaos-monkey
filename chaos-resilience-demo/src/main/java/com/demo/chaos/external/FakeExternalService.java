package com.demo.chaos.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sahte harici servis - başka bir mikroservisi simüle eder.
 * 
 * Mental model: Bu bir payment gateway, inventory service veya
 * third-party API olabilir.
 * 
 * Chaos Monkey bu servisin metodlarını hedef alacak.
 */
@Service
public class FakeExternalService {

    private static final Logger log = LoggerFactory.getLogger(FakeExternalService.class);
    private final AtomicInteger callCount = new AtomicInteger(0);

    /**
     * Harici servisi çağırır.
     * Normal durumda ~200ms sürer.
     * Chaos Monkey aktifken 3-5 saniye gecikme eklenir.
     */
    public String callExternalApi() {
        int count = callCount.incrementAndGet();
        long startTime = System.currentTimeMillis();

        log.info("📡 [{}] External API çağrısı başlıyor...", count);

        // Normal çalışma simülasyonu - 200ms
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("✅ [{}] External API cevap verdi: {}ms", count, duration);

        return String.format("""
                {
                    "source": "External Payment Service",
                    "status": "SUCCESS",
                    "callNumber": %d,
                    "responseTime": "%dms",
                    "timestamp": "%s"
                }
                """,
                count,
                duration,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    /**
     * Çağrı sayacını sıfırlar (demo için)
     */
    public void resetCounter() {
        callCount.set(0);
        log.info("🔄 Call counter reset");
    }

    public int getCallCount() {
        return callCount.get();
    }
}
