package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.prw.models.Pregunta;
import es.prw.models.Respuesta;
import es.prw.dtos.EvaluacionDTO;
import es.prw.dtos.RespuestaDTO;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PreguntaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class RespuestaService {

	private final RespuestaRepository respuestaRepository;
	private final TestRepository testRepository;
	private final PreguntaRepository preguntaRepository;

	// Inyección de dependencias por constructor
	public RespuestaService(RespuestaRepository respuestaRepository, TestRepository testRepository,
			PreguntaRepository preguntaRepository) {
		this.respuestaRepository = respuestaRepository;
		this.testRepository = testRepository;
		this.preguntaRepository = preguntaRepository;
	}

	@Transactional(readOnly = true)
	public List<Respuesta> getRespuestas() {
		List<Respuesta> respuestas = respuestaRepository.findAll();
		System.out.println(respuestas);
		return respuestas;
	}

	@Transactional(readOnly = true)
	public List<RespuestaDTO> getRespuestasDTO() {
		return respuestaRepository.findAll().stream().map(RespuestaDTO::new).collect(Collectors.toList());
	}

	// Obtener respuestas de una pregunta
	@Transactional(readOnly = true)
	public List<Respuesta> getRespuestasByPregunta(int idPregunta) {
		return preguntaRepository.findById(idPregunta).map(respuestaRepository::findByPregunta)
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

		List<Respuesta> respuestasCorrectas = Optional
				.ofNullable(respuestaRepository.findByIdRespuestaIn(evaluacionDTO.getRespuestas()))
				.orElse(Collections.emptyList());

		return respuestasCorrectas.stream().mapToDouble(Respuesta::getNota).sum();
	}

	@Transactional
	public Respuesta crearRespuestaDesdeDTO(RespuestaDTO respuestaDTO) {
		if (respuestaDTO.getIdPregunta() == null) {
			throw new IllegalArgumentException("La respuesta debe tener asociada una pregunta válida.");
		}
		Optional<Pregunta> preguntaOpt = preguntaRepository.findById(respuestaDTO.getIdPregunta());
		if (!preguntaOpt.isPresent()) {
			throw new IllegalArgumentException("Pregunta no encontrada con id: " + respuestaDTO.getIdPregunta());
		}
		Respuesta respuesta = new Respuesta();
		respuesta.setTextoRespuesta(respuestaDTO.getTextoRespuesta());
		respuesta.setTextoExplicacion(respuestaDTO.getTextoExplicacion());
		respuesta.setNota(respuestaDTO.getNota());
		respuesta.setPregunta(preguntaOpt.get());
		return respuestaRepository.save(respuesta);
	}

	@Transactional
	public Respuesta actualizarRespuestaDesdeDTO(Integer id, RespuestaDTO respuestaDTO) {
		// Buscar la respuesta existente
		Respuesta respuestaExistente = respuestaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Respuesta no encontrada con ID: " + id));

		// Actualizar los campos simples
		respuestaExistente.setTextoRespuesta(respuestaDTO.getTextoRespuesta());
		respuestaExistente.setTextoExplicacion(respuestaDTO.getTextoExplicacion());
		respuestaExistente.setNota(respuestaDTO.getNota());

		// Buscar la pregunta asociada usando el id del DTO
		Pregunta pregunta = preguntaRepository.findById(respuestaDTO.getIdPregunta()).orElseThrow(
				() -> new RuntimeException("Pregunta no encontrada con ID: " + respuestaDTO.getIdPregunta()));

		// Establecer la relación
		respuestaExistente.setPregunta(pregunta);

		// Guardar y retornar la respuesta actualizada
		return respuestaRepository.save(respuestaExistente);
	}

	@Transactional
	public void eliminarRespuesta(int id) {
		respuestaRepository.deleteById(id);
	}

}
