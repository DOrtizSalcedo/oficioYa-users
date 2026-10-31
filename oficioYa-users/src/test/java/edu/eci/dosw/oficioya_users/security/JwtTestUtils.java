package edu.eci.dosw.oficioya_users.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Genera tokens como los que emitiría Identity, solo para pruebas. */
public final class JwtTestUtils {

    /** Debe coincidir con jwt.secret de src/test/resources/application.properties. */
    public static final String SECRET = "clave-de-pruebas-unitarias-con-minimo-32-caracteres";

    private JwtTestUtils() {
    }

    public static String token(String subject, Object roles) {
        return token(SECRET, subject, roles, 60_000);
    }

    public static String token(String secret, String subject, Object roles, long ttlMillis) {
        var builder = Jwts.builder()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)));
        if (subject != null) {
            builder.subject(subject);
        }
        if (roles != null) {
            builder.claim("roles", roles);
        }
        return builder.compact();
    }

    public static String bearer(String subject, Object roles) {
        return "Bearer " + token(subject, roles);
    }
}
