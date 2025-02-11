package es.prw.services;
import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Test;
import es.prw.models.Materia;
import es.prw.models.Puntuacion;
import es.prw.repositories.TestRepository;
import es.prw.repositories.MateriaRepository;
import es.prw.repositories.PuntuacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private PuntuacionRepository puntuacionRepository;

    // Obtener tests por materia
    public List<Test> getTestsByMateria(int idMateria) {
        return testRepository.findTestsByMateria(idMateria); // Corrección aquí
    }

    // Obtener historial de notas por test
    public List<NotaHistorialDTO> obtenerHistorialNotas() {
        List<Test> tests = testRepository.findAll();

        return tests.stream().map(test -> {
            List<Double> notas = puntuacionRepository.findNotasByTest(test);
            return new NotaHistorialDTO(test.getTest(), notas);
        }).collect(Collectors.toList());
    }
}
