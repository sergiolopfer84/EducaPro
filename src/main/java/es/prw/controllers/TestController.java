package es.prw.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Test;
import es.prw.services.TestService;
import java.util.List;

@RestController
@RequestMapping("/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/materia/{idMateria}")
    public List<Test> obtenerTestsPorMateria(@PathVariable int idMateria) {
        return testService.getTestsByMateria(idMateria);
    }
    @GetMapping("/historial")
    public List<NotaHistorialDTO> obtenerHistorialNotas() {
        return testService.obtenerHistorialNotas();
    }
}
