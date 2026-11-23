package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluacionPracticaRepository extends JpaRepository<EvaluacionPractica, Long> {

    Optional<EvaluacionPractica> findByNumeroPracticaAndPracticaIsNull(Integer numeroPractica);

    Optional<EvaluacionPractica> findByPracticaId(Long practicaId);

    List<EvaluacionPractica> findByNumeroPracticaAndPracticaIsNotNull(Integer numeroPractica);

    List<EvaluacionPractica> findByPracticaIsNullOrderByNumeroPracticaAsc();
}
