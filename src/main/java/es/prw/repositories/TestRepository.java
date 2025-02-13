package es.prw.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import es.prw.models.Materia;
import es.prw.models.Test;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {

    @Transactional(readOnly = true)
    int countByMateria(Materia materia);

    @Transactional(readOnly = true)
    List<Test> findByMateriaIdMateria(Integer idMateria); // Usa la convención de nombres de Spring Data
}
