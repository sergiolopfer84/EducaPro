package es.prw.services;

import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Test;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PuntuacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
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
                return new NotaHistorialDTO(test.getNombreTest(), (notas != null) ? notas : Collections.emptyList());
            })
            .collect(Collectors.toList());
    }
}
