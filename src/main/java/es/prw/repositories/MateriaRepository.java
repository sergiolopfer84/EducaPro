

package es.prw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.prw.models.Materia;

@Repository
public interface MateriaRepository extends JpaRepository<Materia, Integer> {
}
