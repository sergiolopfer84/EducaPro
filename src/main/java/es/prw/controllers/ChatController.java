package es.prw.controllers;

import es.prw.models.Usuario;
import es.prw.models.Puntuacion;
import es.prw.models.Test;
import es.prw.daos.PuntuacionDao;
import es.prw.daos.TestDao;
import es.prw.services.OpenAIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
@SessionAttributes("usuario")
public class ChatController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private PuntuacionDao puntuacionDao;

    @Autowired
    private TestDao testDao;

    @PostMapping
    public String obtenerRespuesta(@RequestBody String mensaje, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "No estás autenticado. Inicia sesión para usar el asistente.";
        }

        String nombreUsuario = usuario.getNombre();
        int idUsuario = usuario.getIdUsuario();

        // Intentamos identificar si el usuario está preguntando por una materia específica
        String materiaConsultada = extraerMateriaDesdeMensaje(mensaje);
        int idMateria = (materiaConsultada != null) ? obtenerIdMateriaPorNombre(materiaConsultada) : -1;

        // Obtener las notas del usuario **solo de la materia consultada** (si se especificó una)
        List<Puntuacion> puntuaciones = (idMateria != -1) ?
                puntuacionDao.getPuntuacionesPorMateria(idUsuario, idMateria, session) :
                puntuacionDao.getPuntuacionesByUsuario(idUsuario, session);

        // Mapeo de notas organizadas por materia y test
        Map<String, Map<String, List<Double>>> notasPorMateria = new HashMap<>();

        for (Puntuacion puntuacion : puntuaciones) {
            // Obtener el test relacionado con la puntuación
            List<Test> tests = testDao.getTests(puntuacion.getIdTest());

            if (!tests.isEmpty()) {
                Test test = tests.get(0);
                String materia = "Desconocida";
                String nombreTest = test.getTest();

                if (test.getIdMateria() != 0) {
                    materia = obtenerNombreMateria(test.getIdMateria());
                }

                // Agregar la información al mapa de notas
                notasPorMateria.putIfAbsent(materia, new HashMap<>());
                notasPorMateria.get(materia).putIfAbsent(nombreTest, new java.util.ArrayList<>());
                notasPorMateria.get(materia).get(nombreTest).add(puntuacion.getNotaConseguida());
            }
        }

        // Crear el objeto con la información del usuario y su historial de notas
        Map<String, Object> datosChat = new HashMap<>();
        datosChat.put("usuario", nombreUsuario);
        datosChat.put("mensaje", mensaje);
        datosChat.put("notasPorMateria", notasPorMateria);

        return openAIService.obtenerRespuestaIA(datosChat);
    }

    private String extraerMateriaDesdeMensaje(String mensaje) {
        // Aquí podrías implementar un algoritmo más sofisticado para detectar la materia
        if (mensaje.toLowerCase().contains("geografía")) return "Geografía";
        if (mensaje.toLowerCase().contains("biología")) return "Biología";
        return null;
    }

    private int obtenerIdMateriaPorNombre(String materia) {
        // Implementar la lógica para obtener el ID de la materia desde la BD
        if (materia.equalsIgnoreCase("Geografía")) return 1;
        if (materia.equalsIgnoreCase("Biología")) return 2;
        return -1;
    }

    private String obtenerNombreMateria(int idMateria) {
        // Implementar la lógica para obtener el nombre de la materia por su ID
        return switch (idMateria) {
            case 1 -> "Geografía";
            case 2 -> "Biología";
            default -> "Desconocida";
        };
    }
}
