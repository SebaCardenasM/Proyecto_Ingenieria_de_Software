package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers.InicioController;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers.PlanificacionOnlineController;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlanificacionOnlineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PlanificacionActualEstudianteTest {

    @Autowired
    private PlanificacionOnlineService planificacionOnlineService;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private InicioController inicioController;

    @Autowired
    private PlanificacionOnlineController planificacionOnlineController;

    @Test
    void testResolverPracticaActualParaEstudianteConPracticasAsignadasPeroSinPracticaActual() {
        // Sebastián (RUT 21.756.184-1) tiene prácticas asignadas (Práctica 1 y Práctica 5)
        // pero su campo practicaActual era null
        Optional<Estudiante> sebaOpt = estudianteRepository.findById("21.756.184-1");
        assertTrue(sebaOpt.isPresent(), "El estudiante Sebastián debe existir en la BD");
        Estudiante seba = sebaOpt.get();

        Integer practicaResuelta = planificacionOnlineService.resolverPracticaActual(seba);
        assertNotNull(practicaResuelta, "No debe retornar null si tiene prácticas asignadas");
        assertEquals(5, practicaResuelta, "Debe resolver a la Práctica 5 que está pendiente en 2026-2");
    }

    @Test
    void testResolverPracticaActualParaEstudianteSinPracticas() {
        // Diego Navarro (22.111.000-5) no tiene prácticas asignadas
        Optional<Estudiante> diegoOpt = estudianteRepository.findById("22.111.000-5");
        if (diegoOpt.isPresent()) {
            Estudiante diego = diegoOpt.get();
            Integer resuelta = planificacionOnlineService.resolverPracticaActual(diego);
            assertNull(resuelta, "Debe ser null para estudiantes que realmente no tienen práctica inscrita");
        }
    }

    @Test
    void testInicioEstudianteModelContienePracticaActual() {
        Authentication auth = new TestingAuthenticationToken("21.756.184-1", "pass", "ROLE_ESTUDIANTE");
        Model model = new ConcurrentModel();

        String vista = inicioController.inicioEstudiante(model, auth);
        assertEquals("Estudiante/indexEstudiante", vista);
        assertTrue(model.containsAttribute("practicaActual"), "El modelo debe contener el atributo practicaActual");
        assertEquals(5, model.getAttribute("practicaActual"), "practicaActual debe ser 5 para Sebastián");
    }

    @Test
    void testAccesoAMisPlanificacionesNoBloqueaAEstudianteConPractica() {
        Principal principal = () -> "21.756.184-1";
        Model model = new ConcurrentModel();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        String vista = planificacionOnlineController.misPlanificaciones(model, principal, ra);
        assertEquals("Estudiante/planificaciones", vista, "No debe redirigir con error al index; debe abrir la vista");
        assertEquals(5, model.getAttribute("practicaActual"));
        assertNotNull(model.getAttribute("infoPractica"));
    }
}
