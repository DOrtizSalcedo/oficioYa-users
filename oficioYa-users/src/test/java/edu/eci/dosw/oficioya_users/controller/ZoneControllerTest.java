package edu.eci.dosw.oficioya_users.controller;

import edu.eci.dosw.oficioya_users.dto.ZoneDTO;
import edu.eci.dosw.oficioya_users.exception.BadRequestException;
import edu.eci.dosw.oficioya_users.security.SecurityConfig;
import edu.eci.dosw.oficioya_users.service.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Catálogo es público: se prueba sin token y con la seguridad real. */
@WebMvcTest(ZoneController.class)
@Import(SecurityConfig.class)
class ZoneControllerTest {

    private static final String ZONES = "/api/v1/zones";
    private static final ZoneDTO CEDRITOS = new ZoneDTO(1L, "Cedritos", "Usaquén");

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ZoneService zoneService;

    @Test
    void withoutIdsReturnsWholeCatalogWithoutToken() throws Exception {
        when(zoneService.findAll()).thenReturn(List.of(CEDRITOS));

        mockMvc.perform(get(ZONES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Cedritos"))
                .andExpect(jsonPath("$[0].locality").value("Usaquén"));
    }

    @Test
    void emptyIdsReturnsWholeCatalog() throws Exception {
        when(zoneService.findAll()).thenReturn(List.of(CEDRITOS));

        mockMvc.perform(get(ZONES).param("ids", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cedritos"));
        verify(zoneService).findAll();
    }

    @Test
    void withIdsReturnsRequestedZones() throws Exception {
        when(zoneService.findByIds(List.of(1L, 2L))).thenReturn(List.of(CEDRITOS));

        mockMvc.perform(get(ZONES).param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cedritos"));
    }

    @Test
    void tooManyIdsReturns400() throws Exception {
        when(zoneService.findByIds(List.of(1L, 2L)))
                .thenThrow(new BadRequestException("Se pueden consultar máximo 50 zonas por petición"));

        mockMvc.perform(get(ZONES).param("ids", "1,2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Se pueden consultar máximo 50 zonas por petición"));
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get(ZONES).param("ids", "uno"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(zoneService);
    }

    @Test
    void onlyGetIsPublic() throws Exception {
        mockMvc.perform(post(ZONES))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(zoneService);
    }
}
