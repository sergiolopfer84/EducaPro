package es.prw.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.UsuarioService;
import es.prw.services.LoginAttemptService; // Importamos la clase

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService; // Agregamos la variable

    // Inyección de dependencias en el constructor
    public AuthController(AuthenticationManager authenticationManager, 
                          UsuarioService usuarioService, 
                          UsuarioRepository usuarioRepository,
                          LoginAttemptService loginAttemptService) { // Agregamos aquí
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.loginAttemptService = loginAttemptService; // Inicializamos
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Usuario usuario) {
        boolean isRegistered = usuarioService.registerUser(usuario.getNombre(), usuario.getEmail(), usuario.getPass())
                .isPresent();

        Map<String, String> response = new HashMap<>();
        if (isRegistered) {
            response.put("message", "Usuario registrado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "El email ya está en uso");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El email y la contraseña no pueden estar vacíos."));
        }

        // Verificamos si el usuario está bloqueado antes de intentar autenticación
        if (loginAttemptService.isBlocked(email)) {
            long remainingTime = loginAttemptService.getRemainingLockTime(email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Demasiados intentos fallidos. Inténtalo de nuevo en " + remainingTime + " segundos."));
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Si la autenticación es exitosa, obtenemos el usuario desde la BD
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

            // Reiniciar intentos fallidos si el login es exitoso
            loginAttemptService.loginSucceeded(email);

            // Guardamos el usuario en la sesión
            session.setAttribute("usuario", usuario);
            session.setMaxInactiveInterval(1800); // 30 minutos de sesión activa

            return ResponseEntity.ok(Map.of("message", "Inicio de sesión exitoso."));
        } catch (AuthenticationException ex) {
            // Registrar intento fallido
            loginAttemptService.loginFailed(email);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas."));
        }
    }

//    @GetMapping("/checkSession")
//    public ResponseEntity<Map<String, String>> checkSession() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated()) {
//            return ResponseEntity.status(401).body(Map.of("error", "No hay usuario en sesión"));
//        }
//
//        String email = authentication.getName();
//        return usuarioRepository.findByEmail(email)
//                .map(usuario -> ResponseEntity.ok(Map.of("message", "Usuario en sesión: " + usuario.getNombre())))
//                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Usuario no encontrado")));
//    }
}
