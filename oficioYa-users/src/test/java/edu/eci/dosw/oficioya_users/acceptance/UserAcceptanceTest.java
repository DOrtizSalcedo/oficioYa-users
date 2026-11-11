package edu.eci.dosw.oficioya_users.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de aceptación: recorren cada requisito funcional (RF-01 a RF-08).
 * Los criterios están documentados en docs/requirements/acceptance-criteria.md.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:oficioya_acceptance;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "admin.email=admin@oficioya.com",
        "admin.name=Administrador",
        "admin.phone=3000000000"
})
@DisplayName("Pruebas de aceptación del microservicio de usuarios")
class UserAcceptanceTest {

    private static final String AUTH = "Authorization";
    private static final String USERS = "/api/v1/users";
    private static final String ME = USERS + "/me";
    private static final String ADMIN_USERS = "/api/v1/admin/users";
    private static final String ADMIN_EMAIL = "admin@oficioya.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${jwt.secret}")
    private String secret;

    private UUID adminId;

    @BeforeEach
    void findSeededAdmin() throws Exception {
        String body = mockMvc.perform(get(ADMIN_USERS).header(AUTH, token(UUID.randomUUID(), "ADMIN")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode user : objectMapper.readTree(body)) {
            if (ADMIN_EMAIL.equals(user.get("email").asText())) {
                adminId = UUID.fromString(user.get("id").asText());
            }
        }
    }

    // RF-01

    @Nested
    @DisplayName("RF-01 Registrar cuenta")
    class Register {

        @Test
        @DisplayName("Dado un correo nuevo, cuando me registro con datos válidos, entonces la cuenta queda activa con el rol elegido")
        void registersActiveAccount() throws Exception {
            mockMvc.perform(registerRequest(registerBody("Ana", "ana.rf01@mail.com", "CONTRATANTE")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Ana"))
                    .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                    .andExpect(jsonPath("$.roles", containsInAnyOrder("CONTRATANTE")))
                    .andExpect(jsonPath("$.email").value("*****@mail.com"));
        }

        @Test
        @DisplayName("Dado un correo nuevo, cuando me registro como trabajador y contratante, entonces la cuenta tiene ambos roles")
        void registersWithBothRoles() throws Exception {
            mockMvc.perform(registerRequest(registerBody("Luis", "luis.rf01@mail.com", "TRABAJADOR", "CONTRATANTE")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.roles", containsInAnyOrder("TRABAJADOR", "CONTRATANTE")));
        }

        @Test
        @DisplayName("Dado un correo ya registrado, cuando intento registrarme con él, entonces recibo 409")
        void rejectsDuplicatedEmail() throws Exception {
            register("Ana", "repetido.rf01@mail.com", "CONTRATANTE");

            mockMvc.perform(registerRequest(registerBody("Otra", "REPETIDO.rf01@mail.com", "TRABAJADOR")))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Cuando intento registrarme con el rol ADMIN, entonces recibo 400")
        void rejectsAdminRole() throws Exception {
            mockMvc.perform(registerRequest(registerBody("Eva", "eva.rf01@mail.com", "ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Cuando me registro con dos teléfonos principales, entonces recibo 400")
        void rejectsTwoPrincipalPhones() throws Exception {
            String body = """
                    {"name":"Juan","email":"juan.rf01@mail.com",
                     "phones":[{"number":"3001111111","principal":true},{"number":"3002222222","principal":true}],
                     "roles":["TRABAJADOR"]}""";

            mockMvc.perform(registerRequest(body)).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Cuando me registro con más de tres teléfonos o datos inválidos, entonces recibo 400")
        void rejectsInvalidData() throws Exception {
            String tooManyPhones = """
                    {"name":"Juan","email":"juan2.rf01@mail.com",
                     "phones":[{"number":"3001111111","principal":true},{"number":"3002222222","principal":false},
                               {"number":"3003333333","principal":false},{"number":"3004444444","principal":false}],
                     "roles":["TRABAJADOR"]}""";

            mockMvc.perform(registerRequest(tooManyPhones)).andExpect(status().isBadRequest());
            mockMvc.perform(registerRequest("{\"name\":\"\",\"email\":\"no-es-correo\",\"phones\":[],\"roles\":[]}"))
                    .andExpect(status().isBadRequest());
        }
    }

    // RF-02

    @Nested
    @DisplayName("RF-02 Agregar rol a la cuenta")
    class AddRole {

        @Test
        @DisplayName("Dado un contratante activo, cuando agrega el rol trabajador, entonces su cuenta tiene ambos roles")
        void addsMissingRole() throws Exception {
            UUID id = register("Carla", "carla.rf02@mail.com", "CONTRATANTE");

            mockMvc.perform(post(ME + "/roles/TRABAJADOR").header(AUTH, token(id, "CONTRATANTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.roles", containsInAnyOrder("CONTRATANTE", "TRABAJADOR")));
        }

        @Test
        @DisplayName("Dado un usuario que ya tiene el rol, cuando lo vuelve a agregar, entonces recibe 409")
        void rejectsRepeatedRole() throws Exception {
            UUID id = register("Pedro", "pedro.rf02@mail.com", "TRABAJADOR");

            mockMvc.perform(post(ME + "/roles/TRABAJADOR").header(AUTH, token(id, "TRABAJADOR")))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Cuando un usuario intenta agregarse el rol ADMIN, entonces recibe 400")
        void rejectsAdminRole() throws Exception {
            UUID id = register("Sara", "sara.rf02@mail.com", "TRABAJADOR");

            mockMvc.perform(post(ME + "/roles/ADMIN").header(AUTH, token(id, "TRABAJADOR")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Dado el administrador, cuando intenta agregarse un rol, entonces recibe 403")
        void adminCannotAddRoles() throws Exception {
            mockMvc.perform(post(ME + "/roles/TRABAJADOR").header(AUTH, token(adminId, "ADMIN")))
                    .andExpect(status().isForbidden());
        }
    }

    // RF-03

    @Nested
    @DisplayName("RF-03 Consultar perfil propio")
    class OwnProfile {

        @Test
        @DisplayName("Dado un usuario activo, cuando consulta su perfil, entonces ve sus datos con correo y teléfono enmascarados")
        void showsMaskedOwnProfile() throws Exception {
            UUID id = register("Marta", "marta.rf03@mail.com", "TRABAJADOR");

            mockMvc.perform(get(ME).header(AUTH, token(id, "TRABAJADOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Marta"))
                    .andExpect(jsonPath("$.email").value("*****@mail.com"))
                    .andExpect(jsonPath("$.phones[0].number").value("******4567"))
                    .andExpect(jsonPath("$.phones[0].principal").value(true))
                    .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));
        }

        @Test
        @DisplayName("Cuando consulto el perfil sin token, entonces recibo 401")
        void requiresToken() throws Exception {
            mockMvc.perform(get(ME)).andExpect(status().isUnauthorized());
        }
    }

    // RF-04

    @Nested
    @DisplayName("RF-04 Editar perfil propio")
    class UpdateProfile {

        @Test
        @DisplayName("Dado un usuario activo, cuando cambia su nombre y teléfonos, entonces quedan guardados solo los nuevos")
        void updatesNameAndPhones() throws Exception {
            UUID id = register("Jorge", "jorge.rf04@mail.com", "TRABAJADOR");
            String update = """
                    {"name":"Jorge Andrés",
                     "phones":[{"number":"3109998888","principal":true},{"number":"3107776666","principal":false}]}""";

            mockMvc.perform(json(patch(ME).header(AUTH, token(id, "TRABAJADOR")), update))
                    .andExpect(status().isOk());

            mockMvc.perform(get(ADMIN_USERS + "/" + id).header(AUTH, token(adminId, "ADMIN")))
                    .andExpect(jsonPath("$.name").value("Jorge Andrés"))
                    .andExpect(jsonPath("$.email").value("jorge.rf04@mail.com"))
                    .andExpect(jsonPath("$.phones", hasSize(2)))
                    .andExpect(jsonPath("$.phones[*].number", containsInAnyOrder("3109998888", "3107776666")));
        }

        @Test
        @DisplayName("Cuando edita sus teléfonos sin uno principal, entonces recibe 400")
        void rejectsPhonesWithoutPrincipal() throws Exception {
            UUID id = register("Laura", "laura.rf04@mail.com", "TRABAJADOR");
            String update = "{\"phones\":[{\"number\":\"3109998888\",\"principal\":false}]}";

            mockMvc.perform(json(patch(ME).header(AUTH, token(id, "TRABAJADOR")), update))
                    .andExpect(status().isBadRequest());
        }
    }

    // RF-05

    @Nested
    @DisplayName("RF-05 Consultar perfil público")
    class PublicProfile {

        @Test
        @DisplayName("Dado otro usuario activo, cuando consulto su perfil, entonces solo veo id, nombre y roles")
        void showsOnlyPublicData() throws Exception {
            UUID worker = register("Tomás", "tomas.rf05@mail.com", "TRABAJADOR");
            UUID client = register("Paula", "paula.rf05@mail.com", "CONTRATANTE");

            mockMvc.perform(get(USERS + "/" + worker).header(AUTH, token(client, "CONTRATANTE")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(worker.toString()))
                    .andExpect(jsonPath("$.name").value("Tomás"))
                    .andExpect(jsonPath("$.roles", containsInAnyOrder("TRABAJADOR")))
                    .andExpect(jsonPath("$.email").doesNotExist())
                    .andExpect(jsonPath("$.phones").doesNotExist());
        }

        @Test
        @DisplayName("Dado un usuario desactivado, cuando consulto su perfil público, entonces recibo 404")
        void hidesInactiveUsers() throws Exception {
            UUID worker = register("Rosa", "rosa.rf05@mail.com", "TRABAJADOR");
            UUID client = register("Iván", "ivan.rf05@mail.com", "CONTRATANTE");
            changeStatus(worker, "deactivate");

            mockMvc.perform(get(USERS + "/" + worker).header(AUTH, token(client, "CONTRATANTE")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Dado que mi cuenta está desactivada, cuando consulto un perfil público, entonces recibo 403")
        void inactiveRequesterIsForbidden() throws Exception {
            UUID worker = register("Hugo", "hugo.rf05@mail.com", "TRABAJADOR");
            UUID client = register("Nora", "nora.rf05@mail.com", "CONTRATANTE");
            changeStatus(client, "deactivate");

            mockMvc.perform(get(USERS + "/" + worker).header(AUTH, token(client, "CONTRATANTE")))
                    .andExpect(status().isForbidden());
        }
    }

    // RF-06

    @Nested
    @DisplayName("RF-06 Desactivar cuenta")
    class Deactivate {

        @Test
        @DisplayName("Dado un usuario activo, cuando el admin lo desactiva con motivo, entonces la cuenta queda inactiva y no puede usar su perfil")
        void deactivatesAccount() throws Exception {
            UUID id = register("Diego", "diego.rf06@mail.com", "TRABAJADOR");

            changeStatus(id, "deactivate")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountStatus").value("INACTIVE"));

            mockMvc.perform(get(ME).header(AUTH, token(id, "TRABAJADOR")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Cuando el admin desactiva una cuenta sin motivo, entonces recibe 400")
        void requiresReason() throws Exception {
            UUID id = register("Elena", "elena.rf06@mail.com", "TRABAJADOR");

            mockMvc.perform(json(patch(ADMIN_USERS + "/" + id + "/deactivate").header(AUTH, token(adminId, "ADMIN")),
                            "{\"reason\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Dado una cuenta ya inactiva, cuando el admin la vuelve a desactivar, entonces recibe 409")
        void rejectsAlreadyInactive() throws Exception {
            UUID id = register("Raúl", "raul.rf06@mail.com", "TRABAJADOR");
            changeStatus(id, "deactivate");

            changeStatus(id, "deactivate").andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Cuando el admin intenta desactivar su propia cuenta, entonces recibe 403")
        void adminCannotDeactivateItself() throws Exception {
            changeStatus(adminId, "deactivate").andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Cuando un usuario que no es admin intenta desactivar una cuenta, entonces recibe 403")
        void onlyAdminCanDeactivate() throws Exception {
            UUID id = register("Sofía", "sofia.rf06@mail.com", "TRABAJADOR");

            mockMvc.perform(json(patch(ADMIN_USERS + "/" + id + "/deactivate").header(AUTH, token(id, "TRABAJADOR")),
                            "{\"reason\":\"Prueba\"}"))
                    .andExpect(status().isForbidden());
        }
    }

    // RF-07

    @Nested
    @DisplayName("RF-07 Reactivar cuenta")
    class Activate {

        @Test
        @DisplayName("Dado un usuario inactivo, cuando el admin lo reactiva con motivo, entonces vuelve a usar su perfil")
        void reactivatesAccount() throws Exception {
            UUID id = register("Andrea", "andrea.rf07@mail.com", "CONTRATANTE");
            changeStatus(id, "deactivate");

            changeStatus(id, "activate")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountStatus").value("ACTIVE"));

            mockMvc.perform(get(ME).header(AUTH, token(id, "CONTRATANTE")))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Dado una cuenta ya activa, cuando el admin la reactiva, entonces recibe 409")
        void rejectsAlreadyActive() throws Exception {
            UUID id = register("Felipe", "felipe.rf07@mail.com", "CONTRATANTE");

            changeStatus(id, "activate").andExpect(status().isConflict());
        }
    }

    // RF-08

    @Nested
    @DisplayName("RF-08 Consultar información completa (administrador)")
    class AdminView {

        @Test
        @DisplayName("Dado un usuario registrado, cuando el admin lo consulta, entonces ve todos sus datos sin enmascarar")
        void adminSeesFullData() throws Exception {
            UUID id = register("Camilo", "camilo.rf08@mail.com", "TRABAJADOR");

            mockMvc.perform(get(ADMIN_USERS + "/" + id).header(AUTH, token(adminId, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("camilo.rf08@mail.com"))
                    .andExpect(jsonPath("$.phones[0].number").value("3001234567"))
                    .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("Cuando el admin lista los usuarios, entonces aparecen las cuentas registradas")
        void adminListsUsers() throws Exception {
            register("Valeria", "valeria.rf08@mail.com", "CONTRATANTE");

            mockMvc.perform(get(ADMIN_USERS).header(AUTH, token(adminId, "ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].email", hasItem("valeria.rf08@mail.com")));
        }

        @Test
        @DisplayName("Cuando el admin consulta un id que no existe, entonces recibe 404")
        void unknownUserReturns404() throws Exception {
            mockMvc.perform(get(ADMIN_USERS + "/" + UUID.randomUUID()).header(AUTH, token(adminId, "ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Cuando un usuario que no es admin consulta la información completa, entonces recibe 403")
        void nonAdminIsForbidden() throws Exception {
            UUID id = register("Gloria", "gloria.rf08@mail.com", "TRABAJADOR");

            mockMvc.perform(get(ADMIN_USERS + "/" + id).header(AUTH, token(id, "TRABAJADOR")))
                    .andExpect(status().isForbidden());
        }
    }

    // Utilidades

    private String token(UUID userId, String... roles) {
        return "Bearer " + Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of(roles))
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .compact();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static String registerBody(String name, String email, String... roles) {
        String roleList = String.join("\",\"", roles);
        return """
                {"name":"%s","email":"%s","phones":[{"number":"3001234567","principal":true}],"roles":["%s"]}"""
                .formatted(name, email, roleList);
    }

    private UUID register(String name, String email, String... roles) throws Exception {
        String body = mockMvc.perform(registerRequest(registerBody(name, email, roles)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(body).get("id").asText());
    }

    private ResultActions changeStatus(UUID userId, String action) throws Exception {
        return mockMvc.perform(json(patch(ADMIN_USERS + "/" + userId + "/" + action).header(AUTH, token(adminId, "ADMIN")),
                "{\"reason\":\"Prueba de aceptación\"}"));
    }

    private MockHttpServletRequestBuilder registerRequest(String body) {
        return json(post(USERS), body).header(AUTH, token(UUID.randomUUID(), "SERVICE"));
    }
}
