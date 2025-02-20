package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.prw.dtos.PreguntaDTO;
import es.prw.models.Pregunta;
import es.prw.models.Respuesta;
import es.prw.models.Test;
import es.prw.repositories.PreguntaRepository;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final TestRepository testRepository;
    private final RespuestaRepository respuestaRepository;
	private final TestService testService;

    public PreguntaService(PreguntaRepository preguntaRepository, TestRepository testRepository, RespuestaRepository respuestaRepository, TestService testService) {
        this.preguntaRepository = preguntaRepository;
        this.testRepository = testRepository;
        this.respuestaRepository = respuestaRepository;
        this.testService = testService;
    }

    @Transactional(readOnly = true)
    public List<Pregunta> getPreguntas() {
        List<Pregunta> preguntas = preguntaRepository.findAll();
        System.out.println(preguntas);
        return preguntas;
    }
    
    @Transactional(readOnly = true)
    public List<PreguntaDTO> getPreguntasDTO() {
        return preguntaRepository.findAll()
                .stream()
                .map(PreguntaDTO::new)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<Pregunta> getPreguntasConRespuestas(int idTest) {
        testRepository.findById(idTest).orElseThrow(() -> new RuntimeException("❌ Test no encontrado con ID: " + idTest));

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
    @Transactional
    public Pregunta crearPreguntaDesdeDTO(PreguntaDTO dto) {
        if (dto.getIdTest() == null) {
            throw new IllegalArgumentException("El test asociado es requerido.");
        }
        // Buscar el Test usando el idTest recibido en el DTO.
        Test test = testService.buscarPorId(dto.getIdTest());
        if (test == null) {
            throw new IllegalArgumentException("Test no encontrado con id: " + dto.getIdTest());
        }
        // Crear la entidad Pregunta a partir del DTO.
        Pregunta pregunta = new Pregunta();
        pregunta.setTextoPregunta(dto.getTextoPregunta());
        pregunta.setTest(test);
        return preguntaRepository.save(pregunta);
    }

    @Transactional
    public Pregunta crearPregunta(Pregunta pregunta) {
        if (pregunta.getTest() == null || pregunta.getTest().getIdTest() == null) {
            throw new IllegalArgumentException("El test asociado es requerido.");
        }
        // Supongamos que tienes un método en testService para buscar por id:
        Test test = testService.buscarPorId(pregunta.getTest().getIdTest());
        if (test == null) {
            throw new IllegalArgumentException("Test no encontrado con id: " + pregunta.getTest().getIdTest());
        }
        pregunta.setTest(test);
        // Continuar con la lógica de guardado...
        return preguntaRepository.save(pregunta);
    }

    @Transactional
    public Pregunta actualizarPreguntaDesdeDTO(int id,PreguntaDTO preguntaDTO) {
        Pregunta preguntaExistente = preguntaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pregunta no encontrada con ID: " + id));
        
        // Actualizar el texto
        preguntaExistente.setTextoPregunta(preguntaDTO.getTextoPregunta());
        
        // Obtener el test usando el idTest del DTO
        Test test = testRepository.findById(preguntaDTO.getIdTest())
            .orElseThrow(() -> new IllegalStateException("Test no encontrado con id: " + preguntaDTO.getIdTest()));
        
        preguntaExistente.setTest(test);
        
        return preguntaRepository.save(preguntaExistente);
    }



    @Transactional
    public void eliminarPregunta(int id) {
        preguntaRepository.deleteById(id);
    }
}
