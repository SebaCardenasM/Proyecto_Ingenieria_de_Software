package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;

public interface InformeRepository extends JpaRepository<Informe, Long> {
}