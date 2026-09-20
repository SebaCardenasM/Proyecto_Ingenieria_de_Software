package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ArchivoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;

@Repository
public interface ArchivoPracticaRepository extends JpaRepository<ArchivoPractica, Long> {

    @Query("SELECT a FROM ArchivoPractica a " +
           "JOIN a.practica p " +
           "JOIN p.estudiante e " +
           "JOIN e.usuario u " +
           "WHERE (:busqueda IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "   OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "   OR u.rut LIKE CONCAT('%', :busqueda, '%')) " +
           "AND (:numPractica IS NULL OR p.numeroPractica = :numPractica) " +
           "AND (:tipo IS NULL OR a.tipoDocumento = :tipo)")
    List<ArchivoPractica> buscarConFiltros(
        @Param("busqueda") String busqueda,
        @Param("numPractica") Integer numPractica,
        @Param("tipo") TipoDocumento tipo
    );
}