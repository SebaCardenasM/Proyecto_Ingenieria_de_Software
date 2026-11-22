package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Colaborador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ColaboradorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.util.RutUtil;

/** Gestión de profesores colaboradores (acceso: ROLE_COORDINADOR, ver SecurityConfig). */
@Controller
@RequestMapping("/colaboradores")
public class ColaboradorController {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("^[+0-9 ()-]{7,20}$");

    private static final String VISTA_LISTA = "Coordinador/colaboradores";
    private static final String VISTA_REGISTRO = "Coordinador/registro_colaborador";
    private static final String VISTA_EDICION = "Coordinador/editar_colaborador";

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    // ------------------------------------------------------------------ listado

    @GetMapping
    @Transactional(readOnly = true)
    public String listar(Model model) {
        List<Colaborador> lista = colaboradorRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        (Colaborador c) -> c.getApellido() == null ? "" : c.getApellido(),
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
        lista.forEach(c -> { if (c.getPracticas() != null) c.getPracticas().size(); });
        model.addAttribute("colaboradores", lista);
        return VISTA_LISTA;
    }

    // ------------------------------------------------------------------ registro

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("colaborador", new Colaborador());
        return VISTA_REGISTRO;
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("colaborador") Colaborador form, Model model,
                          RedirectAttributes ra) {
        String rut = RutUtil.conPuntos(form.getRut());

        String error = null;
        if (rut == null || !RutUtil.esValido(rut)) {
            error = "El RUT ingresado no es válido.";
        } else if (buscar(rut).isPresent()) {
            error = "Ya existe un profesor colaborador con ese RUT.";
        } else {
            error = validarDatos(form, null);
        }

        if (error != null) {
            model.addAttribute("error", error);
            return VISTA_REGISTRO;
        }

        Colaborador c = new Colaborador();
        c.setRut(rut);                       // siempre en formato canónico (con puntos)
        aplicarDatos(c, form);
        colaboradorRepository.save(c);

        ra.addFlashAttribute("tituloExito", "¡Colaborador registrado!");
        ra.addFlashAttribute("mensajeExito",
                "El profesor colaborador " + c.getNombre() + " " + c.getApellido() + " fue registrado exitosamente.");
        return "redirect:/colaboradores";
    }

    // ------------------------------------------------------------------ edición

    @GetMapping("/editar/{rut}")
    @Transactional(readOnly = true)
    public String editar(@PathVariable String rut, Model model) {
        Colaborador c = buscar(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT de colaborador inválido: " + rut));
        model.addAttribute("colaborador", c);
        return VISTA_EDICION;
    }

    @PostMapping("/actualizar/{rut}")
    @Transactional
    public String actualizar(@PathVariable String rut,
                             @ModelAttribute("colaborador") Colaborador form,
                             Model model, RedirectAttributes ra) {
        Colaborador c = buscar(rut)
                .orElseThrow(() -> new IllegalArgumentException("RUT de colaborador inválido: " + rut));

        String error = validarDatos(form, c.getRut());
        if (error != null) {
            form.setRut(c.getRut());         // el RUT no viene en el formulario (no se edita)
            model.addAttribute("error", error);
            return VISTA_EDICION;
        }

        aplicarDatos(c, form);               // el RUT nunca se modifica: es la clave primaria
        colaboradorRepository.save(c);

        ra.addFlashAttribute("tituloExito", "¡Colaborador actualizado!");
        ra.addFlashAttribute("mensajeExito",
                "Los datos de " + c.getNombre() + " " + c.getApellido() + " fueron actualizados exitosamente.");
        return "redirect:/colaboradores";
    }

    // ------------------------------------------------------------------ internos

    /** Busca tolerando RUT guardados con o sin puntos (los creados por el formulario por enlace). */
    private Optional<Colaborador> buscar(String rut) {
        for (String clave : new String[] { rut, RutUtil.conPuntos(rut), RutUtil.normalizar(rut) }) {
            if (clave == null) continue;
            Optional<Colaborador> c = colaboradorRepository.findById(clave);
            if (c.isPresent()) return c;
        }
        return Optional.empty();
    }

    /** Devuelve el mensaje de error o null. rutActual = RUT del que se edita (null si es nuevo). */
    private String validarDatos(Colaborador f, String rutActual) {
        String nombre = f.getNombre() == null ? "" : f.getNombre().trim();
        String apellido = f.getApellido() == null ? "" : f.getApellido().trim();
        String correo = f.getCorreo() == null ? "" : f.getCorreo().trim();
        String telefono = f.getTelefono() == null ? "" : f.getTelefono().trim();

        if (nombre.isEmpty() || apellido.isEmpty()) {
            return "El nombre y el apellido no pueden quedar vacíos.";
        }
        if (nombre.length() > 100 || apellido.length() > 100) {
            return "El nombre y el apellido pueden tener hasta 100 caracteres.";
        }
        if (correo.isEmpty() || correo.length() > 150 || !CORREO.matcher(correo).matches()) {
            return "Ingresa un correo válido.";
        }
        if (!telefono.isEmpty() && !TELEFONO.matcher(telefono).matches()) {
            return "El teléfono solo puede tener números, espacios, +, ( ) y guiones (entre 7 y 20 caracteres).";
        }
        // Se revisa en memoria: puede haber correos repetidos de colaboradores creados por enlace
        boolean correoEnUso = colaboradorRepository.findAll().stream()
                .anyMatch(o -> o.getCorreo() != null
                        && o.getCorreo().trim().equalsIgnoreCase(correo)
                        && !o.getRut().equals(rutActual));
        if (correoEnUso) {
            return "El correo ingresado ya pertenece a otro colaborador.";
        }
        return null;
    }

    private void aplicarDatos(Colaborador destino, Colaborador f) {
        destino.setNombre(f.getNombre().trim());
        destino.setApellido(f.getApellido().trim());
        destino.setCorreo(f.getCorreo().trim());
        String tel = f.getTelefono() == null ? "" : f.getTelefono().trim();
        destino.setTelefono(tel.isEmpty() ? null : tel);
    }
}