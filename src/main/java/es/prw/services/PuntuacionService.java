package es.prw.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    @Autowired
    private PuntuacionRepository puntuacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestRepository testRepository;

    // Guardar una nueva puntuación
    public Optional<Puntuacion> savePuntuacion(Integer idUsuario, int idTest, double notaConseguida) {
        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);
        Optional<Test> test = testRepository.findById(idTest);

        if (usuario.isPresent() && test.isPresent()) {
            Puntuacion puntuacion = new Puntuacion();
            puntuacion.setUsuario(usuario.get());
            puntuacion.setTest(test.get());
            puntuacion.setNotaConseguida(notaConseguida);
            puntuacion.setFecha(new Date());

            return Optional.of(puntuacionRepository.save(puntuacion));
        }
        return Optional.empty();
    }

    // Obtener puntuaciones de un usuario por materia
    public List<Puntuacion> getPuntuacionesPorMateria(Integer idUsuario, int idMateria) {
        return puntuacionRepository.findPuntuacionesByUsuario(idUsuario)
                .stream()
                .filter(p -> p.getTest() != null && 
                             p.getTest().getMateria() != null && 
                             p.getTest().getMateria().getIdMateria().equals(idMateria))
                .toList();
    }

    // Obtener últimas 2 puntuaciones de un usuario en un test
    public List<Double> getUltimasPuntuacionesByTest(Integer idUsuario, int idTest) {
        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);
        Optional<Test> test = testRepository.findById(idTest);

        if (usuario.isPresent() && test.isPresent()) {
            return puntuacionRepository.findByUsuarioAndTest(usuario.get(), test.get())
                    .stream()
                    .map(Puntuacion::getNotaConseguida)
                    .limit(2)
                    .toList();
        }
        return List.of();
    }

    // Obtener todas las puntuaciones de un usuario
    public List<Puntuacion> getPuntuacionesByUsuario(Integer idUsuario) {
        return puntuacionRepository.findPuntuacionesByUsuario(idUsuario);
    }
}
