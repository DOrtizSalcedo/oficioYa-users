package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.RoleDTO;
import edu.eci.dosw.oficioya_users.security.JwtTestUtils;
import edu.eci.dosw.oficioya_users.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@Import(WebSecurityTestConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RoleService roleService;

    private final String token = JwtTestUtils.bearer(UUID.randomUUID().toString(), "CONTRATANTE");

    private RoleDTO role(UUID id, String name) {
        RoleDTO dto = new RoleDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    @Test
    void getAllReturnsRoles() throws Exception {
        when(roleService.findAll()).thenReturn(List.of(role(UUID.randomUUID(), "ADMIN")));

        mockMvc.perform(get("/api/roles").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void getByIdReturnsRole() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.findById(id)).thenReturn(role(id, "TRABAJADOR"));

        mockMvc.perform(get("/api/roles/{id}", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TRABAJADOR"));
    }
}
