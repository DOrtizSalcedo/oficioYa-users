package edu.eci.dosw.oficioya_users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.dto.StatusChangeDTO;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.security.JwtTestUtils;
import edu.eci.dosw.oficioya_users.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(WebSecurityTestConfig.class)
class AdminUserControllerTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private AdminUserService adminUserService;

    private final UUID adminId = UUID.randomUUID();
    private final String adminToken = JwtTestUtils.bearer(adminId.toString(), List.of("ADMIN"));
    private final String workerToken = JwtTestUtils.bearer(UUID.randomUUID().toString(), List.of("TRABAJADOR"));

    private AdminUserDTO adminView(UUID id, AccountStatus status) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(id);
        dto.setName("Ana Pérez");
        dto.setEmail("ana@mail.com");
        dto.setAccountStatus(status);
        return dto;
    }

    private String reason(String text) throws Exception {
        StatusChangeDTO dto = new StatusChangeDTO();
        dto.setReason(text);
        return objectMapper.writeValueAsString(dto);
    }

    @Test
    void adminCanListUsersWithFullData() throws Exception {
        when(adminUserService.findAll(any(Pageable.class))).thenReturn(
                new PageImpl<>(List.of(adminView(UUID.randomUUID(), AccountStatus.ACTIVE)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/admin/users").header(AUTH, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("ana@mail.com"));
    }

    @Test
    void nonAdminGets403() throws Exception {
        mockMvc.perform(get("/api/admin/users").header(AUTH, workerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(adminUserService);
    }

    @Test
    void withoutTokenGets401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanSeeOneUser() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminUserService.findById(id)).thenReturn(adminView(id, AccountStatus.INACTIVE));

        mockMvc.perform(get("/api/admin/users/{id}", id).header(AUTH, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("INACTIVE"));
    }

    @Test
    void deactivateUsesAdminIdFromToken() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminUserService.deactivate(adminId, id, "Mal uso")).thenReturn(adminView(id, AccountStatus.INACTIVE));

        mockMvc.perform(patch("/api/admin/users/{id}/deactivate", id).header(AUTH, adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(reason("Mal uso")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("INACTIVE"));
    }

    @Test
    void deactivateWithoutReasonReturns400() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{id}/deactivate", UUID.randomUUID()).header(AUTH, adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(reason(" ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.reason").exists());
        verifyNoInteractions(adminUserService);
    }

    @Test
    void activateReturns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminUserService.activate(adminId, id, "Caso resuelto")).thenReturn(adminView(id, AccountStatus.ACTIVE));

        mockMvc.perform(patch("/api/admin/users/{id}/activate", id).header(AUTH, adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(reason("Caso resuelto")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }
}
