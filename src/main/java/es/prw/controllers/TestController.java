package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Materia;
import es.prw.models.Test;
import es.prw.services.TestService;
import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    
    @GetMapping
    public ResponseEntity<List<Test>> obtenerTests() {
        List<Test> tests = testService.getTests() ;
        return tests.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tests);
    }
    @GetMapping("/materia/{idMateria}")
    public ResponseEntity<List<Test>> obtenerTestsPorMateria(@PathVariable int idMateria) {
        List<Test> tests = testService.getTestsByMateria(idMateria);
        return tests.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tests);
    }

    @GetMapping("/historial")
    public ResponseEntity<List<NotaHistorialDTO>> obtenerHistorialNotas() {
        List<NotaHistorialDTO> historialNotas = testService.obtenerHistorialNotas();
        return historialNotas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(historialNotas);
    }
    @GetMapping("/activos")
    public ResponseEntity<List<Test>> obtenertestsActivos() {
        List<Test> testActivos = testService.obtenerTestActivos(); // Nuevo método en el servicio
        return ResponseEntity.ok(testActivos);
    }
    @PutMapping("/{id}/toggle-activa")
    public ResponseEntity<Void> cambiarEstadoTest(@PathVariable Integer id) {
        testService.toggleEstadoTest(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/materia/{idMateria}/activos")
    public ResponseEntity<List<Test>> obtenerTestsActivosPorMateria(@PathVariable int idMateria) {
        List<Test> testsActivos = testService.obtenerTestsActivosPorMateria(idMateria);
        return testsActivos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(testsActivos);
    }


}
