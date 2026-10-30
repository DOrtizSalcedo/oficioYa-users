package edu.eci.dosw.oficioya_users;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class OficioyaUsersApplication {

    public static void main(String[] args) {
        SpringApplication.run(OficioyaUsersApplication.class, args);
    }
}