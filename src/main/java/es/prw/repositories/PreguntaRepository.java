package es.prw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.prw.models.Pregunta;
import es.prw.models.Test;
import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {
    List<Pregunta> findByTest(Test test);
}
