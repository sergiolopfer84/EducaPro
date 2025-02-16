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

    // ✅ Obtener respuestas por pregunta
    @GetMapping("/pregunta/{idPregunta}")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasPorPregunta(@PathVariable int idPregunta) {
        List<Respuesta> respuestas = respuestaService.getRespuestasByPregunta(idPregunta);
        return respuestas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(respuestas);
    }

    // ✅ Obtener respuestas por múltiples IDs
    @PostMapping("/ids")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasPorIds(@RequestBody List<Integer> idsRespuestas) {
        List<Respuesta> respuestas = respuestaService.getRespuestasByIds(idsRespuestas);
        return respuestas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(respuestas);
    }

    // ✅ Evaluar respuestas de un test
    @PostMapping("/evaluar")
    public ResponseEntity<Double> evaluarRespuestas(@RequestBody EvaluacionDTO evaluacionDTO) {
        return ResponseEntity.ok(respuestaService.evaluarRespuestas(evaluacionDTO));
    }

    // ✅ Obtener respuestas de sesión de un test
    @GetMapping("/sesion")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasSesion(@RequestParam("idTest") int idTest, HttpSession session) {
        List<?> respuestasList = (List<?>) session.getAttribute("respuestasTest_" + idTest);

        if (respuestasList != null && !respuestasList.isEmpty() && respuestasList.get(0) instanceof Respuesta) {
            return ResponseEntity.ok((List<Respuesta>) respuestasList);
        }

        return ResponseEntity.noContent().build();
    }
}
