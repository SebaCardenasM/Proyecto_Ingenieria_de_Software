package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.SeccionPautaDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.PlantillaPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlantillaPautaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PlantillaPautaServiceTest {

    @Autowired
    private PlantillaPautaService plantillaService;

    @Test
    void testPlantillasPorDefecto() {
        PlantillaPauta esp = plantillaService.obtenerOAsegurarPlantilla(TipoPauta.ESPECIALIDAD);
        assertNotNull(esp);
        assertEquals(TipoPauta.ESPECIALIDAD, esp.getTipoPauta());
        assertEquals(20, plantillaService.obtenerTotalCriterios(TipoPauta.ESPECIALIDAD));
        assertEquals(140, plantillaService.obtenerPuntajeMaximo(TipoPauta.ESPECIALIDAD));

        PlantillaPauta ori = plantillaService.obtenerOAsegurarPlantilla(TipoPauta.ORIENTACION);
        assertNotNull(ori);
        assertEquals(TipoPauta.ORIENTACION, ori.getTipoPauta());
        assertEquals(18, plantillaService.obtenerTotalCriterios(TipoPauta.ORIENTACION));
        assertEquals(126, plantillaService.obtenerPuntajeMaximo(TipoPauta.ORIENTACION));
    }

    @Test
    void testParsearSecciones() {
        String json = "[{\"titulo\":\"SECCIÓN PRUEBA\",\"items\":[\"Criterio 1\",\"Criterio 2\"]}]";
        List<SeccionPautaDTO> secciones = plantillaService.parsearSecciones(json);
        assertEquals(1, secciones.size());
        assertEquals("SECCIÓN PRUEBA", secciones.get(0).getTitulo());
        assertEquals(2, secciones.get(0).getItems().size());
    }

    @Test
    void testGuardarBorradorYDefinitivo() {
        String jsonNuevo = "[{\"titulo\":\"APARTADO MODIFICADO\",\"items\":[\"Nuevo criterio A\",\"Nuevo criterio B\",\"Nuevo criterio C\"]}]";

        // 1. Guardar como borrador
        plantillaService.guardarBorrador(TipoPauta.ESPECIALIDAD, jsonNuevo, "TEST_USER");
        PlantillaPauta espConBorrador = plantillaService.obtenerOAsegurarPlantilla(TipoPauta.ESPECIALIDAD);
        assertTrue(espConBorrador.isTieneBorrador());
        assertEquals(jsonNuevo, espConBorrador.getEsquemaBorradorJson());
        // El esquema activo NO debe cambiar al guardar solo borrador:
        assertEquals(20, plantillaService.obtenerTotalCriterios(TipoPauta.ESPECIALIDAD));

        // 2. Guardar definitivo (publicar)
        plantillaService.guardarDefinitivo(TipoPauta.ESPECIALIDAD, jsonNuevo, "TEST_USER");
        PlantillaPauta espPublicada = plantillaService.obtenerOAsegurarPlantilla(TipoPauta.ESPECIALIDAD);
        assertFalse(espPublicada.isTieneBorrador());
        assertNull(espPublicada.getEsquemaBorradorJson());
        // Ahora el esquema activo cambió a 3 criterios:
        assertEquals(3, plantillaService.obtenerTotalCriterios(TipoPauta.ESPECIALIDAD));
        assertEquals(21, plantillaService.obtenerPuntajeMaximo(TipoPauta.ESPECIALIDAD));

        // 3. Restablecer por defecto
        plantillaService.restablecerPorDefecto(TipoPauta.ESPECIALIDAD, "TEST_USER");
        assertEquals(20, plantillaService.obtenerTotalCriterios(TipoPauta.ESPECIALIDAD));
        assertEquals(140, plantillaService.obtenerPuntajeMaximo(TipoPauta.ESPECIALIDAD));
    }

    @Test
    void testValidacionEsquemaInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            plantillaService.guardarDefinitivo(TipoPauta.ESPECIALIDAD, "", "USER");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            plantillaService.guardarDefinitivo(TipoPauta.ESPECIALIDAD, "[]", "USER");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            plantillaService.guardarDefinitivo(TipoPauta.ESPECIALIDAD, "[{\"titulo\":\"\",\"items\":[\"Criterio\"]}]", "USER");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            plantillaService.guardarDefinitivo(TipoPauta.ESPECIALIDAD, "[{\"titulo\":\"Apartado\",\"items\":[]}]", "USER");
        });
    }
}
