package es.prw.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public OpenAIService(RestTemplate restTemplate) {  // Inyección por constructor
        this.restTemplate = restTemplate;
    }

    public String obtenerRespuestaIA(Map<String, Object> datosChat) {
        String nombreUsuario = (String) datosChat.get("usuario");
        String mensajeUsuario = (String) datosChat.get("mensaje");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Construcción del cuerpo de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "Eres un tutor académico que ayuda a los estudiantes."),
            Map.of("role", "user", "content", "Mi nombre es " + nombreUsuario + "."),
            Map.of("role", "user", "content", mensajeUsuario)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || !body.containsKey("choices")) {
                return "Error: respuesta inesperada de OpenAI.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
            if (choices.isEmpty()) {
                return "Error: la IA no generó ninguna respuesta.";
            }

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

            return (String) message.get("content");

        } catch (HttpStatusCodeException e) {
            return "Error en la solicitud a OpenAI: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Error al procesar la respuesta de OpenAI: " + e.getMessage();
        }
    }
}
