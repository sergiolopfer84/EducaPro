package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Respuesta;
import es.prw.services.RespuestaService;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @GetMapping("/pregunta/{idPregunta}")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasPorPregunta(@PathVariable int idPregunta) {
        List<Respuesta> respuestas = respuestaService.getRespuestasByPregunta(idPregunta);
        return respuestas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(respuestas);
    }

    @PostMapping("/ids")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasPorIds(@RequestBody List<Integer> idsRespuestas) {
        List<Respuesta> respuestas = respuestaService.getRespuestasByIds(idsRespuestas);
        return respuestas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(respuestas);
    }

    @PostMapping("/evaluar")
    public ResponseEntity<Double> evaluarRespuestas(@RequestBody EvaluacionDTO evaluacionDTO) {
        double nota = respuestaService.evaluarRespuestas(evaluacionDTO);
        return ResponseEntity.ok(nota);
    }

    @GetMapping("/obtenerRespuestasSesion")
    public ResponseEntity<List<Respuesta>> obtenerRespuestasSesion(@RequestParam("idTest") int idTest, HttpSession session) {
        Object respuestasObj = session.getAttribute("respuestasTest_" + idTest);
        
        if (respuestasObj instanceof List<?>) {
            List<?> respuestasList = (List<?>) respuestasObj;
            
            if (!respuestasList.isEmpty() && respuestasList.get(0) instanceof Respuesta) {
                return ResponseEntity.ok((List<Respuesta>) respuestasList);
            }
        }

        return ResponseEntity.noContent().build();
    }
}
