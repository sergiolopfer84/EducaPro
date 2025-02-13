/*
 * package es.prw.services;
 * 
 * import org.springframework.beans.factory.annotation.Value; import
 * org.springframework.http.*; import org.springframework.stereotype.Service;
 * import org.springframework.web.client.RestTemplate; import
 * org.springframework.web.client.HttpStatusCodeException; import java.util.*;
 * 
 * @Service public class OpenAIService {
 * 
 * @Value("${openai.api.key}") private String apiKey;
 * 
 * @Value("${openai.api.url}") private String apiUrl;
 * 
 * private final RestTemplate restTemplate;
 * 
 * public OpenAIService(RestTemplate restTemplate) { // Inyección por
 * constructor this.restTemplate = restTemplate; }
 * 
 * public String obtenerRespuestaIA(Map<String, Object> datosChat) { String
 * nombreUsuario = (String) datosChat.get("usuario"); String mensajeUsuario =
 * (String) datosChat.get("mensaje");
 * 
 * HttpHeaders headers = new HttpHeaders(); headers.set("Authorization",
 * "Bearer " + apiKey); headers.set("Content-Type", "application/json");
 * 
 * // Construcción del cuerpo de la petición Map<String, Object> requestBody =
 * new HashMap<>(); requestBody.put("model", "gpt-3.5-turbo");
 * requestBody.put("messages", List.of( Map.of("role", "system", "content",
 * "Eres un tutor académico que ayuda a los estudiantes."), Map.of("role",
 * "user", "content", "Mi nombre es " + nombreUsuario + "."), Map.of("role",
 * "user", "content", mensajeUsuario) ));
 * 
 * HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody,
 * headers);
 * 
 * try { ResponseEntity<Map> response = restTemplate.exchange(apiUrl,
 * HttpMethod.POST, request, Map.class); Map<String, Object> body =
 * response.getBody();
 * 
 * if (body == null || !body.containsKey("choices")) { return
 * "Error: respuesta inesperada de OpenAI."; }
 * 
 * List<Map<String, Object>> choices = (List<Map<String, Object>>)
 * body.get("choices"); if (choices.isEmpty()) { return
 * "Error: la IA no generó ninguna respuesta."; }
 * 
 * Map<String, Object> firstChoice = choices.get(0); Map<String, Object> message
 * = (Map<String, Object>) firstChoice.get("message");
 * 
 * return (String) message.get("content");
 * 
 * } catch (HttpStatusCodeException e) { return
 * "Error en la solicitud a OpenAI: " + e.getStatusCode() + " - " +
 * e.getResponseBodyAsString(); } catch (Exception e) { return
 * "Error al procesar la respuesta de OpenAI: " + e.getMessage(); } } }
 */

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
            // Construir el JSON para la petición a OpenAI
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4"); // Modelo a utilizar
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "Eres un asistente de aprendizaje personalizado."),
                    Map.of("role", "user", "content", datosChat.get("mensaje"))
            ));
            requestBody.put("max_tokens", 150);

            // Encabezados HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            // Hacer la petición a OpenAI
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Extraer respuesta de OpenAI
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        } catch (Exception e) {
            return "Error al obtener respuesta de OpenAI: " + e.getMessage();
        }
    }
}
