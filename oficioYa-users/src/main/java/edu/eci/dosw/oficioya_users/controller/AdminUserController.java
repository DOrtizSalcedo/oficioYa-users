package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.AdminUserDTO;
import edu.eci.dosw.oficioya_users.dto.StatusChangeDTO;
import edu.eci.dosw.oficioya_users.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Endpoints exclusivos del administrador. El id del admin se toma del JWT. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDTO>> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.findById(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<AdminUserDTO> deactivate(@AuthenticationPrincipal UUID adminId,
                                                   @PathVariable UUID id,
                                                   @Valid @RequestBody StatusChangeDTO request) {
        return ResponseEntity.ok(adminUserService.deactivate(adminId, id, request.getReason()));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AdminUserDTO> activate(@AuthenticationPrincipal UUID adminId,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody StatusChangeDTO request) {
        return ResponseEntity.ok(adminUserService.activate(adminId, id, request.getReason()));
    }
}