package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PautaRepository extends JpaRepository<Pauta, Long> {

    // Buscar pautas de una práctica específica
    List<Pauta> findByPracticaId(Long practicaId);

    // Buscar pautas según el tipo de evaluador (TUTOR_UNIVERSIDAD o PROFESOR_COLABORADOR)
    List<Pauta> findByTipoEvaluador(TipoEvaluador tipoEvaluador);

    // Buscar pautas evaluadas por un Tutor Supervisor específico (por su RUT)
    List<Pauta> findByProfesorEvaluadorRut(String rutProfesor);

    // Buscar pautas evaluadas por un Colaborador específico (por su RUT)
    List<Pauta> findByColaboradorEvaluadorRut(String rutColaborador);
}