package com.chaxu.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

/** 单实例限流；不信任客户端提供的 X-Forwarded-For。 */
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private record Window(long start, int count) {}
    private final Map<String, Window> windows = new LinkedHashMap<>();
    private synchronized boolean allow(String address) {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().start() >= 60_000);
        Window window = windows.get(address);
        if (window == null) {
            if (windows.size() >= 2048) return false;
            windows.put(address, new Window(now, 1));
            return true;
        }
        if (window.count() >= 20) return false;
        windows.put(address, new Window(window.start(), window.count() + 1));
        return true;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        String path = request.getServletPath();
        if ("POST".equals(request.getMethod()) && (path.equals("/api/auth/login") || path.equals("/api/auth/register")) && !allow(request.getRemoteAddr())) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"尝试过于频繁，请一分钟后重试\",\"data\":null}");
            return;
        }
        chain.doFilter(request, response);
    }
}
