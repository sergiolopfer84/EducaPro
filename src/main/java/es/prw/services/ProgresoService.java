package es.prw.services;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Materia;
import es.prw.models.Test;
import es.prw.repositories.PuntuacionRepository;
import es.prw.repositories.TestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgresoService {

    private final PuntuacionRepository puntuacionRepository;
    private final TestRepository testRepository;

    // Inyección de dependencias por constructor
    public ProgresoService(PuntuacionRepository puntuacionRepository, TestRepository testRepository) {
        this.puntuacionRepository = puntuacionRepository;
        this.testRepository = testRepository;
    }

    @Transactional(readOnly = true)
    public List<MateriaProgresoDTO> obtenerProgresoMaterias(Integer idUsuario) {
        List<Materia> materias = testRepository.findAll().stream()
                .map(Test::getMateria)
                .distinct()
                .collect(Collectors.toList()); // Mejor compatibilidad con versiones anteriores de Java

        return materias.stream().map(materia -> {
            int totalTests = testRepository.countByMateria(materia);
            int testsAprobados = puntuacionRepository.countAprobadosByMateria(materia.getIdMateria());
            return new MateriaProgresoDTO(materia.getNombreMateria(), totalTests, testsAprobados);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, List<Double>>> obtenerProgresoTests(Integer idUsuario) {
        Map<String, Map<String, List<Double>>> historialNotas = new HashMap<>();

        // Consultar las notas desde la base de datos
        List<Object[]> resultados = puntuacionRepository.obtenerHistorialNotasPorUsuario(idUsuario);

        for (Object[] fila : resultados) {
            String materiaNombre = (String) fila[0];
            String testNombre = (String) fila[1];
            Double nota = (Double) fila[2];

            // Usar `computeIfAbsent` para optimizar la inicialización de los mapas
            historialNotas.computeIfAbsent(materiaNombre, k -> new HashMap<>())
                          .computeIfAbsent(testNombre, k -> new ArrayList<>())
                          .add(nota);
        }

        return historialNotas;
    }
}
