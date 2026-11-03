package com.chaxu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
 "spring.datasource.url=jdbc:h2:mem:auth;MODE=MySQL;DB_CLOSE_DELAY=-1", "spring.datasource.username=sa",
 "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
 "spring.flyway.enabled=false", "spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:auth-schema.sql"
})
@ActiveProfiles("mysql")
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    @Autowired ObjectMapper json;
    private RequestPostProcessor csrfToken() throws Exception {
        var response = mvc.perform(get("/api/csrf")).andReturn().getResponse();
        var data = json.readTree(response.getContentAsString()).get("data");
        var cookie = response.getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        return request -> {
            request.setCookies(cookie);
            request.addHeader(data.get("headerName").asText(), data.get("token").asText());
            return request;
        };
    }
    private final String body = "{\"username\":\"tester_1\",\"password\":\"StrongPass123!\",\"displayName\":\"测试茶友\",\"roles\":[\"ADMIN\"]}";

    @Test void registrationSessionAndPermissions() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/register").with(csrfToken()).contentType("application/json").content(body))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.roles[0]").value("CUSTOMER"))
            .andExpect(jsonPath("$.data.password_hash").doesNotExist());
        String hash = jdbc.queryForObject("SELECT password_hash FROM users WHERE username='tester_1'", String.class);
        assertNotEquals("StrongPass123!", hash);
        assertTrue(passwords.matches("StrongPass123!", hash));
        assertEquals(0, jdbc.queryForObject("SELECT points FROM member_accounts m JOIN users u ON u.id=m.user_id WHERE u.username='tester_1'", Long.class));
        mvc.perform(post("/api/auth/register").with(csrfToken()).contentType("application/json").content(body)).andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").with(csrfToken()).contentType("application/json").content("{\"username\":\"tester_1\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/shopping-access").with(csrfToken())).andExpect(status().isUnauthorized());
        var before = new MockHttpSession();
        String oldId = before.getId();
        var result = mvc.perform(post("/api/auth/login").session(before).with(csrfToken()).contentType("application/json").content(body))
            .andExpect(status().isOk()).andReturn();
        var session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotEquals(oldId, session.getId());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.data.username").value("tester_1"));
        mvc.perform(post("/api/auth/shopping-access").session(session).with(csrfToken())).andExpect(status().isOk());
        mvc.perform(post("/api/admin/access").session(session).with(csrfToken())).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isForbidden());
        assertFalse(session.isInvalid());
        mvc.perform(post("/api/auth/logout").session(session).with(csrfToken())).andExpect(status().isNoContent())
            .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(jsonPath("$.data").isEmpty());
        mvc.perform(post("/api/auth/shopping-access").with(csrfToken())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/logout").with(csrfToken())).andExpect(status().isNoContent());
        jdbc.update("UPDATE users SET enabled=false WHERE username='tester_1'");
        mvc.perform(post("/api/auth/login").with(csrfToken()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
    }
    @Test void validation() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrfToken()).contentType("application/json")
            .content("{\"username\":\"bad\",\"password\":\"short\",\"displayName\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void anonymousPagesAndCsrf() throws Exception {
        // 没有静态资源时为 404，不能因未登录返回 401/302。
        mvc.perform(get("/member")).andExpect(status().isNotFound());
        mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isNotEmpty());
        mvc.perform(get("/api/auth/me")).andExpect(status().isOk());
    }
    @Test void realCsrfHeaderAndCookie() throws Exception {
        var response = mvc.perform(get("/api/csrf")).andReturn().getResponse();
        var data = json.readTree(response.getContentAsString()).get("data");
        mvc.perform(post("/api/auth/register").cookie(response.getCookie("XSRF-TOKEN"))
            .header(data.get("headerName").asText(), data.get("token").asText())
            .contentType("application/json").content(body.replace("tester_1", "csrf_tea")))
            .andExpect(status().isCreated());
    }
    @Test @WithMockUser(roles = "STAFF") void staffCanUseStaffApi() throws Exception {
        mvc.perform(post("/api/admin/access").with(csrfToken())).andExpect(status().isOk());
    }
}
