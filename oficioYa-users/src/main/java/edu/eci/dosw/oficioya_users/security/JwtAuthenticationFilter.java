package edu.eci.dosw.oficioya_users.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;
    private final String rolesClaim;

    public JwtAuthenticationFilter(String secret, String rolesClaim) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.rolesClaim = rolesClaim;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser().verifyWith(key).build()
                        .parseSignedClaims(header.substring(7)).getPayload();
                UUID userId = UUID.fromString(claims.getSubject());
                List<?> roles = claims.get(rolesClaim, List.class);
                List<SimpleGrantedAuthority> authorities = roles == null ? List.of() : roles.stream()
                        .map(String::valueOf)
                        .map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
                        .toList();
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(userId, null, authorities));
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
