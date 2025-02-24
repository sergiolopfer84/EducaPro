package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Respuesta;
import es.prw.services.RespuestaService;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

	private final RespuestaService respuestaService;

	public RespuestaController(RespuestaService respuestaService) {
		this.respuestaService = respuestaService;
	}

	// ✅ Obtener todas las respuestas
	@GetMapping
	public ResponseEntity<List<Respuesta>> obtenerRespuestas() {
		return ResponseEntity.ok(respuestaService.getRespuestas());
	}

	// ✅ Obtener respuestas de sesión de un test
	@GetMapping("/sesion")
	public ResponseEntity<List<Respuesta>> obtenerRespuestasSesion(@RequestParam("idTest") int idTest,
			HttpSession session) {
		List<?> respuestasList = (List<?>) session.getAttribute("respuestasTest_" + idTest);
		System.out.println("Respuestas recuperadas de sesión: " + respuestasList);

		if (respuestasList != null && !respuestasList.isEmpty() && respuestasList.get(0) instanceof Respuesta) {
			return ResponseEntity.ok((List<Respuesta>) respuestasList);
		}

		return ResponseEntity.noContent().build();
	}
}
