package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.MateriaDTO;
import es.prw.dtos.PreguntaDTO;
import es.prw.dtos.RespuestaDTO;
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
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaDTO>> obtenerMateriasDTO() {
        return ResponseEntity.ok(materiaService.getMateriasDTO());
    }

    @PostMapping("/materias")
    public ResponseEntity<Materia> crearMateria(@RequestBody Materia materia) {
        System.out.println("📩 Recibiendo solicitud para crear materia: " + materia);

        if (materia.getNombreMateria() == null || materia.getNombreMateria().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null); // ⚠ Evita insertar valores nulos o vacíos
        }

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

    @PutMapping("/materias/{id}/toggle-activa")
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
    @GetMapping("/tests")
    public ResponseEntity<List<TestDTO>> obtenerTestsDTO() {
        return ResponseEntity.ok(testService.getTestsDTO());
    }

    @PostMapping("/tests")
    public ResponseEntity<Test> crearTest(@RequestBody TestDTO testDTO) {
        Materia materia = materiaService.buscarPorId(testDTO.getIdMateria());
        if (materia == null) {
            return ResponseEntity.badRequest().build();
        }

        Test nuevoTest = new Test();
        nuevoTest.setNombreTest(testDTO.getNombreTest());
        nuevoTest.setMateria(materia);
        nuevoTest.setActiva(testDTO.isActiva());

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

    @PutMapping("/tests/{id}/toggle-activa")
    public ResponseEntity<Test> toggleEstadoTest(@PathVariable int id) {
        return ResponseEntity.ok(testService.toggleEstado(id));
    }

    // ============================
    // 📌 CRUD PREGUNTAS
    // ============================
    
    
    @GetMapping("/preguntas")
    public ResponseEntity<List<PreguntaDTO>> obtenerPreguntasDTO() {
        return ResponseEntity.ok(preguntaService.getPreguntasDTO());
    }

    @PostMapping("/preguntas")
    public ResponseEntity<Pregunta> crearPregunta(@RequestBody PreguntaDTO preguntaDTO) {
        Pregunta preguntaGuardada = preguntaService.crearPreguntaDesdeDTO(preguntaDTO);
        return ResponseEntity.ok(preguntaGuardada);
    }


    @PutMapping("/preguntas/{id}")
    public ResponseEntity<Pregunta> actualizarPregunta(@PathVariable int id, @RequestBody PreguntaDTO preguntaDTO) {
        Pregunta preguntaActualizada = preguntaService.actualizarPreguntaDesdeDTO(id, preguntaDTO);
        return ResponseEntity.ok(preguntaActualizada);
    }


    @DeleteMapping("/preguntas/{id}")
    public ResponseEntity<Void> eliminarPregunta(@PathVariable int id) {
        preguntaService.eliminarPregunta(id);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // 📌 CRUD RESPUESTAS
    // ============================
    @GetMapping("/respuestas")
    public ResponseEntity<List<RespuestaDTO>> obtenerRespuestasDTO() {
        return ResponseEntity.ok(respuestaService.getRespuestasDTO());
    }

    @PostMapping("/respuestas")
    public ResponseEntity<Respuesta> crearRespuesta(@RequestBody RespuestaDTO respuestaDTO) {
        Respuesta respuestaGuardada = respuestaService.crearRespuestaDesdeDTO(respuestaDTO);
        return ResponseEntity.ok(respuestaGuardada);
    }

    @PutMapping("/respuestas/{id}")
    public ResponseEntity<Respuesta> actualizarRespuesta(@PathVariable Integer id,
                                                         @RequestBody RespuestaDTO respuestaDTO) {
        Respuesta respuestaActualizada = respuestaService.actualizarRespuestaDesdeDTO(id, respuestaDTO);
        return ResponseEntity.ok(respuestaActualizada);
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
