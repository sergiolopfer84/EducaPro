package es.prw.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.UsuarioService;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    @Autowired
    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService; 
        this.usuarioRepository = usuarioRepository;
        
    }
    @GetMapping("/api/current-user")
    public ResponseEntity<Usuario> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // En realidad es un 'User' de Spring Security, no tu Usuario
        org.springframework.security.core.userdetails.User springUser =
            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        String email = springUser.getUsername(); // Aquí saca el "usuario" -> email
        // Ahora buscas en tu BD la entidad Usuario
        Usuario usuarioReal = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));

        // Devuelves tu entidad con todos sus campos
        return ResponseEntity.ok(usuarioReal);
    }


    
    @GetMapping("/perfil")
    public ResponseEntity<Usuario> getPerfil(Authentication authentication) {
        String email = authentication.getName();
        return usuarioService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/cambiar-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(Authentication authentication, @RequestBody String nuevaPassword) {
        String email = authentication.getName();
        Optional<Usuario> usuario = usuarioService.findByEmail(email);

        if (usuario.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado."));
        }

        // Validar que la nueva contraseña no esté vacía y tenga al menos 6 caracteres
        if (nuevaPassword == null || nuevaPassword.trim().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres."));
        }

        usuarioService.cambiarPassword(usuario.get().getIdUsuario(), nuevaPassword);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }

    @GetMapping("/{email}")
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable String email) {
        return usuarioService.findByEmail(email.toLowerCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/limpiarSesion")
    public ResponseEntity<Map<String, String>> limpiarSesion(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Sesión limpiada con éxito."));
    }
}
