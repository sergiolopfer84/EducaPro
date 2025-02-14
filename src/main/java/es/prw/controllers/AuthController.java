package es.prw.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.UsuarioService;
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
	// Inyección de dependencias en el constructor
	public AuthController(AuthenticationManager authenticationManager, UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
		this.authenticationManager = authenticationManager;
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@RequestBody Usuario usuario) {
		
		System.out.println(usuario.getNombre()+" "+ usuario.getEmail()+" " + usuario.getPass());
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
	   
		System.out.println("Entrando en el controlador de login");
		String email = credentials.get("email");
	    String password = credentials.get("password");

	    System.out.println("email: " + email + ", password: " + password); // Asegúrate de que esto se imprima

	    if (email == null || email.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Map.of("error", "El email no puede estar vacío."));
	    }

	    try {
	    	System.out.println("Tratando de autenticar");
	        Authentication auth = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(email, password)
	        );

	        Usuario usuario = usuarioService.findByEmail(email)
	                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en la base de datos"));

	        session.setAttribute("usuario", usuario);
	        session.setMaxInactiveInterval(1800); // 30 minutos de sesión activa

	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Inicio de sesión exitoso.");
	        return ResponseEntity.ok(response);

	    } catch (AuthenticationException ex) {
	        Map<String, String> response = new HashMap<>();
	        response.put("error", "Credenciales incorrectas.");
	        return ResponseEntity.status(401).body(response);
	    }
	}

	@GetMapping("/checkSession")
	public ResponseEntity<Map<String, String>> checkSession() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("Entrando en el controlador de checksession");
		Map<String, String> response = new HashMap<>();

		if (authentication != null && authentication.isAuthenticated()
				&& authentication.getPrincipal() instanceof Usuario) {
			 org.springframework.security.core.userdetails.User springUser =
			            (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

			        String email = springUser.getUsername(); // Aquí saca el "usuario" -> email
			        // Ahora buscas en tu BD la entidad Usuario
			        Usuario usuarioReal = usuarioRepository.findByEmail(email)
			            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
				    
			response.put("message", "Usuario en sesión: " + usuarioReal.getNombre());
			return ResponseEntity.ok(response);
		} else {
			response.put("error", "No hay usuario en sesión");
			return ResponseEntity.status(401).body(response);
		}
	}

}
