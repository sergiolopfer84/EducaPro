package es.prw.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Respuesta;
import es.prw.services.RespuestaService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/respuestas")
public class RespuestaController {

    @Autowired
    private RespuestaService respuestaService;

    @GetMapping("/pregunta/{idPregunta}")
    public List<Respuesta> obtenerRespuestasPorPregunta(@PathVariable int idPregunta) {
        return respuestaService.getRespuestasByPregunta(idPregunta);
    }

    @PostMapping("/ids")
    public List<Respuesta> obtenerRespuestasPorIds(@RequestBody List<Integer> idsRespuestas) {
        return respuestaService.getRespuestasByIds(idsRespuestas);
    }
    
    @PostMapping("/evaluar")
    public ResponseEntity<Double> evaluarRespuestas(@RequestBody EvaluacionDTO evaluacionDTO) {
        double nota = respuestaService.evaluarRespuestas(evaluacionDTO);
        return ResponseEntity.ok(nota);
    }
    
    @SuppressWarnings("unchecked")
	@GetMapping("/obtenerRespuestasSesion")
    @ResponseBody
    public List<Respuesta> obtenerRespuestasSesion(@RequestParam("idTest") int idTest, HttpSession session) {
        List<Respuesta> respuestas = (List<Respuesta>) session.getAttribute("respuestasTest_" + idTest);
        return (respuestas != null) ? respuestas : new ArrayList<>();
    }

}
