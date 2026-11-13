package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.ZoneDTO;
import edu.eci.dosw.oficioya_users.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@Tag(name = "Zonas", description = "Catálogo de barrios donde trabajan los trabajadores")
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    @Operation(summary = "Consultar zonas",
            description = "RF-09. Sin ids devuelve todo el catálogo; con ids devuelve solo las que existen (máximo 50). No requiere token")
    public List<ZoneDTO> findZones(
            @Parameter(description = "Ids separados por coma, por ejemplo 1,2", example = "1,2")
            @RequestParam(required = false) List<Long> ids) {
        return ids == null || ids.isEmpty() ? zoneService.findAll() : zoneService.findByIds(ids);
    }
}
