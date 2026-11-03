package com.chaxu.auth;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Profile("mysql")
public class AuthService {
    public record Account(long id, String username, String displayName, List<String> roles) implements Serializable {}
    private record Credential(long id, String username, String displayName, String hash, boolean enabled) {}
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final String dummyHash;

    public AuthService(JdbcTemplate jdbc, PasswordEncoder passwords) {
        this.jdbc = jdbc;
        this.passwords = passwords;
        this.dummyHash = passwords.encode("unused-password-for-timing");
    }

    private String normalize(String username) { return username.trim().toLowerCase(Locale.ROOT); }

    @Transactional
    public Account register(String username, String password, String displayName) {
        String name = normalize(username);
        var key = new GeneratedKeyHolder();
        try {
            jdbc.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users(username, display_name, password_hash) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, name);
                statement.setString(2, displayName.strip());
                statement.setString(3, passwords.encode(password));
                return statement;
            }, key);
        } catch (DuplicateKeyException error) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该账号已被注册，请更换账号或直接登录");
        }
        long id = key.getKey().longValue();
        jdbc.update("INSERT INTO user_roles(user_id, role_id) SELECT ?, id FROM roles WHERE code = 'CUSTOMER'", id);
        jdbc.update("INSERT INTO member_accounts(user_id, points) VALUES (?, 0)", id);
        return new Account(id, name, displayName.strip(), List.of("CUSTOMER"));
    }

    public Account login(String username, String password) {
        var rows = jdbc.query("SELECT id, username, display_name, password_hash, enabled FROM users WHERE username = ?",
            (rs, row) -> new Credential(rs.getLong("id"), rs.getString("username"), rs.getString("display_name"),
                rs.getString("password_hash"), rs.getBoolean("enabled")), normalize(username));
        Credential user = rows.isEmpty() ? null : rows.getFirst();
        boolean matches = passwords.matches(password, user == null || user.hash() == null ? dummyHash : user.hash());
        if (user == null || user.hash() == null || !matches || !user.enabled())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码错误，或账号已停用");
        List<String> roles = jdbc.queryForList("SELECT r.code FROM roles r JOIN user_roles ur ON ur.role_id = r.id WHERE ur.user_id = ?", String.class, user.id());
        return new Account(user.id(), user.username(), user.displayName(), roles);
    }
}
