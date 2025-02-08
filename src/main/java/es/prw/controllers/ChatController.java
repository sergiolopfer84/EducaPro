package es.prw.controllers;

import es.prw.services.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin // Permitir solicitudes desde el frontend
public class ChatController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping
    public String obtenerRespuesta(@RequestBody String mensaje) {
        return openAIService.obtenerRespuestaIA(mensaje);
    }
}
