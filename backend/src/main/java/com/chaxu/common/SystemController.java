package com.chaxu.common;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {
    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> health() { return ApiResponse.ok(Map.of("status", "UP", "stage", "scaffold")); }

    @GetMapping("/api/csrf")
    public ApiResponse<Map<String, String>> csrf(CsrfToken token) {
        return ApiResponse.ok(Map.of("headerName", token.getHeaderName(), "token", token.getToken()));
    }
}
