package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers.EstudianteController;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PautaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EstudianteControllerPautaTest {

    @Autowired
    private EstudianteController estudianteController;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void testVerPautaOnlineColaboradorSinError() {
        // Buscar pautas del estudiante Sebastian Eduardo Cardenas (21.756.104-1)
        List<Pauta> pautas = pautaRepository.findAll().stream()
                .filter(p -> p.getEstudiante() != null && "21.756.104-1".equals(p.getEstudiante().getRut()))
                .filter(p -> "ONLINE_ENVIADA".equals(p.getRutaServidor()))
                .toList();

        if (!pautas.isEmpty()) {
            Pauta p = pautas.get(0);
            Model model = new ConcurrentModel();
            Principal principal = () -> "21.756.104-1";

            String vista = estudianteController.verPautaOnline(p.getId(), model, principal);
            assertEquals("Colaborador/evaluacion-colaborador", vista);
            assertEquals(true, model.getAttribute("modoLectura"));
            assertEquals(false, model.getAttribute("todasCompletadas"));
            assertEquals(true, model.getAttribute("vistaEstudiante"));
            assertNotNull(model.getAttribute("datos"));
            assertNotNull(model.getAttribute("pauta"));
        }
    }
}
