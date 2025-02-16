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
                .toList(); 
    }
    

    @Transactional
    public Materia guardarMateria(Materia materia) {
        return materiaRepository.save(materia);
    }

    @Transactional
    public Materia actualizarMateria(int id, Materia nuevaMateria) {
        return materiaRepository.findById(id).map(materia -> {
            materia.setNombreMateria(nuevaMateria.getNombreMateria());
            materia.setActiva(nuevaMateria.isActiva());
            return materiaRepository.save(materia);
        }).orElseThrow(() -> new RuntimeException("Materia no encontrada"));
    }

    @Transactional
    public void eliminarMateria(int id) {
        materiaRepository.deleteById(id);
    }

    @Transactional
    public Materia cambiarEstadoMateria(int id, boolean estado) {
        return materiaRepository.findById(id).map(materia -> {
            return materiaRepository.save(materia);
        }).orElseThrow(() -> new RuntimeException("Materia no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Materia> obtenerMateriasActivas() {
        return materiaRepository.findByActivaTrue();
    }

    @Transactional(readOnly = true)
    public List<Materia> obtenerMateriasInactivas() {
        return materiaRepository.findByActivaFalse();
    }
    
    public void toggleEstadoMateria(Integer id) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Materia no encontrada"));
        materia.setActiva(!materia.isActiva());
        materiaRepository.save(materia);
    }

}
