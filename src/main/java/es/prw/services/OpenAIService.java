package es.prw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    @Autowired
    public OpenAIService() {
        this.restTemplate = new RestTemplate();
    }

    public String obtenerRespuestaIA(Map<String, Object> datosChat) {
        String nombreUsuario = (String) datosChat.get("usuario");
        String mensajeUsuario = (String) datosChat.get("mensaje");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Construimos el cuerpo de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "Eres un tutor académico que ayuda a los estudiantes."),
            Map.of("role", "user", "content", "Mi nombre es " + nombreUsuario + "."),
            Map.of("role", "user", "content", mensajeUsuario)
        ));

        // Construcción de la petición
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Llamada a la API de OpenAI
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

        // Extraer la respuesta de la IA
        Map<String, Object> body = response.getBody();
        if (body == null) {
            return "Error: respuesta vacía de OpenAI.";
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "Error: no se recibieron 'choices' de OpenAI.";
        }

        // ✅ Aplicamos cast explícito para evitar errores de tipo
        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        
        return (String) message.get("content");
    }

}


