package es.prw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.prw.models.Pregunta;
import es.prw.models.Test;
import es.prw.repositories.PreguntaRepository;
import es.prw.repositories.TestRepository;
import java.util.List;
import java.util.Optional;

@Service
public class PreguntaService {

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private TestRepository testRepository;

    // Obtener preguntas con respuestas de un test
    public List<Pregunta> getPreguntasConRespuestas(int idTest) {
        Optional<Test> test = testRepository.findById(idTest);
        return test.map(preguntaRepository::findByTest).orElseThrow(() -> new RuntimeException("Test no encontrado"));
    }
}
