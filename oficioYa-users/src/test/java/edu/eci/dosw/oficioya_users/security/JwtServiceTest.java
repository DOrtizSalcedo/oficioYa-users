package edu.eci.dosw.oficioya_users.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(JwtTestUtils.SECRET, "roles");

    @Test
    void parsesValidTokenWithRoleList() {
        UUID id = UUID.randomUUID();
        Claims claims = jwtService.parse(JwtTestUtils.token(id.toString(), List.of("ADMIN", "ROLE_TRABAJADOR")));

        assertThat(jwtService.getUserId(claims)).isEqualTo(id);
        assertThat(jwtService.getAuthorities(claims)).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN", "ROLE_TRABAJADOR");
    }

    @Test
    void acceptsSingleRoleAsText() {
        Claims claims = jwtService.parse(JwtTestUtils.token(UUID.randomUUID().toString(), "CONTRATANTE"));

        assertThat(jwtService.getAuthorities(claims)).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CONTRATANTE");
    }

    @Test
    void tokenWithoutRolesHasNoAuthorities() {
        Claims claims = jwtService.parse(JwtTestUtils.token(UUID.randomUUID().toString(), null));

        assertThat(jwtService.getAuthorities(claims)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        String token = JwtTestUtils.token(JwtTestUtils.SECRET, UUID.randomUUID().toString(), "ADMIN", -60_000);

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String token = JwtTestUtils.token("otra-clave-distinta-de-al-menos-32-caracteres!!",
                UUID.randomUUID().toString(), "ADMIN", 60_000);

        assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsSubjectThatIsNotUuid() {
        Claims claims = jwtService.parse(JwtTestUtils.token("no-es-uuid", "ADMIN"));

        assertThatThrownBy(() -> jwtService.getUserId(claims)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMissingSubject() {
        Claims claims = jwtService.parse(JwtTestUtils.token(null, "ADMIN"));

        assertThatThrownBy(() -> jwtService.getUserId(claims)).isInstanceOf(JwtException.class);
    }
}
