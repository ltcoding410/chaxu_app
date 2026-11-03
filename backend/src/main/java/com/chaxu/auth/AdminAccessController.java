package com.chaxu.auth;

import com.chaxu.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminAccessController {
    @PostMapping("/api/admin/access")
    public ApiResponse<Boolean> access() { return ApiResponse.ok(true); }
}
