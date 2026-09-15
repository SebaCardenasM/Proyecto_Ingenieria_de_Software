package repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;

@Repository
public interface PlanificacionRepository extends JpaRepository<Planificacion, Long> {
}