package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EnlaceEvaluacion;

@Repository
public interface EnlaceEvaluacionRepository extends JpaRepository<EnlaceEvaluacion, Long> {

    Optional<EnlaceEvaluacion> findByToken(String token);

    // Obtiene el enlace para actualización dentro de la transacción
    @Query("SELECT e FROM EnlaceEvaluacion e WHERE e.token = :token")
    Optional<EnlaceEvaluacion> findByTokenParaActualizar(@Param("token") String token);

    List<EnlaceEvaluacion> findByPlanificacionId(Long planificacionId);

    Optional<EnlaceEvaluacion> findFirstByPlanificacionIdOrderByFechaCreacionDesc(Long planificacionId);
}
