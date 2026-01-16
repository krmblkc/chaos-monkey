# 🎬 Demo Sunucu Scripti

## Ön Hazırlık

```bash
cd /Users/kerembalkac/Documents/DeMDemo/chaos-resilience-demo
mvn spring-boot:run
```

---

## 1️⃣ Normal Durum (1 dakika)

> **Söyle:** "Şu an sistemimiz normal çalışıyor. External servis hızlı, her şey yolunda."

```bash
# Senaryo 1'e geç
curl http://localhost:8080/api/demo/scenario/1

# Normal request - hızlı response beklenir
curl -w "\n⏱️ Süre: %{time_total}s\n" http://localhost:8080/api/order
```

**Beklenen:** ~200ms response ✅

---

## 2️⃣ Chaos Monkey Devrede (1.5 dakika)

> **Söyle:** "Şimdi Chaos Monkey'i devreye alıyorum."

```bash
# Senaryo 2'ye geç - Chaos aktif
curl http://localhost:8080/api/demo/scenario/2

# Aynı endpoint - YAVAŞ response
curl -w "\n⏱️ Süre: %{time_total}s\n" http://localhost:8080/api/order
```

**Beklenen:** 3-5 saniye ⚠️

> **Vurgula:** "Problem Chaos Monkey değil. Problem bizim hazırlıksız olmamız."

---

## 3️⃣ Korumasız Sistemin Çöküşü (1 dakika)

> **Söyle:** "Hiçbir timeout, circuit breaker veya fallback yok."

```bash
# Paralel requestler gönder - sistem kilitlenir
curl http://localhost:8080/api/order &
curl http://localhost:8080/api/order &
curl http://localhost:8080/api/order &
```

> **Vurgula:** "Prod'da yaşadığımız kaos tam olarak bu."

---

## 4️⃣ Resilience4j ile Yönetilen Kaos (1.5 dakika)

> **Söyle:** "Sistemi düzeltmiyoruz. Sistemin bozulmasına nasıl tepki verdiğimizi değiştiriyoruz."

```bash
# Senaryo 4'e geç - Resilience4j aktif
curl http://localhost:8080/api/demo/scenario/4

# KORUMALI endpoint - HIZLI fallback
curl -w "\n⏱️ Süre: %{time_total}s\n" http://localhost:8080/api/order/protected
```

**Beklenen:** ~2 saniye + Fallback response 🛡️

---

## 5️⃣ Kapanış - Durum Kontrolü

```bash
# Mevcut durumu göster
curl http://localhost:8080/api/demo/status | jq
```

---

## 🔧 Faydalı Komutlar

```bash
# Circuit breaker durumu
curl http://localhost:8080/actuator/circuitbreakers

# Chaos Monkey durumu  
curl http://localhost:8080/actuator/chaosmonkey

# Circuit breaker reset
curl -X POST http://localhost:8080/api/demo/reset-circuit-breaker
```
