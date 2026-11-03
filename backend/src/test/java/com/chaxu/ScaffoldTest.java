package com.chaxu;

import com.chaxu.membership.PointsPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScaffoldTest {
    @Autowired MockMvc mvc;
    @Test void demoStartsWithoutDatabase() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("UP"));
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(6));
    }
    @Test void adminRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/admin/orders")).andExpect(status().isUnauthorized());
    }
    @Test void writeWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/api/orders")).andExpect(status().isForbidden());
    }
    @Test void pointsUseActualPaidWholeYuan() {
        assertEquals(23, PointsPolicy.earned(2399));
        assertEquals(0, PointsPolicy.earned(99));
        assertThrows(IllegalArgumentException.class, () -> PointsPolicy.earned(-1));
    }
}
