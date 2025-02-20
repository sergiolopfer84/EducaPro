package es.prw.services;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import java.util.Map;
import java.util.List;

@Service
public class OpenAIService {

    private final String apiKey;
    private final String apiUrl;
    private final RestTemplate restTemplate;

    public OpenAIService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.apiKey = dotenv.get("OPENAI_API_KEY");
        this.apiUrl = dotenv.get("OPENAI_API_URL");
        this.restTemplate = new RestTemplate();

        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException("Error: API Key de OpenAI no encontrada en .env");
        }
        if (this.apiUrl == null || this.apiUrl.isBlank()) {
            throw new IllegalStateException("Error: API URL de OpenAI no encontrada en .env");
        }
    }

    public String obtenerRespuestaIA(Map<String, Object> datosChat) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4");

            // Construimos la lista de mensajes que recibe la IA
            // Añadimos "infoProgreso" o cualquier campo que quieras enviar
            requestBody.put("messages", List.of(
                Map.of("role", "system", "content",
                       "Eres un tutor académico que ayuda a los estudiantes basándose en sus notas previas."),
                Map.of("role", "user", "content", "Mi nombre es " + datosChat.get("usuario")),
                Map.of("role", "user", "content", datosChat.get("infoProgreso")),
                Map.of("role", "user", "content", datosChat.get("mensaje"))
            ));

            requestBody.put("max_tokens", 400);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            JSONObject jsonResponse = new JSONObject(response.getBody());

            // Extraemos el contenido devuelto por la IA
            return jsonResponse.getJSONArray("choices")
                               .getJSONObject(0)
                               .getJSONObject("message")
                               .getString("content");

        } catch (Exception e) {
            return "Error al obtener respuesta de OpenAI: " + e.getMessage();
        }
    }

}