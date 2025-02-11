package es.prw.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Materia;
import es.prw.services.MateriaService;
import java.util.List;

@RestController
@RequestMapping("/materias")
public class MateriaController {

    @Autowired
    private MateriaService materiaService;

    @GetMapping
    public List<Materia> obtenerMaterias() {
        return materiaService.getMaterias();
    }

    @GetMapping("/progreso")
    public List<MateriaProgresoDTO> obtenerProgresoMaterias() {
        return materiaService.obtenerProgresoMaterias();
    }
}
