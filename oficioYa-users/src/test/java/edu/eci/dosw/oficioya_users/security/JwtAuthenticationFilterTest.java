package edu.eci.dosw.oficioya_users.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(new JwtService(JwtTestUtils.SECRET, "roles"));

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Authentication run(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        if (authorizationHeader != null) {
            request.addHeader("Authorization", authorizationHeader);
        }
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        assertThat(chain.getRequest()).isNotNull();
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Test
    void validTokenAuthenticatesWithUserIdAsPrincipal() throws Exception {
        UUID id = UUID.randomUUID();

        Authentication authentication = run(JwtTestUtils.bearer(id.toString(), List.of("TRABAJADOR")));

        assertThat(authentication.getPrincipal()).isEqualTo(id);
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_TRABAJADOR");
    }

    @Test
    void missingHeaderDoesNotAuthenticate() throws Exception {
        assertThat(run(null)).isNull();
    }

    @Test
    void nonBearerHeaderDoesNotAuthenticate() throws Exception {
        assertThat(run("Basic dXNlcjpwYXNz")).isNull();
    }

    @Test
    void invalidTokenDoesNotAuthenticate() throws Exception {
        assertThat(run("Bearer token.invalido.123")).isNull();
    }

    @Test
    void tokenWithInvalidSubjectDoesNotAuthenticate() throws Exception {
        assertThat(run(JwtTestUtils.bearer("no-es-uuid", "ADMIN"))).isNull();
    }
}
