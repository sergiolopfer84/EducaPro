package es.prw.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.EvaluacionDTO;
import es.prw.models.Puntuacion;
import es.prw.models.Respuesta;
import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.PuntuacionService;
import es.prw.services.ProgresoService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;

import java.util.*;

@RestController
@RequestMapping("/puntuaciones")
public class PuntuacionController {

    private final PuntuacionService puntuacionService;
    private final ProgresoService progresoService;
    private final UsuarioRepository usuarioRepository;

    public PuntuacionController(PuntuacionService puntuacionService, ProgresoService progresoService, UsuarioRepository usuarioRepository) {
        this.puntuacionService = puntuacionService;
        this.progresoService = progresoService;
        this.usuarioRepository = usuarioRepository;
    }

    // ✅ Guardar puntuación de un test
    @PostMapping("/guardar")
    public ResponseEntity<Map<String, String>> guardarPuntuacion(@RequestParam Integer idUsuario, @RequestParam int idTest, @RequestParam double nota) {
        return puntuacionService.savePuntuacion(idUsuario, idTest, nota)
                .map(p -> ResponseEntity.ok(Map.of("message", "Puntuación guardada correctamente.")))
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "No se pudo guardar la puntuación.")));
    }

    // ✅ Obtener todas las puntuaciones de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Puntuacion>> obtenerPuntuacionesUsuario(@PathVariable Integer idUsuario) {
        List<Puntuacion> puntuaciones = puntuacionService.getPuntuacionesByUsuario(idUsuario);
        return puntuaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(puntuaciones);
    }

    // ✅ Obtener puntuaciones de un usuario en una materia
    @GetMapping("/materia/{idUsuario}/{idMateria}")
    public ResponseEntity<List<Puntuacion>> obtenerPuntuacionesMateria(@PathVariable Integer idUsuario, @PathVariable int idMateria) {
        List<Puntuacion> puntuaciones = puntuacionService.getPuntuacionesPorMateria(idUsuario, idMateria);
        return puntuaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(puntuaciones);
    }

    // ✅ Obtener últimas puntuaciones de un test (para un usuario)
    @GetMapping("/test/{idTest}")
    public ResponseEntity<List<Double>> obtenerUltimasPuntuacionesTest(HttpSession session, @PathVariable int idTest) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Double> notas = puntuacionService.getUltimasPuntuacionesByTest(usuario.getIdUsuario(), idTest);
        return notas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notas);
    }

    // ✅ Obtener última y penúltima puntuación de un test
    @GetMapping("/ultimaPuntuacion")
    public ResponseEntity<Map<String, Object>> getUltimaPuntuacion(@RequestParam int idTest, Authentication authentication) {
        Usuario usuario = obtenerUsuarioDesdeAuth(authentication);

        List<Double> notas = puntuacionService.getUltimasPuntuacionesByTest(usuario.getIdUsuario(), idTest);

        Map<String, Object> response = Map.of(
                "ultimaNota", notas.isEmpty() ? null : notas.get(0),
                "penultimaNota", notas.size() > 1 ? notas.get(1) : null
        );

        return ResponseEntity.ok(response);
    }

    // ✅ Evaluar respuestas de un test y guardar la puntuación

    @PostMapping("/calcularNota")
    public ResponseEntity<Map<String, Object>> calcularNota(
            @RequestBody EvaluacionDTO evaluacion,
            HttpSession session)
    {
        // 1. Verificar que haya autenticación
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario no autenticado"));
        }

        // 2. Extraer el principal como User (de Spring Security)
        org.springframework.security.core.userdetails.User springUser =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        String email = springUser.getUsername();

        // 3. Buscar tu entidad Usuario real con ese email
        Usuario usuarioReal = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        // 4. Recuperar las respuestas en la sesión
        @SuppressWarnings("unchecked")
        List<Respuesta> respuestasDelTest =
                (List<Respuesta>) session.getAttribute("respuestasTest_" + evaluacion.getIdTest());
        if (respuestasDelTest == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No se encontraron respuestas en sesión para este test"));
        }

        // 5. Calcular la nota filtrando las elegidas
        double notaTotal = 0.0;
        for (Respuesta r : respuestasDelTest) {
            if (evaluacion.getRespuestas().contains(r.getIdRespuesta())) {
                notaTotal += r.getNota();
            }
        }

        // 6. Guardar la puntuación en la BD (asumiendo que tienes un método savePuntuacion)
        var optPuntuacion = puntuacionService.savePuntuacion(
                usuarioReal.getIdUsuario(),
                evaluacion.getIdTest(),
                notaTotal
        );

        // 7. Construir la respuesta
        if (optPuntuacion.isPresent()) {
            return ResponseEntity.ok(Map.of("nota", optPuntuacion.get().getNotaObtenida()));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar la puntuación"));
        }
    }


    // ✅ Obtener progreso en tests de un usuario
    @GetMapping("/progresoTests")
    public ResponseEntity<Map<String, Map<String, List<Double>>>> obtenerProgresoTests(Authentication authentication) {
        Usuario usuario = obtenerUsuarioDesdeAuth(authentication);
        return ResponseEntity.ok(progresoService.obtenerProgresoTests(usuario.getIdUsuario()));
    }

    // 🔹 Método privado para obtener usuario autenticado
    private Usuario obtenerUsuarioDesdeAuth(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }
}
