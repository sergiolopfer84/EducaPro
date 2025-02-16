package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.TestDTO;
import es.prw.models.*;
import es.prw.services.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ROLE_ADMIN')") // Solo accesible para administradores
public class AdminController {

    private final MateriaService materiaService;
    private final TestService testService;
    private final PreguntaService preguntaService;
    private final RespuestaService respuestaService;
    private final UsuarioService usuarioService;

    public AdminController(
            MateriaService materiaService, TestService testService,
            PreguntaService preguntaService, RespuestaService respuestaService,
            UsuarioService usuarioService) {
        this.materiaService = materiaService;
        this.testService = testService;
        this.preguntaService = preguntaService;
        this.respuestaService = respuestaService;
        this.usuarioService = usuarioService;
    }

    // ============================
    // 📌 CRUD MATERIAS
    // ============================

    @PostMapping("/materias")
    public ResponseEntity<Materia> crearMateria(@RequestBody Materia materia) {
        return ResponseEntity.ok(materiaService.guardarMateria(materia));
    }

    @PutMapping("/materias/{id}")
    public ResponseEntity<Materia> actualizarMateria(@PathVariable int id, @RequestBody Materia materia) {
        return ResponseEntity.ok(materiaService.actualizarMateria(id, materia));
    }

    @DeleteMapping("/materias/{id}")
    public ResponseEntity<Void> eliminarMateria(@PathVariable int id) {
        materiaService.eliminarMateria(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/materias/{id}/toggle-activa")
    public ResponseEntity<Void> toggleEstadoMateria(@PathVariable int id) {
        materiaService.toggleEstado(id); // 🔹 Llamamos al servicio pero no intentamos devolver nada
        return ResponseEntity.noContent().build(); // ✅ Devuelve 204 No Content
    }

    @GetMapping("/materias/activas")
    public ResponseEntity<List<Materia>> obtenerMateriasActivas() {
        return ResponseEntity.ok(materiaService.obtenerMateriasActivas());
    }

    @GetMapping("/materias/inactivas")
    public ResponseEntity<List<Materia>> obtenerMateriasInactivas() {
        return ResponseEntity.ok(materiaService.obtenerMateriasInactivas());
    }

    // ============================
    // 📌 CRUD TESTS
    // ============================

    @PostMapping("/tests")
    public ResponseEntity<Test> crearTest(@RequestBody TestDTO testDTO) {
        Materia materia = materiaService.buscarPorId(testDTO.getIdMateria());
        if (materia == null) {
            return ResponseEntity.badRequest().build();
        }

        Test nuevoTest = new Test();
        nuevoTest.setNombreTest(testDTO.getNombreTest());
        nuevoTest.setMateria(materia);
        nuevoTest.setActivo(testDTO.isActiva());

        return ResponseEntity.ok(testService.guardarTest(nuevoTest));
    }

    @PutMapping("/tests/{id}")
    public ResponseEntity<Test> actualizarTest(@PathVariable int id, @RequestBody Test test) {
        return ResponseEntity.ok(testService.actualizarTest(id, test));
    }

    @DeleteMapping("/tests/{id}")
    public ResponseEntity<Void> eliminarTest(@PathVariable int id) {
        testService.eliminarTest(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tests/{id}/toggle-activa")
    public ResponseEntity<Test> toggleEstadoTest(@PathVariable int id) {
        return ResponseEntity.ok(testService.toggleEstado(id));
    }

    // ============================
    // 📌 CRUD PREGUNTAS
    // ============================

    @PostMapping("/preguntas")
    public ResponseEntity<Pregunta> crearPregunta(@RequestBody Pregunta pregunta) {
        return ResponseEntity.ok(preguntaService.crearPregunta(pregunta));
    }

    @PutMapping("/preguntas/{id}")
    public ResponseEntity<Pregunta> actualizarPregunta(@PathVariable int id, @RequestBody Pregunta pregunta) {
        return ResponseEntity.ok(preguntaService.actualizarPregunta(id, pregunta));
    }

    @DeleteMapping("/preguntas/{id}")
    public ResponseEntity<Void> eliminarPregunta(@PathVariable int id) {
        preguntaService.eliminarPregunta(id);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // 📌 CRUD RESPUESTAS
    // ============================

    @PostMapping("/respuestas")
    public ResponseEntity<Respuesta> crearRespuesta(@RequestBody Respuesta respuesta) {
        return ResponseEntity.ok(respuestaService.crearRespuesta(respuesta));
    }

    @PutMapping("/respuestas/{id}")
    public ResponseEntity<Respuesta> actualizarRespuesta(@PathVariable int id, @RequestBody Respuesta respuesta) {
        return ResponseEntity.ok(respuestaService.actualizarRespuesta(id, respuesta));
    }

    @DeleteMapping("/respuestas/{id}")
    public ResponseEntity<Void> eliminarRespuesta(@PathVariable int id) {
        respuestaService.eliminarRespuesta(id);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // 📌 CRUD USUARIOS
    // ============================

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }
}
