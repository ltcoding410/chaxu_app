package com.chaxu;

import com.chaxu.auth.AuthRateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class AuthRateLimitTest {
    @Test void repeatedAttemptsAreLimitedWithoutBlockingBrowsing() throws Exception {
        var filter = new AuthRateLimitFilter();
        for (int i = 0; i < 21; i++) {
            var request = new MockHttpServletRequest("POST", "/api/auth/login");
            request.setServletPath("/api/auth/login");
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req, res) -> {});
            assertEquals(i < 20 ? 200 : 429, response.getStatus());
        }
        var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET", "/api/products"), response, (req, res) -> {});
        assertEquals(200, response.getStatus());
    }
}
