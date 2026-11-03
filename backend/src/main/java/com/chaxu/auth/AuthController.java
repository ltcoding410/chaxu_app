package com.chaxu.auth;

import com.chaxu.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegisterRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_]{4,32}", message = "账号需为 4–32 位字母、数字或下划线") String username,
        @NotBlank @Size(min = 8, max = 64, message = "密码需为 8–64 位")
        @Pattern(regexp = "[\\x21-\\x7E]+", message = "密码请使用英文字母、数字或符号，不含空格") String password,
        @NotBlank @Size(max = 50, message = "昵称最多 50 字") String displayName) {}
    public record LoginRequest(@NotBlank @Size(max = 50) String username, @NotBlank @Size(max = 64) String password) {}
    private final ObjectProvider<AuthService> services;
    private final CsrfTokenRepository csrf;
    private final HttpSessionSecurityContextRepository contexts = new HttpSessionSecurityContextRepository();
    public AuthController(ObjectProvider<AuthService> services, CsrfTokenRepository csrf) {
        this.services = services;
        this.csrf = csrf;
    }
    private AuthService service() {
        AuthService service = services.getIfAvailable();
        if (service == null) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "账号服务未启用，请启动 mysql 模式并配置数据库");
        return service;
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthService.Account> register(@Valid @RequestBody RegisterRequest body) {
        return ApiResponse.ok(service().register(body.username(), body.password(), body.displayName()));
    }
    @PostMapping("/login")
    public ApiResponse<AuthService.Account> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request, HttpServletResponse response) {
        var account = service().login(body.username(), body.password());
        if (request.getSession(false) != null) request.changeSessionId();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(account, null,
            account.roles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        csrf.saveToken(null, request, response);
        return ApiResponse.ok(account);
    }
    @GetMapping("/me")
    public ApiResponse<Object> me(Authentication authentication) {
        return ApiResponse.ok(authentication != null && authentication.getPrincipal() instanceof AuthService.Account account ? account : null);
    }
    @PostMapping("/shopping-access")
    public ApiResponse<Object> shoppingAccess(Authentication authentication) { return ApiResponse.ok(authentication.getPrincipal()); }
}
