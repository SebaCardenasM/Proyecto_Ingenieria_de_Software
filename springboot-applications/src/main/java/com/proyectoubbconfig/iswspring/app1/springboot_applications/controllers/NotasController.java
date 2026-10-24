package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.NotaEstudianteFila;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.NotasFiltro;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.NotasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/practicas/notas")
public class NotasController {

    @Autowired
    private NotasService notasService;

    @GetMapping
    public String verNotas(@RequestParam Map<String, String> params, Model model) {
        NotasFiltro filtro = NotasFiltro.desde(params);
        List<NotaEstudianteFila> filas = notasService.obtenerFilas(filtro);

        model.addAttribute("filtro", filtro);
        model.addAttribute("filas", filas);
        model.addAttribute("total", filas.size());
        model.addAttribute("promedios", notasService.promedios(filas));
        return "Coordinador/notasEstudiantiles";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportar(@RequestParam Map<String, String> params) {
        NotasFiltro filtro = NotasFiltro.desde(params);
        List<NotaEstudianteFila> filas = notasService.obtenerFilas(filtro);

        // BOM al inicio para que Excel respete UTF-8 (tildes y ñ)
        byte[] bytes = ("\uFEFF" + notasService.generarCsv(filas)).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"notas_estudiantiles.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @Autowired
    private UsuarioRepository usuarioRepository;
    private void cargarDatosUsuario(Model model, Authentication authentication) {
        usuarioRepository.findByRut(authentication.getName()).ifPresent(u -> {
            model.addAttribute("usuarioActivo", u);
            model.addAttribute("rolesUsuario", u.getRoles().stream().map(Enum::name).toList());
        });
    }
}