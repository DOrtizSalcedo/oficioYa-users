package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserResponseDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.enums.AccountStatus;
import edu.eci.dosw.oficioya_users.enums.RoleName;
import edu.eci.dosw.oficioya_users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints del microservicio. El id del usuario autenticado sale del JWT (@AuthenticationPrincipal).
 * Las rutas /api/admin/** solo son accesibles con rol ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody UserRequestDTO request) {
        return userService.register(request);
    }

    @GetMapping("/users/me")
    public UserResponseDTO getOwnProfile(@AuthenticationPrincipal UUID userId) {
        return userService.getOwnProfile(userId);
    }

    @PatchMapping("/users/me")
    public UserResponseDTO updateOwnProfile(@AuthenticationPrincipal UUID userId,
                                            @Valid @RequestBody UserUpdateDTO request) {
        return userService.updateOwnProfile(userId, request);
    }

    @PostMapping("/users/me/roles/{role}")
    public UserResponseDTO addRole(@AuthenticationPrincipal UUID userId, @PathVariable RoleName role) {
        return userService.addRole(userId, role);
    }

    @GetMapping("/users/{id}")
    public UserResponseDTO getPublicProfile(@PathVariable UUID id) {
        return userService.getPublicProfile(id);
    }

    @GetMapping("/admin/users")
    public List<UserResponseDTO> findAll() {
        return userService.findAll();
    }

    @GetMapping("/admin/users/{id}")
    public UserResponseDTO findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PatchMapping("/admin/users/{id}/deactivate")
    public UserResponseDTO deactivate(@AuthenticationPrincipal UUID adminId, @PathVariable UUID id,
                                      @RequestParam String reason) {
        return userService.changeStatus(adminId, id, AccountStatus.INACTIVE, reason);
    }

    @PatchMapping("/admin/users/{id}/activate")
    public UserResponseDTO activate(@AuthenticationPrincipal UUID adminId, @PathVariable UUID id,
                                    @RequestParam String reason) {
        return userService.changeStatus(adminId, id, AccountStatus.ACTIVE, reason);
    }
}
