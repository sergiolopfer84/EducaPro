package es.prw.controllers;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.OpenAIService;
import es.prw.services.ProgresoService;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/asistente")
public class AsistenteController {

    private final OpenAIService openAIService;
    private final ProgresoService progresoService;
    private final UsuarioRepository usuarioRepository;
    // Inyección de dependencias por constructor
    public AsistenteController(OpenAIService openAIService, ProgresoService progresoService,UsuarioRepository usuarioRepository) {
        this.openAIService = openAIService;
        this.progresoService = progresoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public String obtenerRespuesta(@RequestBody String mensaje, Authentication authentication) {
    	  org.springframework.security.core.userdetails.User springUser =
                  (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
          String email = springUser.getUsername();

          // 3. Buscar tu entidad Usuario real con ese email
          Usuario usuarioReal = usuarioRepository.findByEmail(email)
                  .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "Por favor, introduce un mensaje válido.";
        }

        Integer idUsuario = usuarioReal.getIdUsuario();

        // Obtener progreso de materias
        List<MateriaProgresoDTO> progresoMaterias = progresoService.obtenerProgresoMaterias(idUsuario);

        // Convertir List<MateriaProgresoDTO> a Map<String, Map<String, List<Double>>>
        Map<String, Map<String, List<Double>>> notasPorMateria = progresoMaterias.stream()
                .collect(Collectors.toMap(
                        MateriaProgresoDTO::getMateria,
                        materia -> Map.of("Resumen", List.of((double) materia.getTestsAprobados(), (double) materia.getTotalTests()))
                ));

        // Crear objeto con datos para IA
        Map<String, Object> datosChat = new HashMap<>();
        datosChat.put("usuario", usuarioReal.getNombre());
        datosChat.put("mensaje", mensaje);
        datosChat.put("notasPorMateria", notasPorMateria);

        return openAIService.obtenerRespuestaIA(datosChat);
    }
}
