package es.prw.services;

import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Materia;
import es.prw.models.Test;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PuntuacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final PuntuacionRepository puntuacionRepository;

    // Inyección de dependencias por constructor
    public TestService(TestRepository testRepository, PuntuacionRepository puntuacionRepository) {
        this.testRepository = testRepository;
        this.puntuacionRepository = puntuacionRepository;
    }

    
    // Obtener todas las materias
    @Transactional(readOnly = true)
    public List<Test> getTests() {
    	 List<Test> tests = testRepository.findAll();
    	System.out.println(tests);
        return tests;
    }
    // Obtener tests por materia
    @Transactional(readOnly = true)
    public List<Test> getTestsByMateria(int idMateria) {
        return testRepository.findByMateriaIdMateria(idMateria); // Optimizado
    }

    // Obtener historial de notas por test
    @Transactional(readOnly = true)
    public List<NotaHistorialDTO> obtenerHistorialNotas() {
        return testRepository.findAll().stream()
                .map(test -> {
                    List<Double> notas = puntuacionRepository.findNotasByTest(test);
                    return new NotaHistorialDTO(test.getNombreTest(),
                            (notas != null) ? notas : Collections.emptyList());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public Test guardarTest(Test test) {
    	System.out.println("GGuardar tests  testService "+ test);
        return testRepository.save(test);
    }

    @Transactional
    public Test actualizarTest(int id, Test nuevoTest) {
        return testRepository.findById(id).map(test -> {
            test.setNombreTest(nuevoTest.getNombreTest());
            return testRepository.save(test);
        }).orElseThrow(() -> new RuntimeException("Test no encontrado"));
    }

    @Transactional
    public void eliminarTest(int id) {
        testRepository.deleteById(id);
    }

    @Transactional
    public Test cambiarEstadoTest(int id, boolean estado) {
        return testRepository.findById(id).map(test -> {
            test.setActivo(estado);
            return testRepository.save(test);
        }).orElseThrow(() -> new RuntimeException("Test no encontrado"));
    }
    
    
    public void toggleEstadoTest(Integer id) {
        Test test = testRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Test no encontrado"));
        test.setActivo(!test.isActivo());
        testRepository.save(test);
    }
    @Transactional(readOnly = true)
    public List<Test> obtenerTestActivos() {
        return testRepository.findByActivaTrue();
    }
    public List<Test> obtenerTestsActivosPorMateria(int idMateria) {
        return testRepository.findByMateria_IdMateriaAndActivaTrue(idMateria);
    }

    

}
