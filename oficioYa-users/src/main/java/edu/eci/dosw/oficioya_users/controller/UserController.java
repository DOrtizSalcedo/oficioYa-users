package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.AddRoleDTO;
import edu.eci.dosw.oficioya_users.dto.OwnProfileDTO;
import edu.eci.dosw.oficioya_users.dto.PublicProfileDTO;
import edu.eci.dosw.oficioya_users.dto.UserRequestDTO;
import edu.eci.dosw.oficioya_users.dto.UserUpdateDTO;
import edu.eci.dosw.oficioya_users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints del usuario autenticado y perfiles públicos.
 * El id del usuario se toma del JWT (subject), nunca del cliente.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<OwnProfileDTO> register(@Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<OwnProfileDTO> getOwnProfile(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(userService.getOwnProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<OwnProfileDTO> updateOwnProfile(@AuthenticationPrincipal UUID userId,
                                                          @Valid @RequestBody UserUpdateDTO request) {
        return ResponseEntity.ok(userService.updateOwnProfile(userId, request));
    }

    @PostMapping("/me/roles")
    public ResponseEntity<OwnProfileDTO> addRole(@AuthenticationPrincipal UUID userId,
                                                 @Valid @RequestBody AddRoleDTO request) {
        return ResponseEntity.ok(userService.addRole(userId, request.getRole()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getPublicProfile(id));
    }
}