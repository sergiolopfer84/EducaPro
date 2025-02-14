package es.prw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Materia;
import java.util.Optional;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    @Transactional(readOnly = true)
    Optional<Materia> findByNombreMateria(String nombreMateria); // Buscar materia por nombre
}
