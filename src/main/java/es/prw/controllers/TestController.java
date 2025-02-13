package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.NotaHistorialDTO;
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
}
