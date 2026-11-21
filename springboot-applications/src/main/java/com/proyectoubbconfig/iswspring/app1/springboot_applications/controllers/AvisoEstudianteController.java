package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.AvisoVista;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Notificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.NotificacionRepository;

/** Avisos del estudiante (acceso: ROLE_ESTUDIANTE por /estudiante/** en SecurityConfig). */
@Controller
public class AvisoEstudianteController {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @GetMapping("/estudiante/avisos")
    @Transactional
    public String verAvisos(Model model, Principal principal) {
        List<Notificacion> notificaciones = notificacionRepository.findByEstudianteRutOrderByFechaDesc(principal.getName());
        LocalDateTime ahora = LocalDateTime.now();

        List<AvisoVista> avisos = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            EnlaceEvaluacion enlace = n.getEnlace();
            String estado = null;
            String token = null;
            LocalDateTime expira = null;
            if (enlace != null) {
                token = enlace.getToken();
                expira = enlace.getFechaExpiracion();
                if (enlace.isUsado()) {
                    estado = "USADO";
                } else if (ahora.isAfter(expira)) {
                    estado = "EXPIRADO";
                } else {
                    estado = "VIGENTE";
                }
            }
            avisos.add(new AvisoVista(n.getId(), n.getTitulo(), n.getMensaje(), n.getFecha(),
                    !n.isLeida(), token, estado, expira));

            // Al abrir la página los avisos pasan a "leídos" (los nuevos se siguen marcando en pantalla)
            if (!n.isLeida()) {
                n.setLeida(true);
            }
        }
        notificacionRepository.saveAll(notificaciones);

        model.addAttribute("avisos", avisos);
        return "Estudiante/avisos";
    }
}
