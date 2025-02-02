package es.prw.controllers;

import java.security.Timestamp;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import es.prw.daos.MateriaDao;
import es.prw.daos.PreguntaDao;
import es.prw.daos.PuntuacionDao;
import es.prw.daos.RespuestaDao;
import es.prw.daos.TestDao;
import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Materia;
import es.prw.models.Pregunta;
import es.prw.models.Puntuacion;
import es.prw.models.Respuesta;
import es.prw.models.Test;
import es.prw.models.Usuario;
import jakarta.servlet.http.HttpSession;

@SessionAttributes({"usuario", "ultima_puntuacion", "respuestas", "sessionKey","nota"})

@Controller
public class homeController {
	@Autowired
	private RespuestaDao respuestaDao;
	@Autowired
	private PuntuacionDao puntuacionDao;

	@GetMapping("/home")
	public String home(CsrfToken csrfToken, Model model) {

		model.addAttribute("csrfToken", csrfToken);
		return "views/home";
	}

	@GetMapping("/materias")
	@ResponseBody
	public List<Materia> getMaterias() {
		MateriaDao materiaDao = new MateriaDao();
		return materiaDao.getMaterias();
	}

	@GetMapping("/tests")
	@ResponseBody
	public List<Test> getTests(@RequestParam("idMateria") int idMateria) {
		TestDao testDao = new TestDao();
		return testDao.getTests(idMateria);
	}

	@GetMapping("/preguntas")
	@ResponseBody
	public List<Pregunta> getPreguntasConRespuestas(@RequestParam("idTest") int idTest, HttpSession session) {
	    PreguntaDao preguntaDao = new PreguntaDao();
	    return preguntaDao.getPreguntasConRespuestas(idTest, session);
	}



	@PostMapping("/calcularNota")
	public ResponseEntity<Map<String, Object>> calcularNota(@RequestBody EvaluacionDTO evaluacion,
	        @SessionAttribute(name = "usuario", required = false) Usuario usuario, HttpSession session) throws SQLException {
	    Map<String, Object> response = new HashMap<>();

	    // Verificar si el usuario está autenticado
	    if (usuario == null) {
	        response.put("error", "Usuario no autenticado");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    }

	    // Calcular la nota total usando las respuestas almacenadas en sesión o BD
	    double notaTotal = calcularNotaTotal(evaluacion, session);

	    // Crear el objeto de Puntuacion
	    Puntuacion nuevaPuntuacion = new Puntuacion(null, notaTotal, usuario.getIdUsuario(), evaluacion.getIdTest(),
	            new java.util.Date());

	    // Guardar la puntuación en la base de datos
	    Optional<Puntuacion> puntuacionGuardada = puntuacionDao.savePuntuacion(nuevaPuntuacion, session);

	    if (puntuacionGuardada.isPresent()) {
	        Puntuacion ultimaPuntuacion = puntuacionGuardada.get();
	        String sessionKey = "ultima_puntuacion_" + evaluacion.getIdTest();
	        
	        // ✅ Actualizar la sesión con la nueva puntuación
	        session.setAttribute(sessionKey, ultimaPuntuacion);

	        response.put("nota", ultimaPuntuacion.getNotaConseguida());
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("error", "Error al guardar puntuación");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}



	private double calcularNotaTotal(EvaluacionDTO evaluacion, HttpSession session) {
	    if (evaluacion == null || evaluacion.getRespuestas() == null || evaluacion.getRespuestas().isEmpty()) {
	        System.err.println("Error: La evaluación o la lista de respuestas es nula o vacía.");
	        return 0.0; // Retornamos 0 si no hay respuestas
	    }

	    List<Respuesta> respuestas = respuestaDao.getRespuestasByIds(evaluacion.getRespuestas(), session);
	    
	    if (respuestas == null || respuestas.isEmpty()) {
	        System.err.println("Error: No se encontraron respuestas en la base de datos.");
	        return 0.0; // Evita `NullPointerException`
	    }

	    return respuestas.stream().mapToDouble(Respuesta::getNota).sum();
	}


	@GetMapping("/limpiarSesion")
	public ResponseEntity<String> limpiarSesion(HttpSession session) {
		session.invalidate();
		return ResponseEntity.ok("Sesión limpiada");
	}

	@GetMapping("/ultimaPuntuacion")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getUltimaPuntuacion(
	        @SessionAttribute(name = "usuario", required = false) Usuario usuario,
	        @RequestParam("idTest") int idTest,
	        HttpSession session) {

	    Map<String, Object> response = new HashMap<>();

	    if (usuario == null) {
	        response.put("error", "Usuario no autenticado");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    }

	    List<Double> notas = puntuacionDao.getUltimasPuntuacionesByTest(usuario.getIdUsuario(), idTest, session);

	    if (notas.isEmpty()) {
	        response.put("ultimaNota", null);
	        response.put("penultimaNota", null);
	    } else if (notas.size() == 1) {
	        response.put("ultimaNota", notas.get(0));
	        response.put("penultimaNota", null);
	    } else {
	        response.put("ultimaNota", notas.get(0));
	        response.put("penultimaNota", notas.get(1));
	    }

	    return ResponseEntity.ok(response);
	}




}
