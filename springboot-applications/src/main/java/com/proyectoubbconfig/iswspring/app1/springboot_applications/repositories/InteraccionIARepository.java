package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.InteraccionIA;

@Repository
public interface InteraccionIARepository extends JpaRepository<InteraccionIA, Long> {

    public interface ConteoDudaDTO {
        String getCategoria();
        Long getTotal();
    }

    public interface ResumenEstudianteDTO {
        String getRut();
        Long getTotal();
        LocalDateTime getUltima();
    }

    @Query("SELECT i.categoriaDuda AS categoria, COUNT(i) AS total " +
           "FROM InteraccionIA i GROUP BY i.categoriaDuda ORDER BY COUNT(i) DESC")
    List<ConteoDudaDTO> obtenerDudasMasFrecuentes();

    List<InteraccionIA> findTop20ByOrderByFechaDesc();

    // Una fila por usuario: cuántos mensajes y cuándo fue el último
    @Query("SELECT i.rutEstudiante AS rut, COUNT(i) AS total, MAX(i.fecha) AS ultima " +
           "FROM InteraccionIA i GROUP BY i.rutEstudiante ORDER BY MAX(i.fecha) DESC")
    List<ResumenEstudianteDTO> resumenPorEstudiante();

    // Vista del coordinador (se devuelven del más reciente al más antiguo)
    List<InteraccionIA> findTop200ByRutEstudianteOrderByFechaDesc(String rutEstudiante);

    // Para el resumen con IA
    List<InteraccionIA> findTop40ByRutEstudianteOrderByFechaDesc(String rutEstudiante);

    // Historial del chat del propio usuario (sin las revisiones de documentos)
    List<InteraccionIA> findTop30ByRutEstudianteAndCategoriaDudaNotOrderByFechaDesc(String rut, String categoria);

    // Contexto reciente que se le pasa al asistente
    List<InteraccionIA> findTop3ByRutEstudianteAndCategoriaDudaNotOrderByFechaDesc(String rut, String categoria);
}