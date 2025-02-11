package es.prw.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import es.prw.models.Materia;
import es.prw.models.Test;

@Repository
public interface TestRepository extends JpaRepository<Test, Integer> {
    int countByMateria(Materia materia);

    @Query("SELECT t FROM Test t WHERE t.materia.idMateria = :idMateria")
    List<Test> findTestsByMateria(Integer idMateria); // Mejor usar el ID en vez del objeto Materia
}

