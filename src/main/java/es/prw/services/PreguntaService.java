package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Pregunta;
import es.prw.repositories.PreguntaRepository;
import es.prw.repositories.TestRepository;
import java.util.List;

@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final TestRepository testRepository;

    // Inyección de dependencias por constructor
    public PreguntaService(PreguntaRepository preguntaRepository, TestRepository testRepository) {
        this.preguntaRepository = preguntaRepository;
        this.testRepository = testRepository;
    }

    // Obtener preguntas con respuestas de un test
    @Transactional(readOnly = true)
    public List<Pregunta> getPreguntasConRespuestas(int idTest) {
        if (!testRepository.existsById(idTest)) {
            throw new IllegalStateException("Test no encontrado");
        }
        return preguntaRepository.findByTestIdTest(idTest);
    }
}
