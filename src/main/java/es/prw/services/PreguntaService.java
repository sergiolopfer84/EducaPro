package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import es.prw.models.Pregunta;
import es.prw.models.Respuesta;
import es.prw.models.Test;
import es.prw.repositories.PreguntaRepository;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;

import java.util.Collections;
import java.util.List;

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

		/*
		 * // Obtener preguntas con respuestas de un test
		 * 
		 * @Transactional(readOnly = true) public List<Pregunta>
		 * getPreguntasConRespuestas1(int idTest) { if
		 * (!testRepository.existsById(idTest)) { throw new
		 * IllegalStateException("Test no encontrado"); } return
		 * preguntaRepository.findByTestIdTest(idTest); }
		 */
	    
	    
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

}
