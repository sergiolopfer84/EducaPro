package es.prw.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Usuario;
import es.prw.services.UsuarioService;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Usuario usuario) {
        Optional<Usuario> newUser = usuarioService.registerUser(usuario.getNombre(), usuario.getEmail(), usuario.getPass());

        return newUser.isPresent()
                ? ResponseEntity.ok("Usuario registrado exitosamente")
                : ResponseEntity.badRequest().body("El email ya está en uso");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            return ResponseEntity.ok("Inicio de sesión exitoso.");
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Credenciales incorrectas.");
        }
    }
   
}
