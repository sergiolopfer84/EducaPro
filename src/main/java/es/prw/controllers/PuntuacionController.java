package es.prw.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Puntuacion;
import es.prw.models.Respuesta;
import es.prw.models.Usuario;
import es.prw.services.PuntuacionService;
import es.prw.services.ProgresoService; // ✅ Importar ProgresoService
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/puntuaciones")
public class PuntuacionController {

    @Autowired
    private PuntuacionService puntuacionService;

    @Autowired
    private ProgresoService progresoService; // ✅ Inyectar ProgresoService

    @PostMapping("/guardar")
    public ResponseEntity<Puntuacion> guardarPuntuacion(@RequestParam Integer idUsuario, @RequestParam int idTest, @RequestParam double nota) {
        Optional<Puntuacion> puntuacion = puntuacionService.savePuntuacion(idUsuario, idTest, nota);
        return puntuacion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    public List<Puntuacion> obtenerPuntuacionesUsuario(@PathVariable Integer idUsuario) {
        return puntuacionService.getPuntuacionesByUsuario(idUsuario);
    }

    @GetMapping("/materia/{idUsuario}/{idMateria}")
    public List<Puntuacion> obtenerPuntuacionesMateria(@PathVariable Integer idUsuario, @PathVariable int idMateria) {
        return puntuacionService.getPuntuacionesPorMateria(idUsuario, idMateria);
    }

    @GetMapping("/test/{idUsuario}/{idTest}")
    public List<Double> obtenerUltimasPuntuacionesTest(@PathVariable Integer idUsuario, @PathVariable int idTest) {
        return puntuacionService.getUltimasPuntuacionesByTest(idUsuario, idTest);
    }

    @GetMapping("/ultimaPuntuacion")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUltimaPuntuacion(
            @SessionAttribute(name = "usuario", required = false) Usuario usuario,
            @RequestParam("idTest") int idTest) {

        Map<String, Object> response = new HashMap<>();

        if (usuario == null) {
            response.put("error", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<Double> notas = puntuacionService.getUltimasPuntuacionesByTest(usuario.getIdUsuario() , idTest);

        response.put("ultimaNota", notas.isEmpty() ? null : notas.get(0));
        response.put("penultimaNota", notas.size() > 1 ? notas.get(1) : null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/calcularNota")
    public ResponseEntity<Map<String, Object>> calcularNota(
            @RequestBody EvaluacionDTO evaluacion,
            @SessionAttribute(name = "usuario", required = false) Usuario usuario,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (usuario == null) {
            response.put("error", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        double notaTotal = calcularNotaTotal(evaluacion, session);

        Optional<Puntuacion> puntuacionGuardada = puntuacionService.savePuntuacion(
                usuario.getIdUsuario() ,
                evaluacion.getIdTest(),
                notaTotal
        );

        return puntuacionGuardada.map(p -> {
            response.put("nota", p.getNotaConseguida());
            return ResponseEntity.ok(response);
        }).orElseGet(() -> {
            response.put("error", "Error al guardar la puntuación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        });
    }

    private double calcularNotaTotal(EvaluacionDTO evaluacion, HttpSession session) {
        if (evaluacion == null || evaluacion.getRespuestas() == null || evaluacion.getRespuestas().isEmpty()) {
            System.err.println("⚠️ Error: evaluación o lista de respuestas vacía.");
            return 0.0;
        }

        List<Respuesta> respuestasDelTest = (List<Respuesta>) session.getAttribute("respuestasTest_" + evaluacion.getIdTest());

        if (respuestasDelTest == null || respuestasDelTest.isEmpty()) {
            System.err.println("⚠️ No se encontraron respuestas en sesión para el test: " + evaluacion.getIdTest());
            return 0.0;
        }

        return respuestasDelTest.stream()
                .filter(r -> evaluacion.getRespuestas().contains(r.getIdRespuesta()))
                .mapToDouble(Respuesta::getNota)
                .sum();
    }
    
    @GetMapping("/progresoTests")
    public ResponseEntity<Map<String, Map<String, List<Double>>>> obtenerProgresoTests(
            @SessionAttribute(name = "usuario", required = false) Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Map<String, Map<String, List<Double>>> progresoTests = progresoService.obtenerProgresoTests(usuario.getIdUsuario());
        return ResponseEntity.ok(progresoTests);
    }
}
