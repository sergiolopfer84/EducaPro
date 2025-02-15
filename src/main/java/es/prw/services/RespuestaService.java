package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import es.prw.models.Respuesta;
import es.prw.models.Test;
import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Pregunta;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PreguntaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

@Service
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final TestRepository testRepository;
    private final PreguntaRepository preguntaRepository;

    // Inyección de dependencias por constructor
    public RespuestaService(RespuestaRepository respuestaRepository, TestRepository testRepository, PreguntaRepository preguntaRepository) {
        this.respuestaRepository = respuestaRepository;
        this.testRepository = testRepository;
        this.preguntaRepository = preguntaRepository;
    }

    // Obtener respuestas de una pregunta
    @Transactional(readOnly = true)
    public List<Respuesta> getRespuestasByPregunta(int idPregunta) {
        return preguntaRepository.findById(idPregunta)
                .map(respuestaRepository::findByPregunta)
                .orElseThrow(() -> new IllegalStateException("Pregunta no encontrada"));
    }

    // Obtener respuestas por lista de IDs
    @Transactional(readOnly = true)
    public List<Respuesta> getRespuestasByIds(List<Integer> idsRespuestas) {
        return respuestaRepository.findByIdRespuestaIn(idsRespuestas);
    }

    // Evaluar respuestas
    @Transactional(readOnly = true)
    public double evaluarRespuestas(EvaluacionDTO evaluacionDTO) {
        testRepository.findById(evaluacionDTO.getIdTest())
                .orElseThrow(() -> new IllegalStateException("Test no encontrado"));

        List<Respuesta> respuestasCorrectas = Optional.ofNullable(
                respuestaRepository.findByIdRespuestaIn(evaluacionDTO.getRespuestas()))
                .orElse(Collections.emptyList());

        return respuestasCorrectas.stream()
                .mapToDouble(Respuesta::getNota)
                .sum();
    }
    
    @Transactional
    public Respuesta crearRespuesta(Respuesta respuesta) {
        return respuestaRepository.save(respuesta);
    }

    @Transactional
    public Respuesta actualizarRespuesta(int id, Respuesta nuevaRespuesta) {
        Optional<Respuesta> respuestaExistente = respuestaRepository.findById(id);
        
        if (respuestaExistente.isPresent()) {
            Respuesta respuesta = respuestaExistente.get();
            respuesta.setTextoRespuesta(nuevaRespuesta.getTextoRespuesta());
            respuesta.setTextoExplicacion(nuevaRespuesta.getTextoExplicacion());
            respuesta.setNota(nuevaRespuesta.getNota());
            respuesta.setPregunta(nuevaRespuesta.getPregunta());
            return respuestaRepository.save(respuesta);
        } else {
            throw new RuntimeException("Respuesta no encontrada con ID: " + id);
        }
    }

    @Transactional
    public void eliminarRespuesta(int id) {
        respuestaRepository.deleteById(id);
    }
    
    
    
    
}
