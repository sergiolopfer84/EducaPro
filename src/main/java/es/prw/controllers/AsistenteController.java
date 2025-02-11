package es.prw.controllers;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Usuario;
import es.prw.services.OpenAIService;
import es.prw.services.ProgresoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asistente")
@CrossOrigin
@SessionAttributes("usuario")
public class AsistenteController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ProgresoService progresoService;

    @PostMapping
    public String obtenerRespuesta(@RequestBody String mensaje, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "No estás autenticado. Inicia sesión para usar el asistente.";
        }

        String nombreUsuario = usuario.getNombre();
        Integer idUsuario = usuario.getIdUsuario();

        // Obtener progreso de materias
        List<MateriaProgresoDTO> progresoMaterias = progresoService.obtenerProgresoMaterias(idUsuario);

        // Convertir List<MateriaProgresoDTO> a Map<String, Map<String, List<Double>>>
        Map<String, Map<String, List<Double>>> notasPorMateria = new HashMap<>();

        for (MateriaProgresoDTO materia : progresoMaterias) {
            Map<String, List<Double>> notas = new HashMap<>();
            List<Double> listaNotas = new ArrayList<>();

            // Agregar la cantidad de tests aprobados y total como notas en la estructura deseada
            listaNotas.add((double) materia.getTestsAprobados()); // Ejemplo: Nota más alta
            listaNotas.add((double) materia.getTotalTests());     // Ejemplo: Total de tests

            notas.put("Resumen", listaNotas); // Puedes cambiar "Resumen" por otra clave si lo necesitas

            notasPorMateria.put(materia.getMateria(), notas);
        }

        // Crear objeto con datos para IA
        Map<String, Object> datosChat = new HashMap<>();
        datosChat.put("usuario", nombreUsuario);
        datosChat.put("mensaje", mensaje);
        datosChat.put("notasPorMateria", notasPorMateria);

        return openAIService.obtenerRespuestaIA(datosChat);
    }

}
