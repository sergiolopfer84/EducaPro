package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import es.prw.models.*;
import es.prw.services.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Solo accesible para administradores
public class AdminController {

    private final MateriaService materiaService;
    private final TestService testService;
    private final PreguntaService preguntaService;
    private final RespuestaService respuestaService;
    private final UsuarioService usuarioService;

    public AdminController(MateriaService materiaService, TestService testService, 
                           PreguntaService preguntaService, RespuestaService respuestaService, 
                           UsuarioService usuarioService) {
        this.materiaService = materiaService;
        this.testService = testService;
        this.preguntaService = preguntaService;
        this.respuestaService = respuestaService;
        this.usuarioService = usuarioService;
    }

    // CRUD Materia
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

    @PatchMapping("/materias/{id}/estado")
    public ResponseEntity<Materia> cambiarEstadoMateria(@PathVariable int id, @RequestParam boolean estado) {
        return ResponseEntity.ok(materiaService.cambiarEstadoMateria(id, estado));
    }

    // CRUD Test
    @PostMapping("/tests")
    public ResponseEntity<Test> crearTest(@RequestBody Test test) {
        return ResponseEntity.ok(testService.guardarTest(test));
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

    @PatchMapping("/tests/{id}/estado")
    public ResponseEntity<Test> cambiarEstadoTest(@PathVariable int id, @RequestParam boolean estado) {
        return ResponseEntity.ok(testService.cambiarEstadoTest(id, estado));
    }

    // CRUD Pregunta
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

    // CRUD Respuesta
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

    // CRUD Usuario
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

	/*
	 * @PatchMapping("/usuarios/{id}/rol") public ResponseEntity<Usuario>
	 * cambiarRolUsuario(@PathVariable int id, @RequestParam int idRol) { return
	 * ResponseEntity.ok(usuarioService.cambiarRolUsuario(id, idRol)); }
	 */
    
 // Obtener solo las materias activas
    @GetMapping("/materias/activas")
    public ResponseEntity<List<Materia>> obtenerMateriasActivas() {
        return ResponseEntity.ok(materiaService.obtenerMateriasActivas());
    }

    // Obtener solo las materias inactivas
    @GetMapping("/materias/inactivas")
    public ResponseEntity<List<Materia>> obtenerMateriasInactivas() {
        return ResponseEntity.ok(materiaService.obtenerMateriasInactivas());
    }

}
