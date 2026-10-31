package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.security.JwtService;
import edu.eci.dosw.oficioya_users.security.SecurityConfig;
import edu.eci.dosw.oficioya_users.security.SecurityErrorHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Carga la seguridad real en las pruebas @WebMvcTest.
 * JwtAuthenticationFilter no se importa porque @WebMvcTest ya incluye los beans de tipo Filter.
 */
@TestConfiguration
@Import({SecurityConfig.class, JwtService.class, SecurityErrorHandler.class})
public class WebSecurityTestConfig {
}
