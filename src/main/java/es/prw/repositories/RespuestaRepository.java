package es.prw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.prw.models.Respuesta;
import es.prw.models.Pregunta;
import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {
    
    List<Respuesta> findByPregunta(Pregunta pregunta);

    List<Respuesta> findByIdRespuestaIn(List<Integer> idsRespuestas);
}
