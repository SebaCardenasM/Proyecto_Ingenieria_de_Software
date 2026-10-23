package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.CentroPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CentroPracticaRepository extends JpaRepository<CentroPractica, Long> {
    List<CentroPractica> findByComuna(String comuna);
}