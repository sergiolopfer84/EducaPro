package es.prw.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import es.prw.models.Pregunta;
import es.prw.services.PreguntaService;
import java.util.List;

@RestController
@RequestMapping("/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    @GetMapping("/test/{idTest}")
    public List<Pregunta> obtenerPreguntasPorTest(@PathVariable int idTest) {
        return preguntaService.getPreguntasConRespuestas(idTest);
    }
}
