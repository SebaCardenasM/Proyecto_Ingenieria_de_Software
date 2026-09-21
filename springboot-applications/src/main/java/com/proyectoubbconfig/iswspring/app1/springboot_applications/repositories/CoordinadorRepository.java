package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Coordinador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoordinadorRepository extends JpaRepository<Coordinador, Long> {

    // Método útil para buscar un coordinador por el ID de su usuario asociado
    Optional<Coordinador> findByUsuarioId(Long usuarioId);
}