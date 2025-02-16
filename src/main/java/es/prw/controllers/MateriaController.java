package es.prw.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Materia;
import es.prw.services.MateriaService;
import java.util.List;

@RestController
@RequestMapping("/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    // ✅ Obtener todas las materias
    @GetMapping
    public ResponseEntity<List<Materia>> obtenerMaterias() {
        return ResponseEntity.ok(materiaService.getMaterias());
    }

    // ✅ Obtener solo las materias activas
    @GetMapping("/activas")
    public ResponseEntity<List<Materia>> obtenerMateriasActivas() {
        return ResponseEntity.ok(materiaService.obtenerMateriasActivas());
    }

    // ✅ Obtener progreso de materias
    @GetMapping("/progreso")
    public ResponseEntity<List<MateriaProgresoDTO>> obtenerProgresoMaterias() {
        return ResponseEntity.ok(materiaService.obtenerProgresoMaterias());
    }
}
