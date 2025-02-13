package es.prw.services;

import es.prw.dtos.MateriaProgresoDTO;
import es.prw.models.Materia;
import es.prw.repositories.MateriaRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.PuntuacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final TestRepository testRepository;
    private final PuntuacionRepository puntuacionRepository;

    // Inyección de dependencias en el constructor
    public MateriaService(MateriaRepository materiaRepository, TestRepository testRepository, PuntuacionRepository puntuacionRepository) {
        this.materiaRepository = materiaRepository;
        this.testRepository = testRepository;
        this.puntuacionRepository = puntuacionRepository;
    }

    // Obtener todas las materias
    @Transactional(readOnly = true)
    public List<Materia> getMaterias() {
    	 List<Materia> materias = materiaRepository.findAll();
    	System.out.println(materias);
        return materias;
    }

    // Obtener progreso de materias con DTO
    @Transactional(readOnly = true)
    public List<MateriaProgresoDTO> obtenerProgresoMaterias() {
        return materiaRepository.findAll().stream()
                .map(materia -> new MateriaProgresoDTO(
                        materia.getNombreMateria(), // Corregido
                        testRepository.countByMateria(materia),
                        puntuacionRepository.countAprobadosByMateria(materia.getIdMateria())
                ))
                .toList(); // Reemplaza collect(Collectors.toList())
    }
}
