package edu.eci.dosw.oficioya_users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya_users.TestData;
import edu.eci.dosw.oficioya_users.dto.AddRoleDTO;
import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.exception.BusinessRuleException;
import edu.eci.dosw.oficioya_users.exception.ConflictException;
import edu.eci.dosw.oficioya_users.exception.ForbiddenOperationException;
import edu.eci.dosw.oficioya_users.exception.ResourceNotFoundException;
import edu.eci.dosw.oficioya_users.security.JwtTestUtils;
import edu.eci.dosw.oficioya_users.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(WebSecurityTestConfig.class)
class UserControllerTest {

    private static final String AUTH = "Authorization";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    private final UUID userId = UUID.randomUUID();
    private final String token = JwtTestUtils.bearer(userId.toString(), "TRABAJADOR");

    private OwnProfileDTO ownProfile() {
        OwnProfileDTO dto = new OwnProfileDTO();
        dto.setId(userId);
        dto.setName("Ana Pérez");
        dto.setEmail("*****@mail.com");
        dto.setRoles(Set.of("TRABAJADOR"));
        return dto;
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    // ---------- registro ----------

    @Test
    void registerIsPublicAndReturns201() throws Exception {
        when(userService.register(any(UserRequestDTO.class))).thenReturn(ownProfile());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json(TestData.userRequest(RoleName.TRABAJADOR))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("*****@mail.com"));
    }

    @Test
    void registerWithInvalidDataReturns400WithFieldErrors() throws Exception {
        UserRequestDTO request = TestData.userRequest(RoleName.TRABAJADOR);
        request.setName("");
        request.setEmail("no-es-correo");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
        verifyNoInteractions(userService);
    }

    @Test
    void registerWithMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{ mal json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithUnknownRoleReturns400() throws Exception {
        String body = "{\"name\":\"Ana\",\"email\":\"ana@mail.com\","
                + "\"phones\":[{\"number\":\"3001234567\",\"principal\":true}],\"roles\":[\"JEFE\"]}";

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerDuplicatedEmailReturns409() throws Exception {
        when(userService.register(any(UserRequestDTO.class))).thenThrow(new ConflictException("Ya existe"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json(TestData.userRequest(RoleName.TRABAJADOR))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe"));
    }

    @Test
    void registerConcurrentDuplicateReturns409() throws Exception {
        when(userService.register(any(UserRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json(TestData.userRequest(RoleName.TRABAJADOR))))
                .andExpect(status().isConflict());
    }

    @Test
    void registerAdminRoleReturns403() throws Exception {
        when(userService.register(any(UserRequestDTO.class)))
                .thenThrow(new ForbiddenOperationException("No permitido"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json(TestData.userRequest(RoleName.ADMIN))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unexpectedErrorReturns500() throws Exception {
        when(userService.register(any(UserRequestDTO.class))).thenThrow(new IllegalStateException("fallo"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(json(TestData.userRequest(RoleName.TRABAJADOR))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error interno del servidor"));
    }

    // ---------- perfil propio ----------

    @Test
    void getOwnProfileUsesIdFromToken() throws Exception {
        when(userService.getOwnProfile(userId)).thenReturn(ownProfile());

        mockMvc.perform(get("/api/users/me").header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void getOwnProfileWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(userService);
    }

    @Test
    void getOwnProfileWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me").header(AUTH, "Bearer token.invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOwnProfileOfInactiveAccountReturns403() throws Exception {
        when(userService.getOwnProfile(userId)).thenThrow(new ForbiddenOperationException("La cuenta está inactiva"));

        mockMvc.perform(get("/api/users/me").header(AUTH, token))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOwnProfileReturns200() throws Exception {
        UserUpdateDTO request = new UserUpdateDTO();
        request.setName("Ana María");
        when(userService.updateOwnProfile(eq(userId), any(UserUpdateDTO.class))).thenReturn(ownProfile());

        mockMvc.perform(patch("/api/users/me").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk());
        verify(userService).updateOwnProfile(eq(userId), any(UserUpdateDTO.class));
    }

    @Test
    void updateOwnProfileWithBusinessErrorReturns400() throws Exception {
        UserUpdateDTO request = new UserUpdateDTO();
        request.setPhones(java.util.List.of(TestData.phoneDTO("3001234567", false)));
        when(userService.updateOwnProfile(eq(userId), any(UserUpdateDTO.class)))
                .thenThrow(new BusinessRuleException("Debe haber exactamente un teléfono principal"));

        mockMvc.perform(patch("/api/users/me").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addRoleReturns200() throws Exception {
        AddRoleDTO request = new AddRoleDTO();
        request.setRole(RoleName.CONTRATANTE);
        when(userService.addRole(userId, RoleName.CONTRATANTE)).thenReturn(ownProfile());

        mockMvc.perform(post("/api/users/me/roles").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON).content(json(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addRoleWithoutRoleReturns400() throws Exception {
        mockMvc.perform(post("/api/users/me/roles").header(AUTH, token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- perfil público ----------

    @Test
    void getPublicProfileReturns200() throws Exception {
        UUID otherId = UUID.randomUUID();
        PublicProfileDTO dto = new PublicProfileDTO();
        dto.setId(otherId);
        dto.setName("Luis");
        when(userService.getPublicProfile(otherId)).thenReturn(dto);

        mockMvc.perform(get("/api/users/{id}", otherId).header(AUTH, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void getPublicProfileNotFoundReturns404() throws Exception {
        UUID otherId = UUID.randomUUID();
        when(userService.getPublicProfile(otherId)).thenThrow(new ResourceNotFoundException("No existe"));

        mockMvc.perform(get("/api/users/{id}", otherId).header(AUTH, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicProfileWithInvalidUuidReturns400() throws Exception {
        mockMvc.perform(get("/api/users/no-es-uuid").header(AUTH, token))
                .andExpect(status().isBadRequest());
    }
}
