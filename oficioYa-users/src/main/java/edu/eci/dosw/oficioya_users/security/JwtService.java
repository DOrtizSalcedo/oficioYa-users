package edu.eci.dosw.oficioya_users.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Valida los JWT emitidos por el microservicio de Identity (HS256).
 * Este servicio no genera tokens: solo verifica firma y expiración y extrae id y roles.
 */
@Service
public class JwtService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final SecretKey key;
    private final String rolesClaim;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.roles-claim:roles}") String rolesClaim) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.rolesClaim = rolesClaim;
    }

    /** Devuelve los claims si la firma y la expiración son válidas; lanza JwtException si no. */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    /** El subject (sub) del token es el UUID del usuario. */
    public UUID getUserId(Claims claims) {
        try {
            return UUID.fromString(claims.getSubject());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new JwtException("El subject del token no es un UUID válido");
        }
    }

    /** Acepta el claim como lista ("roles": ["ADMIN"]) o como texto ("role": "ADMIN"), con o sin prefijo ROLE_. */
    public List<GrantedAuthority> getAuthorities(Claims claims) {
        Object value = claims.get(rolesClaim);
        List<String> roles;
        if (value instanceof Collection<?> collection) {
            roles = collection.stream().map(String::valueOf).toList();
        } else if (value instanceof String role) {
            roles = List.of(role);
        } else {
            roles = List.of();
        }
        return roles.stream()
                .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .toList();
    }
}