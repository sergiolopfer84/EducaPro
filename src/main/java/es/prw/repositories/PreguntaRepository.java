package es.prw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Pregunta;
import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    @Transactional(readOnly = true)
    List<Pregunta> findByTestIdTest(Integer idTest); // Alternativa más eficiente

    @Transactional(readOnly = true)
    List<Pregunta> findByTest(Pregunta test); // Si prefieres usar el objeto completo
}
