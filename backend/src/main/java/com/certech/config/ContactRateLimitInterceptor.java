package com.certech.config;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/** Per-process backstop only. The hosting edge must enforce the distributed limit. */
public final class ContactRateLimitInterceptor implements HandlerInterceptor {
    private static final int MAX_CLIENT_BUCKETS = 10_000;
    private final int maxRequests;
    private final long windowMillis;
    private final byte[] processKey = new byte[32];
    private final Map<String, Window> windows = new HashMap<>();

    public ContactRateLimitInterceptor(int maxRequests, long windowSeconds) {
        if (maxRequests < 1 || windowSeconds < 1) {
            throw new IllegalArgumentException("Contact rate limit settings must be positive.");
        }
        this.maxRequests = maxRequests;
        this.windowMillis = Math.multiplyExact(windowSeconds, 1000L);
        new SecureRandom().nextBytes(processKey);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return true;

        long now = System.currentTimeMillis();
        long windowStart = now - now % windowMillis;
        String client = fingerprint(request.getRemoteAddr());
        synchronized (windows) {
            Iterator<Map.Entry<String, Window>> iterator = windows.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getValue().startMillis + windowMillis <= now) iterator.remove();
            }
            Window window = windows.get(client);
            if (window == null) {
                if (windows.size() >= MAX_CLIENT_BUCKETS) {
                    reject(response, (int) Math.max(1, (windowStart + windowMillis - now + 999) / 1000));
                    return false;
                }
                window = new Window(windowStart, 0);
                windows.put(client, window);
            } else if (window.startMillis != windowStart) {
                window = new Window(windowStart, 0);
                windows.put(client, window);
            }
            if (window.count >= maxRequests) {
                reject(response, (int) Math.max(1, (windowStart + windowMillis - now + 999) / 1000));
                return false;
            }
            window.count++;
        }
        return true;
    }

    private String fingerprint(String address) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(processKey, "HmacSHA256"));
        byte[] digest = mac.doFinal((address == null ? "unknown" : address).getBytes(StandardCharsets.UTF_8));
        return java.util.HexFormat.of().formatHex(digest);
    }

    private void reject(HttpServletResponse response, int retryAfterSeconds) throws java.io.IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", Integer.toString(retryAfterSeconds));
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"Has enviado demasiadas solicitudes. Inténtalo de nuevo más tarde.\"}");
    }

    private static final class Window {
        private final long startMillis;
        private int count;
        private Window(long startMillis, int count) { this.startMillis = startMillis; this.count = count; }
    }
}
