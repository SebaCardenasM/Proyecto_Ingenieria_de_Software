package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

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

    @Query("SELECT i.categoriaDuda AS categoria, COUNT(i) AS total " +
           "FROM InteraccionIA i GROUP BY i.categoriaDuda ORDER BY COUNT(i) DESC")
    List<ConteoDudaDTO> obtenerDudasMasFrecuentes();

    List<InteraccionIA> findTop20ByOrderByFechaDesc();
}