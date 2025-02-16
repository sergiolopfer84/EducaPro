package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import es.prw.models.Materia;
import es.prw.models.Pregunta;
import es.prw.models.Respuesta;
import es.prw.models.Test;
import es.prw.repositories.PreguntaRepository;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PreguntaService {

	 private final PreguntaRepository preguntaRepository;
	    private final TestRepository testRepository;
	    private final RespuestaRepository respuestaRepository;

	    public PreguntaService(PreguntaRepository preguntaRepository, TestRepository testRepository, RespuestaRepository respuestaRepository) {
	        this.preguntaRepository = preguntaRepository;
	        this.testRepository = testRepository;
	        this.respuestaRepository = respuestaRepository;
	    }

	    @Transactional(readOnly = true)
	    public List<Pregunta> getPreguntas() {
	    	 List<Pregunta> preguntas = preguntaRepository.findAll();
	    	System.out.println(preguntas);
	        return preguntas;
	    }
	    
    @Transactional(readOnly = true)
    public List<Pregunta> getPreguntasConRespuestas(int idTest) {
        if (!testRepository.existsById(idTest)) {
            throw new IllegalStateException("Test no encontrado");
        }
        List<Pregunta> preguntas = preguntaRepository.findByTestIdTest(idTest);
        Collections.shuffle(preguntas); // Barajar preguntas
        
        // Barajar respuestas dentro de cada pregunta
        preguntas.forEach(pregunta -> {
            List<Respuesta> respuestas = respuestaRepository.findByPregunta(pregunta);
            Collections.shuffle(respuestas);
            pregunta.setRespuestas(respuestas);
        });
        
        return preguntas;
    }

    
    @PostMapping("/preguntas")
    public Pregunta crearPregunta(@RequestBody Pregunta pregunta) {
        Test test = testRepository.findById(pregunta.getTest().getIdTest())
            .orElseThrow(() -> new RuntimeException("Test no encontrado"));
        pregunta.setTest(test);
        return preguntaRepository.save(pregunta);
    }
    
	/*
	 * @Transactional public Pregunta crearPregunta(Pregunta pregunta) { return
	 * preguntaRepository.save(pregunta); }
	 */

    @Transactional
    public Pregunta actualizarPregunta(int id, Pregunta nuevaPregunta) {
        Optional<Pregunta> preguntaExistente = preguntaRepository.findById(id);
        
        if (preguntaExistente.isPresent()) {
            Pregunta pregunta = preguntaExistente.get();
            pregunta.setPregunta(nuevaPregunta.getPregunta());
            pregunta.setTest(nuevaPregunta.getTest());
            return preguntaRepository.save(pregunta);
        } else {
            throw new RuntimeException("Pregunta no encontrada con ID: " + id);
        }
    }

    @Transactional
    public void eliminarPregunta(int id) {
        preguntaRepository.deleteById(id);
    }
    
    
    
}
