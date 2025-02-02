package es.prw.controllers;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.prw.daos.UserDao;
import es.prw.models.Usuario;
import es.prw.services.LoginAttemptService;

@CrossOrigin(origins = "http://localhost:8080")
@Controller
public class MainController {

	private final UserDao userDao;

	@Autowired
	public MainController(UserDao userDao) {
		this.userDao = userDao;
	}

	// Controlador para cargar la página index.html
	@GetMapping("/")
	public String index(Model model, CsrfToken csrfToken) {
		model.addAttribute("csrfToken", csrfToken);
		return "views/index"; // Debe existir un template llamado "index.html"
	}

	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody Usuario usuario) {

		try {

			Optional<Usuario> newUser = userDao.registerUser(usuario.getNombre(), usuario.getEmail(),
					usuario.getPass());
			if (newUser.isPresent()) {
				return ResponseEntity.ok("Registro exitoso");
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El email ya está en uso.");
			}
		} catch (SQLException e) {
			System.err.println("Error al registrar el usuario: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error al registrar el usuario: " + e.getMessage());
		}
	}
	@RestController
	@RequestMapping("/login")
	public class LoginController {

	    @Autowired
	    private AuthenticationManager authenticationManager;

	    @Autowired
	    private LoginAttemptService loginAttemptService;

	    @PostMapping
	    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
	        if (loginAttemptService.isBlocked(email)) {
	            long remainingTime = loginAttemptService.getRemainingLockTime(email);
	            return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body("Cuenta bloqueada durante " + remainingTime + " segundos.");
	        }

	        try {
	            Authentication auth = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(email, password)
	            );
	            loginAttemptService.loginSucceeded(email);
	            return ResponseEntity.ok("Inicio de sesión exitoso.");
	        } catch (AuthenticationException ex) {
	            loginAttemptService.loginFailed(email);
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body("Credenciales incorrectas.");
	        }
	    }
	}

	@RestController
	public class UserInfoController {

		@Autowired
		private UserDao userDao;

		@GetMapping("/api/current-user")
		public Usuario getCurrentUser(Authentication authentication) {
			// authentication.getName() será el email con el que se autenticó
			String email = authentication.getName();
			return userDao.findByEmail(email).orElseThrow(() -> new RuntimeException("No se encontró el usuario"));
		}
	}
	

}
