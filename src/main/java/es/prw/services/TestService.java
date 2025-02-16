package es.prw.services;

import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Test;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PuntuacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TestService {

    private final TestRepository testRepository;
    private final PuntuacionRepository puntuacionRepository;

    public TestService(TestRepository testRepository, PuntuacionRepository puntuacionRepository) {
        this.testRepository = testRepository;
        this.puntuacionRepository = puntuacionRepository;
    }

    // Obtener todos los tests
    @Transactional(readOnly = true)
    public List<Test> getTests() {
        return testRepository.findAll();
    }

    // Obtener tests por materia
    @Transactional(readOnly = true)
    public List<Test> getTestsByMateria(int idMateria) {
        return testRepository.findByMateriaIdMateria(idMateria);
    }

    // Obtener historial de notas por test
    @Transactional(readOnly = true)
    public List<NotaHistorialDTO> obtenerHistorialNotas() {
        return testRepository.findAll().stream()
                .map(test -> new NotaHistorialDTO(
                        test.getNombreTest(),
                        puntuacionRepository.findNotasByTest(test)
                ))
                .collect(Collectors.toList());
    }

    // Guardar nuevo test
    @Transactional
    public Test guardarTest(Test test) {
        if (test.getMateria() == null) {
            throw new IllegalArgumentException("❌ No se puede guardar un Test sin Materia");
        }
        return testRepository.save(test);
    }

    // Actualizar test
    @Transactional
    public Test actualizarTest(int id, Test nuevoTest) {
        Test testExistente = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Test no encontrado"));

        testExistente.setNombreTest(nuevoTest.getNombreTest());
        testExistente.setActivo(nuevoTest.isActivo());
        
        return testRepository.save(testExistente);
    }

    // Eliminar test
    @Transactional
    public void eliminarTest(int id) {
        if (!testRepository.existsById(id)) {
            throw new RuntimeException("❌ No se puede eliminar. Test no encontrado");
        }
        testRepository.deleteById(id);
    }

    // Alternar estado del test (activar/desactivar)
    @Transactional
    public Test toggleEstado(int id) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Test no encontrado"));
        
        test.setActivo(!test.isActivo());
        return testRepository.save(test);
    }

    // Obtener tests activos
    @Transactional(readOnly = true)
    public List<Test> obtenerTestActivos() {
        return testRepository.findByActivaTrue();
    }

    // Obtener tests activos por materia
    @Transactional(readOnly = true)
    public List<Test> obtenerTestsActivosPorMateria(int idMateria) {
        return testRepository.findByMateria_IdMateriaAndActivaTrue(idMateria);
    }
}
