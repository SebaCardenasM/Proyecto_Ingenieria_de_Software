package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Dupla;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DuplaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DuplaService {

    public static final Set<Integer> PRACTICAS_EN_DUPLAS = Set.of(3, 4);

    @Autowired
    private DuplaRepository duplaRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    public boolean permiteDuplas(Integer numeroPractica) {
        return numeroPractica != null && PRACTICAS_EN_DUPLAS.contains(numeroPractica);
    }

    @Transactional(readOnly = true)
    public List<Integer> obtenerNumerosPracticaConDuplasDeProfesor(String rutProfesor) {
        Profesor profesor = profesorRepository.findById(rutProfesor).orElse(null);
        if (profesor == null) return Collections.emptyList();

        return practicaRepository.findByProfesor(profesor).stream()
                .map(Practica::getNumeroPractica)
                .filter(Objects::nonNull)
                .filter(PRACTICAS_EN_DUPLAS::contains)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Dupla> listarDuplasPorProfesorYPractica(String rutProfesor, Integer numeroPractica) {
        if (!permiteDuplas(numeroPractica)) {
            return Collections.emptyList();
        }
        return duplaRepository.findByProfesorRutAndNumeroPractica(rutProfesor, numeroPractica);
    }

    @Transactional(readOnly = true)
    public List<Practica> listarEstudiantesDisponibles(String rutProfesor, Integer numeroPractica) {
        if (!permiteDuplas(numeroPractica)) {
            return Collections.emptyList();
        }
        Profesor profesor = profesorRepository.findById(rutProfesor).orElse(null);
        if (profesor == null) return Collections.emptyList();

        List<Practica> practicasProfesor = practicaRepository.findByProfesor(profesor).stream()
                .filter(p -> numeroPractica.equals(p.getNumeroPractica()))
                .filter(p -> p.getEstudiante() != null)
                .toList();

        List<Dupla> duplasExistentes = duplaRepository.findByProfesorRutAndNumeroPractica(rutProfesor, numeroPractica);
        Set<String> rutsConDupla = new HashSet<>();
        for (Dupla d : duplasExistentes) {
            if (d.getEstudiante1() != null) rutsConDupla.add(d.getEstudiante1().getRut());
            if (d.getEstudiante2() != null) rutsConDupla.add(d.getEstudiante2().getRut());
        }

        return practicasProfesor.stream()
                .filter(p -> !rutsConDupla.contains(p.getEstudiante().getRut()))
                .sorted(Comparator.comparing((Practica p) -> p.getEstudiante().getApellido(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(p -> p.getEstudiante().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Practica> listarTodosEstudiantesDeProfesor(String rutProfesor, Integer numeroPractica) {
        if (!permiteDuplas(numeroPractica)) {
            return Collections.emptyList();
        }
        Profesor profesor = profesorRepository.findById(rutProfesor).orElse(null);
        if (profesor == null) return Collections.emptyList();

        return practicaRepository.findByProfesor(profesor).stream()
                .filter(p -> numeroPractica.equals(p.getNumeroPractica()))
                .filter(p -> p.getEstudiante() != null)
                .sorted(Comparator.comparing((Practica p) -> p.getEstudiante().getApellido(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(p -> p.getEstudiante().getNombre(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public Dupla crearDupla(String rutProfesor, Long practica1Id, Long practica2Id, Integer numeroPractica) {
        if (!permiteDuplas(numeroPractica)) {
            throw new IllegalArgumentException("La Práctica N° " + numeroPractica + " no permite modalidad en duplas (solo Prácticas 3 y 4).");
        }
        if (practica1Id == null || practica2Id == null) {
            throw new IllegalArgumentException("Debes seleccionar ambos estudiantes para formar la dupla.");
        }
        if (practica1Id.equals(practica2Id)) {
            throw new IllegalArgumentException("Debes seleccionar dos estudiantes distintos.");
        }

        Practica p1 = practicaRepository.findById(practica1Id)
                .orElseThrow(() -> new IllegalArgumentException("La práctica del primer estudiante no existe."));
        Practica p2 = practicaRepository.findById(practica2Id)
                .orElseThrow(() -> new IllegalArgumentException("La práctica del segundo estudiante no existe."));

        if (p1.getProfesor() == null || !rutProfesor.equalsIgnoreCase(p1.getProfesor().getRut()) ||
            p2.getProfesor() == null || !rutProfesor.equalsIgnoreCase(p2.getProfesor().getRut())) {
            throw new IllegalArgumentException("Solo puedes formar duplas con estudiantes asignados a tus prácticas.");
        }

        if (!numeroPractica.equals(p1.getNumeroPractica()) || !numeroPractica.equals(p2.getNumeroPractica())) {
            throw new IllegalArgumentException("Ambos estudiantes deben pertenecer a la Práctica " + numeroPractica +
                    ". No se pueden mezclar estudiantes de prácticas diferentes.");
        }

        Estudiante e1 = p1.getEstudiante();
        Estudiante e2 = p2.getEstudiante();
        if (e1 == null || e2 == null) {
            throw new IllegalArgumentException("Ambas prácticas deben tener un estudiante asignado.");
        }
        if (e1.getRut().equalsIgnoreCase(e2.getRut())) {
            throw new IllegalArgumentException("No puedes emparejar al mismo estudiante consigo mismo.");
        }

        if (duplaRepository.existsByEstudianteRutAndNumeroPractica(e1.getRut(), numeroPractica)) {
            throw new IllegalArgumentException("El/la estudiante " + e1.getNombre() + " " + e1.getApellido() +
                    " ya forma parte de otra dupla en la Práctica " + numeroPractica + ".");
        }
        if (duplaRepository.existsByEstudianteRutAndNumeroPractica(e2.getRut(), numeroPractica)) {
            throw new IllegalArgumentException("El/la estudiante " + e2.getNombre() + " " + e2.getApellido() +
                    " ya forma parte de otra dupla en la Práctica " + numeroPractica + ".");
        }

        Profesor profesor = p1.getProfesor();
        Integer ano = p1.getAno() != null ? p1.getAno() : p2.getAno();
        Integer semestre = p1.getSemestre() != null ? p1.getSemestre() : p2.getSemestre();

        Dupla nuevaDupla = new Dupla(numeroPractica, e1, e2, p1, p2, profesor, ano, semestre);
        return duplaRepository.save(nuevaDupla);
    }

    @Transactional
    public void eliminarDupla(String rutProfesor, Long duplaId) {
        Dupla dupla = duplaRepository.findById(duplaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la dupla especificada."));

        if (dupla.getProfesor() == null || !rutProfesor.equalsIgnoreCase(dupla.getProfesor().getRut())) {
            throw new IllegalArgumentException("No tienes permisos para deshacer esta dupla.");
        }

        duplaRepository.delete(dupla);
    }

    @Transactional(readOnly = true)
    public Optional<Dupla> buscarDuplaPorEstudianteYPractica(String rutEstudiante, Integer numeroPractica) {
        if (!permiteDuplas(numeroPractica)) {
            return Optional.empty();
        }
        return duplaRepository.findByEstudianteRutAndNumeroPractica(rutEstudiante, numeroPractica);
    }

    @Transactional(readOnly = true)
    public Optional<Dupla> buscarDuplaPorPractica(Long practicaId) {
        if (practicaId == null) return Optional.empty();
        return duplaRepository.findByPracticaId(practicaId);
    }
}
