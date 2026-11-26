package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EnlaceEvaluacionRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionColaboradorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EvaluacionColaboradorServiceTest {

    @Autowired
    private EnlaceEvaluacionRepository enlaceRepository;

    @Autowired
    private EvaluacionColaboradorService colaboradorService;

    @Test
    void testFindByTokenParaActualizarNoLanzaErrorSql() {
        // Verifica que la consulta SQL en MariaDB/MySQL no falle con sintaxis 'for update of'
        Optional<EnlaceEvaluacion> enlaceOpt = enlaceRepository.findByTokenParaActualizar("token-inexistente-test");
        assertTrue(enlaceOpt.isEmpty(), "Para un token inexistente debe retornar vacío sin error SQL");
    }

    @Test
    void testValidarTokenInvalido() {
        EvaluacionColaboradorService.Validacion v = colaboradorService.validar("token-falso");
        assertEquals(EvaluacionColaboradorService.Estado.INVALIDO, v.estado());
    }
}
