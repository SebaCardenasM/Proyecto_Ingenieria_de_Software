package repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
}