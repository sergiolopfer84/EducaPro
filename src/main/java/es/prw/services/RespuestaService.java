package es.prw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.prw.models.Respuesta;
import es.prw.models.Test;
import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Pregunta;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PreguntaRepository;
import java.util.List;
import java.util.Optional;

@Service
public class RespuestaService {

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private TestRepository testRepository;
    
    @Autowired
    private PreguntaRepository preguntaRepository;

    // Obtener respuestas de una pregunta
    public List<Respuesta> getRespuestasByPregunta(int idPregunta) {
        Optional<Pregunta> pregunta = preguntaRepository.findById(idPregunta);
        return pregunta.map(respuestaRepository::findByPregunta)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));
    }

    // Obtener respuestas por lista de IDs
    public List<Respuesta> getRespuestasByIds(List<Integer> idsRespuestas) {
        return respuestaRepository.findByIdRespuestaIn(idsRespuestas);
    }
    public double evaluarRespuestas(EvaluacionDTO evaluacionDTO) {
        Test test = testRepository.findById(evaluacionDTO.getIdTest())
                .orElseThrow(() -> new RuntimeException("Test no encontrado"));

        List<Respuesta> respuestasCorrectas = respuestaRepository.findByIdRespuestaIn(evaluacionDTO.getRespuestas());

        double notaTotal = respuestasCorrectas.stream()
                .mapToDouble(Respuesta::getNota)
                .sum();

        return notaTotal;
    }
}
