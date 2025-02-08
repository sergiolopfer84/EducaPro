package es.prw.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String obtenerRespuestaIA(String mensajeUsuario) {
        // 1. Cabeceras con la clave de API
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // 2. Cuerpo de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");  // Cambia a "gpt-4" si tienes acceso
        // Usamos List.of(...) para las "messages" en vez de un array
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "Eres un tutor académico que ayuda a los estudiantes."),
            Map.of("role", "user", "content", mensajeUsuario)
        ));

        // 3. Construir la petición
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 4. Hacer la petición a OpenAI
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

        // 5. Parsear la respuesta

        // Obtenemos todo el cuerpo
        Map<String, Object> responseBody = response.getBody();

        // "choices" es una lista de Map
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        // Tomamos el primer objeto de la lista
        Map<String, Object> firstChoice = choices.get(0);

        // El "message" es un Map con "role" y "content"
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        // El contenido del mensaje está en "content"
        String content = (String) message.get("content");

        // 6. Devolver la respuesta de la IA
        return content;
    }
}
