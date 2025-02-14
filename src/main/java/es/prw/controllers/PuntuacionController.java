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

    public PuntuacionController(PuntuacionService puntuacionService, ProgresoService progresoService,UsuarioRepository usuarioRepository) {
        this.puntuacionService = puntuacionService;
        this.progresoService = progresoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/guardar")
    public ResponseEntity<Map<String, String>> guardarPuntuacion(@RequestParam Integer idUsuario, @RequestParam int idTest, @RequestParam double nota) {
        Optional<Puntuacion> puntuacion = puntuacionService.savePuntuacion(idUsuario, idTest, nota);

        if (puntuacion.isPresent()) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Puntuación guardada correctamente."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "No se pudo guardar la puntuación."));
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Puntuacion>> obtenerPuntuacionesUsuario(@PathVariable Integer idUsuario) {
        List<Puntuacion> puntuaciones = puntuacionService.getPuntuacionesByUsuario(idUsuario);
        return puntuaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(puntuaciones);
    }

    @GetMapping("/materia/{idUsuario}/{idMateria}")
    public ResponseEntity<List<Puntuacion>> obtenerPuntuacionesMateria(@PathVariable Integer idUsuario, @PathVariable int idMateria) {
        List<Puntuacion> puntuaciones = puntuacionService.getPuntuacionesPorMateria(idUsuario, idMateria);
        return puntuaciones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(puntuaciones);
    }

    @GetMapping("/test/{idUsuario}/{idTest}")
    public ResponseEntity<List<Double>> obtenerUltimasPuntuacionesTest( HttpSession session, @PathVariable int idTest) {
    	  Usuario usuario = (Usuario) session.getAttribute("usuario");
        List<Double> notas = puntuacionService.getUltimasPuntuacionesByTest(usuario.getIdUsuario(), idTest);
        return notas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(notas);
    }

   

    // En tu método getUltimaPuntuacion
    @GetMapping("/ultimaPuntuacion")
    public ResponseEntity<?> getUltimaPuntuacion(@RequestParam int idTest, Authentication authentication) {
        // En vez de (Usuario) ...
        org.springframework.security.core.userdetails.User springUser =
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        String email = springUser.getUsername();
        
        // A partir de aquí, buscas al Usuario real en tu BD:
        Usuario usuarioReal = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        // Llamar a un método que retorne las 2 últimas notas
        List<Double> notas = puntuacionService.getUltimasPuntuacionesByTest(usuarioReal.getIdUsuario(), idTest);

        Map<String, Object> response = new HashMap<>();
        if (notas.isEmpty()) {
            response.put("ultimaNota", null);
            response.put("penultimaNota", null);
        } else if (notas.size() == 1) {
            response.put("ultimaNota", notas.get(0));
            response.put("penultimaNota", null);
        } else {
            response.put("ultimaNota", notas.get(0));
            response.put("penultimaNota", notas.get(1));
        }
        return ResponseEntity.ok(response);
    }

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



    private double calcularNotaTotal(EvaluacionDTO evaluacion, HttpSession session) {
        if (evaluacion == null || evaluacion.getRespuestas() == null || evaluacion.getRespuestas().isEmpty()) {
            return 0.0;
        }

        Object respuestasObj = session.getAttribute("respuestasTest_" + evaluacion.getIdTest());
        if (!(respuestasObj instanceof List<?>)) {
            return 0.0;
        }

        List<Respuesta> respuestasDelTest = (List<Respuesta>) respuestasObj;

        return respuestasDelTest.stream()
                .filter(r -> evaluacion.getRespuestas().contains(r.getIdRespuesta()))
                .mapToDouble(Respuesta::getNota)
                .sum();
    }

    @GetMapping("/progresoTests")
    public ResponseEntity<Map<String, Map<String, List<Double>>>> obtenerProgresoTests(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Obtiene el usuario autenticado en Spring Security
        org.springframework.security.core.userdetails.User springUser =
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        if (springUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyMap());
        }

        String email = springUser.getUsername(); // Extrae el email del usuario autenticado

        // Busca el usuario real en la base de datos
        Optional<Usuario> usuarioRealOpt = usuarioRepository.findByEmail(email);

        if (usuarioRealOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyMap());
        }

        Usuario usuarioReal = usuarioRealOpt.get();

        // Llama al servicio usando el ID del usuario autenticado
        Map<String, Map<String, List<Double>>> progresoTests = progresoService.obtenerProgresoTests(usuarioReal.getIdUsuario());

        return ResponseEntity.ok(progresoTests);
    }

}
