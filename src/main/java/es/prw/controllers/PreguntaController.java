package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Pregunta;
import es.prw.models.Respuesta;
import es.prw.services.PreguntaService;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/preguntas")
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    // ✅ Obtener todas las preguntas
    @GetMapping
    public ResponseEntity<List<Pregunta>> obtenerPreguntas() {
        return ResponseEntity.ok(preguntaService.getPreguntas());
    }

    // ✅ Obtener preguntas de un test específico y almacenar respuestas en sesión
    @GetMapping("/test/{idTest}")
    public ResponseEntity<List<Pregunta>> obtenerPreguntasPorTest(
            @PathVariable int idTest,
            HttpSession session) {

        List<Pregunta> preguntas = preguntaService.getPreguntasConRespuestas(idTest);

        // Guardar respuestas en sesión de forma más eficiente
        List<Respuesta> respuestas = preguntas.stream()
                .flatMap(p -> p.getRespuestas().stream())
                .collect(Collectors.toList());

        session.setAttribute("respuestasTest_" + idTest, respuestas);

        return ResponseEntity.ok(preguntas);
    }
}
