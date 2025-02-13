package es.prw.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.prw.models.Rol;
import es.prw.models.Usuario;
import es.prw.repositories.RolRepository;
import es.prw.repositories.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección de dependencias por constructor
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método para registrar un nuevo usuario
    @Transactional
    public Optional<Usuario> registerUser(String nombre, String email, String pass) {
        if (usuarioRepository.existsByEmail(email)) {
            return Optional.empty();
        }

        String encodedPassword = passwordEncoder.encode(pass);
        Usuario newUser = new Usuario();
        newUser.setNombre(nombre);
        newUser.setEmail(email);
        newUser.setPass(encodedPassword);

        Rol userRole = rolRepository.findByNombre("USER")
                .orElseThrow(() -> new IllegalStateException("Rol USER no encontrado"));

        // Usamos HashSet para permitir modificaciones en el futuro
        newUser.setRoles(new HashSet<>(Set.of(userRole)));

        usuarioRepository.save(newUser);
        return Optional.of(newUser);
    }

    // Método para verificar si un email ya está en uso
    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // Método para buscar un usuario por email
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Método para cambiar la contraseña de un usuario
    @Transactional
    public boolean cambiarPassword(Integer idUsuario, String nuevaPassword) {
        return usuarioRepository.findById(idUsuario).map(usuario -> {
            usuario.setPass(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }
}
