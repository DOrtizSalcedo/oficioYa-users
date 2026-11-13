package edu.eci.dosw.oficioya_users.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectMapper objectMapper,
                                                   @Value("${jwt.secret}") String secret,
                                                   @Value("${jwt.roles-claim}") String rolesClaim) throws Exception {
        JsonSecurityErrorHandler errorHandler = new JsonSecurityErrorHandler(objectMapper);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("SERVICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/{id}").hasRole("SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/by-email").hasRole("SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}/account").hasRole("SERVICE")
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/zones").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .addFilterBefore(new JwtAuthenticationFilter(secret, rolesClaim), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
