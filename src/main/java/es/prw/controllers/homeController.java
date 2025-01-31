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
	public List<Pregunta> getPreguntasConRespuestas(@RequestParam("idTest") int idTest) {
	    PreguntaDao preguntaDao = new PreguntaDao();
	    return preguntaDao.getPreguntasConRespuestas(idTest);
	}
	
	@PostMapping("/calcularNota")
	public ResponseEntity<Map<String, Object>> calcularNota(@RequestBody EvaluacionDTO evaluacion,
	                                                        @SessionAttribute(name = "usuario", required = false) Usuario usuario) {
	    Map<String, Object> response = new HashMap<>();

	    // Verificar si el usuario está autenticado
	    if (usuario == null) {
	        response.put("error", "Usuario no autenticado");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    }

	    // Obtener respuestas desde el DAO y calcular la nota
	    double notaTotal = 0.0;
	    try {
	        List<Respuesta> respuestas = respuestaDao.getRespuestasByIds(evaluacion.getRespuestas());

	        if (respuestas.isEmpty()) {
	            response.put("error", "No se encontraron respuestas para los IDs proporcionados");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	        }

	        notaTotal = respuestas.stream()
	                .mapToDouble(Respuesta::getNota)
	                .sum();
	        System.out.println("nota total  "+ notaTotal);
	    } catch (Exception e) {
	        response.put("error", "Error al obtener las respuestas: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	    // Guardar la puntuación en la base de datos usando el DAO
	    try {
	        Puntuacion nuevaPuntuacion = new Puntuacion();
	        nuevaPuntuacion.setIdUsuario(usuario.getIdUsuario());
	        nuevaPuntuacion.setIdTest(evaluacion.getIdTest());
	        nuevaPuntuacion.setNotaConseguida(notaTotal);
	        nuevaPuntuacion.setFecha(new java.util.Date()); // Fecha actual

	        puntuacionDao.savePuntuacion(nuevaPuntuacion);
	    } catch (SQLException e) {
	        response.put("error", "Error al guardar la puntuación: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }

	    // Respuesta exitosa con la nota obtenida
	    response.put("nota", notaTotal);
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/ultimaPuntuacion")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getUltimaPuntuacion(
	        @SessionAttribute(name = "usuario", required = false) Usuario usuario,
	        @RequestParam("idTest") int idTest) {
		
		System.out.println(" id usuario cont "+ usuario);
		System.out.println(" id test cont "+ idTest);

	    Map<String, Object> response = new HashMap<>();

	    if (usuario == null) {
	        response.put("error", "Usuario no autenticado");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	    }

	    Optional<Puntuacion> ultimaPuntuacion = puntuacionDao.getUltimaPuntuacion(usuario.getIdUsuario(), idTest);

	    if (ultimaPuntuacion.isPresent()) {
	        response.put("nota", ultimaPuntuacion.get().getNotaConseguida());
	        return ResponseEntity.ok(response);
	    } else {
	        response.put("nota", null);
	        return ResponseEntity.ok(response); // No hay puntuación previa
	    }
	}

}
