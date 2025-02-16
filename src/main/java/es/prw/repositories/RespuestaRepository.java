package es.prw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Respuesta;
import es.prw.models.Pregunta;
import java.util.List;

@Repository
public interface RespuestaRepository extends JpaRepository<Respuesta, Integer> {
    //Obtiene todas las respuestas asociadas a una pregunta
    @Transactional(readOnly = true)
    List<Respuesta> findByPregunta(Pregunta pregunta);

    @Transactional(readOnly = true)
    List<Respuesta> findByPreguntaIdPregunta(Integer idPregunta); // Alternativa más eficiente

    ///Encuentra respuestas específicas mediante una lista de IDs.
    @Transactional(readOnly = true)
    List<Respuesta> findByIdRespuestaIn(List<Integer> idsRespuestas);
}
