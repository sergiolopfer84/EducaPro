package es.prw.controllers;

import java.sql.SQLException;
import java.util.ArrayList;
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

    /**
     * Carga las preguntas de un test y sus respuestas. Para evitar duplicaciones,
     * guardamos TODAS las respuestas en una sola lista en la sesión, con una
     * clave única: "respuestasTest_{idTest}".
     */
    @GetMapping("/preguntas")
    @ResponseBody
    public List<Pregunta> getPreguntasConRespuestas(@RequestParam("idTest") int idTest, HttpSession session) {
        PreguntaDao preguntaDao = new PreguntaDao();
        List<Pregunta> preguntas = preguntaDao.getPreguntasConRespuestas(idTest, session);

        // Almacenar todas las respuestas en sesión para su posterior consulta
        List<Respuesta> todasLasRespuestas = new ArrayList<>();
        for (Pregunta pregunta : preguntas) {
            todasLasRespuestas.addAll(pregunta.getRespuestas());
        }

        // Usamos una sola clave para todo el test
        session.setAttribute("respuestasTest_" + idTest, todasLasRespuestas);

        return preguntas;
    }

    /**
     * Calcula la nota total comparando las respuestas seleccionadas por el usuario
     * con las respuestas correctas (almacenadas en la sesión).
     */
    @PostMapping("/calcularNota")
    public ResponseEntity<Map<String, Object>> calcularNota(
            @RequestBody EvaluacionDTO evaluacion,
            @SessionAttribute(name = "usuario", required = false) Usuario usuario,
            HttpSession session) throws SQLException {

        Map<String, Object> response = new HashMap<>();

        // Verificar que el usuario está autenticado
        if (usuario == null) {
            response.put("error", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        double notaTotal = calcularNotaTotal(evaluacion, session);

        // Guardar la puntuación en la base de datos
        Puntuacion nuevaPuntuacion = new Puntuacion(
                null,
                notaTotal,
                usuario.getIdUsuario(),
                evaluacion.getIdTest(),
                new java.util.Date()
        );

        Optional<Puntuacion> puntuacionGuardada = puntuacionDao.savePuntuacion(nuevaPuntuacion, session);

        if (puntuacionGuardada.isPresent()) {
            Puntuacion ultimaPuntuacion = puntuacionGuardada.get();
            // Se actualiza en sesión solo la nota del test actual
            String sessionKey = "ultima_puntuacion_" + evaluacion.getIdTest();
            session.setAttribute(sessionKey, ultimaPuntuacion);

            response.put("nota", ultimaPuntuacion.getNotaConseguida());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Error al guardar puntuación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cálculo de la nota total: recuperamos de la sesión TODAS las respuestas
     * (respuestasTest_{idTest}), filtramos solo las que el usuario haya marcado
     * y sumamos sus notas.
     */
    private double calcularNotaTotal(EvaluacionDTO evaluacion, HttpSession session) {
        if (evaluacion == null || evaluacion.getRespuestas() == null || evaluacion.getRespuestas().isEmpty()) {
            System.err.println("Error: evaluación o lista de respuestas vacía.");
            return 0.0;
        }

        // Respuestas que el usuario seleccionó
        List<Integer> idsSeleccionados = evaluacion.getRespuestas();

        // Recuperamos TODAS las respuestas del test desde la sesión
        List<Respuesta> respuestasDelTest = (List<Respuesta>)
                session.getAttribute("respuestasTest_" + evaluacion.getIdTest());

        // Si no hubiera nada en sesión (error o test no cargado)
        if (respuestasDelTest == null || respuestasDelTest.isEmpty()) {
            System.err.println("No se encontraron respuestas en sesión para el test: " + evaluacion.getIdTest());
            return 0.0;
        }

        // Filtramos solo las que coincidan con las seleccionadas por el usuario
        double sumaNotas = respuestasDelTest.stream()
                .filter(r -> idsSeleccionados.contains(r.getIdRespuesta()))
                .mapToDouble(Respuesta::getNota)
                .sum();

        return sumaNotas;
    }

    /**
     * Devuelve todas las respuestas del test (almacenadas en la sesión),
     * para marcarlas como correctas/incorrectas en el front.
     */
    @GetMapping("/obtenerRespuestasSesion")
    @ResponseBody
    public List<Respuesta> obtenerRespuestasSesion(@RequestParam("idTest") int idTest, HttpSession session) {
        List<Respuesta> respuestas = (List<Respuesta>) session.getAttribute("respuestasTest_" + idTest);
        return (respuestas != null) ? respuestas : new ArrayList<>();
    }

    @GetMapping("/limpiarSesion")
    public ResponseEntity<String> limpiarSesion(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Sesión limpiada");
    }

    /**
     * Retorna la última nota y la penúltima nota del usuario en este test, si existen.
     */
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
