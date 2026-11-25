package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import static org.junit.jupiter.api.Assertions.*;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlanificacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;

import java.time.LocalDateTime;

@SpringBootTest
@Transactional
class DocumentoServicePruebaTest {

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private PlanificacionRepository planificacionRepository;

    @Test
    void testServiceNotNull() {
        assertNotNull(documentoService, "El servicio real DocumentoService debe inyectarse correctamente");
    }

    @Test
    void testGuardarPlanificacionConEstadoAutorizadoYObservaciones() {
        for (EstadoDocumento estado : EstadoDocumento.values()) {
            Planificacion p = new Planificacion();
            p.setNombreArchivo("test-" + estado.name().toLowerCase() + ".pdf");
            p.setRutaServidor("uploads/test-" + estado.name().toLowerCase() + ".pdf");
            p.setTipoDocumento(TipoDocumento.PLANIFICACION);
            p.setFechaSubida(LocalDateTime.now());
            p.setEstado(estado);

            Planificacion guardada = planificacionRepository.save(p);
            assertNotNull(guardada.getId(), "La planificación debe guardarse correctamente con estado " + estado);
            assertEquals(estado, guardada.getEstado(), "El estado recuperado debe coincidir con " + estado);
        }
    }
}