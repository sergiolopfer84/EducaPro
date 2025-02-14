package es.prw.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.prw.models.Materia;
import es.prw.models.Test;
import es.prw.models.Usuario;
import es.prw.repositories.MateriaRepository;
import es.prw.repositories.PreguntaRepository;
import es.prw.repositories.RespuestaRepository;
import es.prw.repositories.TestRepository;
import es.prw.repositories.UsuarioRepository;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")  // o en SecurityConfig, filtrar /admin/**
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final MateriaRepository materiaRepository;
    private final TestRepository testRepository;
    private final PreguntaRepository preguntaRepository;
    private final RespuestaRepository respuestaRepository;
    
    
    public AdminController(
            UsuarioRepository usuarioRepository,
            MateriaRepository materiaRepository,
            TestRepository testRepository,
            PreguntaRepository preguntaRepository,
            RespuestaRepository respuestaRepository
           
    ) {
        this.usuarioRepository = usuarioRepository;
        this.materiaRepository = materiaRepository;
        this.testRepository = testRepository;
        this.preguntaRepository = preguntaRepository;
        this.respuestaRepository = respuestaRepository;
    }

    // ------------------------
    // CRUD DE USUARIOS
    // ------------------------
    
    // Crear usuario
    @PostMapping("/usuarios")
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        // Asignar roles, encriptar password si hace falta, etc.
        return usuarioRepository.save(usuario);
    }

    // Listar usuarios
    @GetMapping("/usuarios")
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // Buscar uno
    @GetMapping("/usuarios/{id}")
    public Optional<Usuario> getUsuario(@PathVariable Integer id) {
        return usuarioRepository.findById(id);
    }

    // Actualizar
    @PutMapping("/usuarios/{id}")
    public Usuario updateUsuario(@PathVariable Integer id, @RequestBody Usuario userUpdate) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizas campos
        usuario.setNombre(userUpdate.getNombre());
        usuario.setEmail(userUpdate.getEmail());
       
        
        return usuarioRepository.save(usuario);
    }

    // Eliminar
    @DeleteMapping("/usuarios/{id}")
    public void deleteUsuario(@PathVariable Integer id) {
        usuarioRepository.deleteById(id);
    }

    // ------------------------
    // CRUD DE MATERIAS
    // ------------------------
    
    @PostMapping("/materias")
    public Materia createMateria(@RequestBody Materia materia) {
       //  materia.setActiva(false);
        return materiaRepository.save(materia);
    }

    @GetMapping("/materias")
    public List<Materia> getAllMaterias() {
        return materiaRepository.findAll();
    }

    @GetMapping("/materias/{id}")
    public Materia getMateria(@PathVariable Integer id) {
        return materiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Materia no encontrada"));
    }

    @PutMapping("/materias/{id}")
    public Materia updateMateria(@PathVariable Integer id, @RequestBody Materia matUpdate) {
        Materia materia = materiaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Materia no encontrada"));
        
        materia.setNombreMateria(matUpdate.getNombreMateria());
       // materia.setActiva(matUpdate.isActiva());
        return materiaRepository.save(materia);
    }

    @DeleteMapping("/materias/{id}")
    public void deleteMateria(@PathVariable Integer id) {
        materiaRepository.deleteById(id);
    }

    // ------------------------
    // CRUD DE TEST
    // ------------------------
    // Aquí relacionamos con Materia, etc.

    @PostMapping("/tests")
    public Test createTest(@RequestBody Test test) {
        // Verificamos que la materia exista
        Materia materia = materiaRepository.findById(test.getMateria().getIdMateria())
            .orElseThrow(() -> new RuntimeException("Materia no existe"));

        test.setMateria(materia);
        return testRepository.save(test);
    }

    @GetMapping("/tests")
    public List<Test> getAllTests() {
        return testRepository.findAll();
    }

    // etc...  
    // Resto de métodos: getById, update, delete

    // ...
}
