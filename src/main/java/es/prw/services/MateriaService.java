
package es.prw.services;
import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Materia;
import es.prw.repositories.MateriaRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PuntuacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MateriaService {

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private PuntuacionRepository puntuacionRepository;

    // Obtener todas las materias
    public List<Materia> getMaterias() {
        return materiaRepository.findAll();
    }

    // Obtener progreso de materias con DTO
    public List<MateriaProgresoDTO> obtenerProgresoMaterias() {
        List<Materia> materias = materiaRepository.findAll();

        return materias.stream().map(materia -> {
            int totalTests = testRepository.countByMateria(materia);
            int testsAprobados = puntuacionRepository.countAprobadosByMateria(materia.getIdMateria());
            return new MateriaProgresoDTO(materia.getMateria(), totalTests, testsAprobados);
        }).collect(Collectors.toList());
    }
}
