package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Dupla;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DuplaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DuplaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DuplaServiceTest {

    @Autowired
    private DuplaService duplaService;

    @Autowired
    private DuplaRepository duplaRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Test
    void testPermiteDuplas() {
        assertTrue(duplaService.permiteDuplas(3), "Práctica 3 debe permitir duplas");
        assertTrue(duplaService.permiteDuplas(4), "Práctica 4 debe permitir duplas");
        assertFalse(duplaService.permiteDuplas(1), "Práctica 1 no permite duplas");
        assertFalse(duplaService.permiteDuplas(2), "Práctica 2 no permite duplas");
        assertFalse(duplaService.permiteDuplas(5), "Práctica 5 no permite duplas");
        assertFalse(duplaService.permiteDuplas(6), "Práctica 6 no permite duplas");
        assertFalse(duplaService.permiteDuplas(null), "Null no permite duplas");
    }

    @Test
    void testListarDuplasProfesorSinDuplas() {
        List<Dupla> duplas = duplaService.listarDuplasPorProfesorYPractica("RUT_INEXISTENTE", 3);
        assertNotNull(duplas);
        assertTrue(duplas.isEmpty());
    }

    @Test
    void testValidacionCrearDuplaMismaPractica() {
        assertThrows(IllegalArgumentException.class, () -> {
            duplaService.crearDupla("123", 1L, 1L, 3);
        }, "No se puede seleccionar el mismo estudiante");
    }

    @Test
    void testValidacionCrearDuplaEnPracticaNoPermitida() {
        assertThrows(IllegalArgumentException.class, () -> {
            duplaService.crearDupla("123", 1L, 2L, 5);
        }, "Práctica 5 no permite duplas");
    }
}
