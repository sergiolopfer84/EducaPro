package es.prw.controllers;

import es.prw.models.Usuario;
import es.prw.repositories.PuntuacionRepository;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.OpenAIService;
import es.prw.services.ProgresoService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/asistente")
public class AsistenteController {

	private final OpenAIService openAIService;
	private final ProgresoService progresoService;
	private final UsuarioRepository usuarioRepository;
	private final PuntuacionRepository puntuacionRepository;

	public AsistenteController(OpenAIService openAIService, ProgresoService progresoService,
			UsuarioRepository usuarioRepository, PuntuacionRepository puntuacionRepository) {
		this.openAIService = openAIService;
		this.progresoService = progresoService;
		this.usuarioRepository = usuarioRepository;
		this.puntuacionRepository = puntuacionRepository;
	}

	@PostMapping
	public String obtenerRespuesta(@RequestBody String mensaje, Authentication authentication) {
		if (mensaje == null || mensaje.trim().isEmpty()) {
			return "Por favor, introduce un mensaje válido.";
		}

		// Obtener usuario autenticado
		org.springframework.security.core.userdetails.User springUser = (org.springframework.security.core.userdetails.User) authentication
				.getPrincipal();
		String email = springUser.getUsername();

		Usuario usuarioReal = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

		Integer idUsuario = usuarioReal.getIdUsuario();

		// Detectar si el mensaje menciona una materia específica
		String materiaConsultada = extraerMateriaDesdeMensaje(mensaje);
		int idMateria = (materiaConsultada != null) ? obtenerIdMateriaPorNombre(materiaConsultada) : -1;

		// Obtener progreso general o por materia específica
		Map<String, Map<String, List<Double>>> notasPorMateria;
		if (idMateria != -1) {
			notasPorMateria = progresoService.obtenerProgresoMateriaEspecifica(idUsuario, idMateria);
		} else {
			notasPorMateria = progresoService.obtenerProgresoTests(idUsuario);
		}

		// Construir infoProgreso con bucle dinámico
		StringBuilder infoProgreso = new StringBuilder();
		infoProgreso.append("El usuario ")
				.append(usuarioReal.getNombre())
				.append(" pregunta por su progreso.\n\n");

		if (notasPorMateria.isEmpty()) {
			infoProgreso.append("No hay registros de tests.");
		} else {
			infoProgreso.append("Aquí están sus notas recientes:\n");
			// Iteramos por cada materia
			for (Map.Entry<String, Map<String, List<Double>>> materiaEntry : notasPorMateria.entrySet()) {
				String nombreMateria = materiaEntry.getKey(); // p. ej. "Geografía" o "Biología"
				Map<String, List<Double>> testsMateria = materiaEntry.getValue();

				infoProgreso.append("\nMateria: ").append(nombreMateria).append("\n");

				// Iteramos por cada test dentro de la materia
				for (Map.Entry<String, List<Double>> testEntry : testsMateria.entrySet()) {
					String nombreTest = testEntry.getKey();
					List<Double> notas = testEntry.getValue();
					infoProgreso.append("   - ").append(nombreTest).append(": ")
							.append(notas).append("\n");
				}
			}
		}

		Map<String, Object> datosChat = Map.of(
				"usuario", usuarioReal.getNombre(),
				"mensaje", mensaje,
				"infoProgreso", infoProgreso.toString());

		// Debug: Ver qué datos está recibiendo la IA
		System.out.println("JSON enviado a la IA: " + datosChat);

		return openAIService.obtenerRespuestaIA(datosChat);
	}

	private String extraerMateriaDesdeMensaje(String mensaje) {
		if (mensaje.toLowerCase().contains("geografía") || mensaje.toLowerCase().contains("geografia"))
			return "Geografía";
		if (mensaje.toLowerCase().contains("biología") || mensaje.toLowerCase().contains("biologia"))
			return "Biología";
		return null;
	}

	private int obtenerIdMateriaPorNombre(String materia) {
		return switch (materia.toLowerCase()) {
			case "geografía" -> 1;
			case "biología" -> 2;
			default -> -1;
		};
	}
}
