package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByEstudianteRutOrderByFechaDesc(String rut);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.estudiante.rut = :rut AND (n.leida IS NULL OR n.leida = false)")
    long contarNoLeidas(@Param("rut") String rut);
}
