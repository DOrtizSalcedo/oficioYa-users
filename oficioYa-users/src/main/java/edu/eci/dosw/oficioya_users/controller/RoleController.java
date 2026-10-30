package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.RoleDTO;
import edu.eci.dosw.oficioya_users.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.findById(id));
    }
}
