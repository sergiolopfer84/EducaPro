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

    // Inyección de dependencias en el constructor
    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public ResponseEntity<List<Materia>> obtenerMaterias() {
        List<Materia> materias = materiaService.getMaterias();
        
        return ResponseEntity.ok(materias);
    }

    @GetMapping("/progreso")
    public ResponseEntity<List<MateriaProgresoDTO>> obtenerProgresoMaterias() {
        List<MateriaProgresoDTO> progreso = materiaService.obtenerProgresoMaterias();
        return ResponseEntity.ok(progreso);
    }
}
