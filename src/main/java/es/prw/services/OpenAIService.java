package es.prw.services;

import es.prw.connection.MySqlConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {

    private final String apiKey;
    private final String apiUrl;

    // Para hacer las peticiones HTTP
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Inyectamos MySqlConnection para obtener las variables
     * que Dotenv cargó del .env (OPENAI_API_KEY, OPENAI_API_URL).
     */
    @Autowired
    public OpenAIService(MySqlConnection mySqlConnection) {
        this.apiKey = mySqlConnection.getOpenAiApiKey();
        this.apiUrl = mySqlConnection.getOpenAiApiUrl();
    }

    /**
     * Método principal para enviar un mensaje y contexto (notas) a la IA
     * y devolver la respuesta de OpenAI.
     *
     * @param datosChat Mapa con datos como usuario, mensaje y notas
     * @return Respuesta generada por la IA
     */
    public String obtenerRespuestaIA(Map<String, Object> datosChat) {
        String nombreUsuario = (String) datosChat.get("usuario");
        String mensajeUsuario = (String) datosChat.get("mensaje");
        Map<String, Map<String, List<Double>>> notasPorMateria =
                (Map<String, Map<String, List<Double>>>) datosChat.get("notasPorMateria");

        // Construimos el historial de notas para la IA
        StringBuilder historialNotas = new StringBuilder();
        historialNotas.append("Historial de notas de ").append(nombreUsuario).append(":\n");

        if (notasPorMateria != null && !notasPorMateria.isEmpty()) {
            for (Map.Entry<String, Map<String, List<Double>>> entryMateria : notasPorMateria.entrySet()) {
                historialNotas.append("\n📘 Asignatura: ").append(entryMateria.getKey()).append("\n");

                for (Map.Entry<String, List<Double>> entryTest : entryMateria.getValue().entrySet()) {
                    historialNotas.append("   📌 Test: ").append(entryTest.getKey()).append("\n");
                    List<Double> notas = entryTest.getValue();
                    historialNotas.append("      🔹 Nota más alta: ").append(Collections.max(notas)).append("\n");
                    historialNotas.append("      🔹 Nota más baja: ").append(Collections.min(notas)).append("\n");
                    double promedio = notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    historialNotas.append("      🔹 Promedio: ").append(String.format("%.2f", promedio)).append("\n");
                }
            }
        } else {
            historialNotas.append("No hay registros de notas.\n");
        }

        // Cabeceras de la petición a OpenAI
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Cuerpo de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content",
                   "Eres un tutor académico que ayuda a los estudiantes basándose en sus notas previas."),
            Map.of("role", "user", "content", "Mi nombre es " + nombreUsuario + "."),
            Map.of("role", "user", "content", historialNotas.toString()),
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

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        return (String) message.get("content");
    }
}
