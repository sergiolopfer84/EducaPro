package es.prw.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.prw.models.Rol;
import es.prw.models.Usuario;
import es.prw.repositories.RolRepository;
import es.prw.repositories.UsuarioRepository;
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Método para registrar un nuevo usuario
    public Optional<Usuario> registerUser(String nombre, String email, String pass) {
        if (usuarioRepository.existsByEmail(email)) {
            return Optional.empty();
        }

        String encodedPassword = passwordEncoder.encode(pass);
        Usuario newUser = new Usuario();
        newUser.setNombre(nombre);  // SE AGREGA EL NOMBRE AQUÍ
        newUser.setEmail(email);
        newUser.setPass(encodedPassword);

        Rol userRole = rolRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        newUser.setRoles(Set.of(userRole));

        usuarioRepository.save(newUser);
        return Optional.of(newUser);
    }


    // Método para verificar si un email ya está en uso
    public boolean isEmailTaken(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // Método para buscar un usuario por email
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Método para cambiar la contraseña de un usuario
    public boolean cambiarPassword(Integer idUsuario, String nuevaPassword) {
        return usuarioRepository.findById(idUsuario).map(usuario -> {
            usuario.setPass(passwordEncoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }
}

