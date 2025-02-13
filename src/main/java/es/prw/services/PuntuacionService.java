package es.prw.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import es.prw.models.Puntuacion;
import es.prw.models.Test;
import es.prw.models.Usuario;
import es.prw.repositories.PuntuacionRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.UsuarioRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PuntuacionService {

    private final PuntuacionRepository puntuacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TestRepository testRepository;

    // Inyección de dependencias por constructor
    public PuntuacionService(PuntuacionRepository puntuacionRepository, UsuarioRepository usuarioRepository, TestRepository testRepository) {
        this.puntuacionRepository = puntuacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.testRepository = testRepository;
    }

    // Guardar una nueva puntuación
    @Transactional
    public Optional<Puntuacion> savePuntuacion(Integer idUsuario, int idTest, double notaConseguida) {
        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);
        Optional<Test> test = testRepository.findById(idTest);

        if (usuario.isPresent() && test.isPresent()) {
            Puntuacion puntuacion = new Puntuacion();
            puntuacion.setUsuario(usuario.get());
            puntuacion.setTest(test.get());
            puntuacion.setNotaObtenida(notaConseguida); // Corregido: antes era `setNotaConseguida`
            puntuacion.setFecha(new Date());

            return Optional.of(puntuacionRepository.save(puntuacion));
        }
        return Optional.empty();
    }

    // Obtener puntuaciones de un usuario por materia
    @Transactional(readOnly = true)
    public List<Puntuacion> getPuntuacionesPorMateria(Integer idUsuario, int idMateria) {
        return puntuacionRepository.findPuntuacionesByUsuario(idUsuario)
                .stream()
                .filter(p -> p.getTest() != null &&
                             p.getTest().getMateria() != null &&
                             p.getTest().getMateria().getIdMateria().equals(idMateria))
                .toList();
    }

    // Obtener últimas 2 puntuaciones de un usuario en un test
    @Transactional(readOnly = true)
    public List<Double> getUltimasPuntuacionesByTest(Integer idUsuario, int idTest) {
        return puntuacionRepository.findUltimasPuntuacionesByUsuarioAndTest(idUsuario, idTest, PageRequest.of(0, 2));
    }

    // Obtener todas las puntuaciones de un usuario
    @Transactional(readOnly = true)
    public List<Puntuacion> getPuntuacionesByUsuario(Integer idUsuario) {
        return puntuacionRepository.findPuntuacionesByUsuario(idUsuario);
    }
}
