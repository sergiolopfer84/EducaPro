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

	// ✅ Obtener todos los tests
	@GetMapping
	public ResponseEntity<List<Test>> obtenerTests() {
		return ResponseEntity.ok(testService.getTests());
	}

	// ✅ Obtener solo los tests activos de una materia específica
	@GetMapping("/materia/{idMateria}/activos")
	public ResponseEntity<List<Test>> obtenerTestsActivosPorMateria(@PathVariable int idMateria) {
		return ResponseEntity.ok(testService.obtenerTestsActivosPorMateria(idMateria));
	}

}
