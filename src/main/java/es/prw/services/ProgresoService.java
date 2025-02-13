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

	/*
	 * @Transactional(readOnly = true) public MateriaProgresoDTO
	 * obtenerProgresoMateriaEspecifica(Integer idUsuario, Integer idMateria) {
	 * Materia materia = testRepository.findMateriaById(idMateria) .orElseThrow(()
	 * -> new RuntimeException("Materia no encontrada con ID: " + idMateria));
	 * 
	 * int totalTests = testRepository.countByMateria(materia); int testsAprobados =
	 * puntuacionRepository.countAprobadosByMateria(idMateria);
	 * 
	 * return new MateriaProgresoDTO(materia.getNombreMateria(), totalTests,
	 * testsAprobados); }
	 */
    @Transactional(readOnly = true)
    public Map<String, Map<String, List<Double>>> obtenerProgresoMateriaEspecifica(Integer idUsuario, Integer idMateria) {
        Map<String, Map<String, List<Double>>> historialNotas = new HashMap<>();

        // Consultar las notas desde la base de datos
        List<Object[]> resultados = puntuacionRepository.obtenerHistorialNotasPorUsuarioYMateria(idUsuario, idMateria);

        // 🔍 Debug: Ver qué devuelve la BD
        System.out.println("Resultados BD: " + resultados);

        for (Object[] fila : resultados) {
            String testNombre = (String) fila[0];
            Double nota = (Double) fila[1];

            // Usar `computeIfAbsent` para inicializar la estructura
            historialNotas.computeIfAbsent("Materia Consultada", k -> new HashMap<>())
                          .computeIfAbsent(testNombre, k -> new ArrayList<>())
                          .add(nota);
        }

        System.out.println("Notas procesadas: " + historialNotas);
        return historialNotas;
    }




}
