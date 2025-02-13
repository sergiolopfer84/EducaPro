package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.prw.models.Pregunta;
import es.prw.models.Respuesta;
import es.prw.services.PreguntaService;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/preguntas")

public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @GetMapping("/test/{idTest}")
    public ResponseEntity<List<Pregunta>> obtenerPreguntasPorTest(
            @PathVariable int idTest,
            HttpSession session) {

        List<Pregunta> preguntas = preguntaService.getPreguntasConRespuestas(idTest);

        // Igual que en el antiguo, meter en sesión
        List<Respuesta> todas = new ArrayList<>();
        for (Pregunta preg : preguntas) {
            todas.addAll(preg.getRespuestas());
        }
        session.setAttribute("respuestasTest_" + idTest, todas);

        return ResponseEntity.ok(preguntas);
    }

}
