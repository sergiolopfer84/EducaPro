package es.prw.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Usuario;
import es.prw.services.UsuarioService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/api/perfil")
    public ResponseEntity<Usuario> getPerfil(Authentication authentication) {
        String email = authentication.getName();
        return usuarioService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(Authentication authentication, @RequestBody String nuevaPassword) {
        String email = authentication.getName();
        Optional<Usuario> usuario = usuarioService.findByEmail(email);

        if (usuario.isPresent()) {
            // Validar que la nueva contraseña no esté vacía
            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía.");
            }

            usuarioService.cambiarPassword(usuario.get().getIdUsuario() , nuevaPassword);
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } else {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }
    }

    // 🔹 Método para obtener un usuario por email
    @GetMapping("/{email}")
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(@PathVariable String email) {
        return usuarioService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
    @GetMapping("/limpiarSesion")
    public ResponseEntity<String> limpiarSesion(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Sesión limpiada");
    }

}
