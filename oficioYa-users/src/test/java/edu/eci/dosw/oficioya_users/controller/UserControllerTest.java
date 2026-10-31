package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.security.SecurityConfig;
import edu.eci.dosw.oficioya_users.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba los endpoints con la seguridad JWT real (filtro + reglas de acceso). */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    private static final String SECRET = "clave-de-pruebas-unitarias-con-minimo-32-caracteres";
    private static final String AUTH = "Authorization";
    private static final String REGISTER_BODY = """
            {"name":"Ana","email":"ana@mail.com",
             "phones":[{"number":"3001234567","principal":true}],"roles":["TRABAJADOR"]}""";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    private static String bearer(String subject, Object roles, String secret, long ttlMillis) {
        return "Bearer " + Jwts.builder()
                .subject(subject)
                .claim("roles", roles)
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String token(Object roles) {
        return bearer(userId.toString(), roles, SECRET, 60_000);
    }

    private static UserResponseDTO response(String name) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setName(name);
        return dto;
    }

    // ---------- seguridad ----------

    @Test
    void registerIsPublic() throws Exception {
        when(userService.register(any(UserRequestDTO.class))).thenReturn(response("Ana"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        verifyNoInteractions(userService);
    }

    @Test
    void invalidTokensReturn401() throws Exception {
        mockMvc.perform(get("/api/users/me").header(AUTH, "Bearer token.invalido"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me").header(AUTH, bearer(userId.toString(), "ADMIN", SECRET, -60_000)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me")
                        .header(AUTH, bearer(userId.toString(), "ADMIN", "otra-clave-distinta-de-al-menos-32-caracteres", 60_000)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me").header(AUTH, bearer("no-es-uuid", "ADMIN", SECRET, 60_000)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me").header(AUTH, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(userService);
    }

    @Test
    void nonAdminCannotUseAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users").header(AUTH, token(List.of("TRABAJADOR"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users").header(AUTH, token(null)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(userService);
    }

    // ---------- usuario ----------

    @Test
    void registerWithInvalidDataReturns400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"no-es-correo\",\"phones\":[],\"roles\":[]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }

    @Test
    void ownProfileUsesIdFromToken() throws Exception {
        when(userService.getOwnProfile(userId)).thenReturn(response("Ana"));

        mockMvc.perform(get("/api/users/me").header(AUTH, token("TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void updateOwnProfile() throws Exception {
        when(userService.updateOwnProfile(eq(userId), any(UserUpdateDTO.class))).thenReturn(response("Ana María"));

        mockMvc.perform(patch("/api/users/me").header(AUTH, token("TRABAJADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Ana María\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana María"));
    }

    @Test
    void addRole() throws Exception {
        when(userService.addRole(userId, RoleName.CONTRATANTE)).thenReturn(response("Ana"));

        mockMvc.perform(post("/api/users/me/roles/CONTRATANTE").header(AUTH, token("TRABAJADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void publicProfile() throws Exception {
        UUID otherId = UUID.randomUUID();
        when(userService.getPublicProfile(otherId)).thenReturn(response("Luis"));

        mockMvc.perform(get("/api/users/{id}", otherId).header(AUTH, token("CONTRATANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis"));
    }

    // ---------- administrador ----------

    @Test
    void adminListsAndSeesUsers() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findAll()).thenReturn(List.of(response("Ana")));
        when(userService.findById(id)).thenReturn(response("Luis"));

        mockMvc.perform(get("/api/admin/users").header(AUTH, token(List.of("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana"));
        mockMvc.perform(get("/api/admin/users/{id}", id).header(AUTH, token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luis"));
    }

    @Test
    void adminDeactivatesAndActivatesWithReason() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDTO inactive = response("Ana");
        inactive.setAccountStatus(AccountStatus.INACTIVE);
        UserResponseDTO active = response("Ana");
        active.setAccountStatus(AccountStatus.ACTIVE);
        when(userService.changeStatus(userId, id, AccountStatus.INACTIVE, "Mal uso")).thenReturn(inactive);
        when(userService.changeStatus(userId, id, AccountStatus.ACTIVE, "Resuelto")).thenReturn(active);

        mockMvc.perform(patch("/api/admin/users/{id}/deactivate", id).param("reason", "Mal uso")
                        .header(AUTH, token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("INACTIVE"));
        mockMvc.perform(patch("/api/admin/users/{id}/activate", id).param("reason", "Resuelto")
                        .header(AUTH, token("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
    }

    @Test
    void deactivateWithoutReasonReturns400() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{id}/deactivate", UUID.randomUUID()).header(AUTH, token("ADMIN")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userService);
    }
}
