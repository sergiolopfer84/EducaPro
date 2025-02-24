package es.prw.controllers;

import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import es.prw.services.UsuarioService;
import jakarta.servlet.http.HttpSession;

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
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String email = authentication.getName();
		return usuarioRepository.findByEmail(email).map(ResponseEntity::ok)
				.orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
	}

	@GetMapping("/perfil")
	public ResponseEntity<Map<String, Object>> getPerfil(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado."));
		}

		String email = authentication.getName();
		return usuarioRepository.findByEmail(email)
				.map(usuario -> ResponseEntity.ok(Map.of("usuario", (Object) usuario))) // Asegura que el valor es de
																						// tipo Object
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("error", (Object) "Usuario no encontrado.")));
	}

	@Transactional
	@PatchMapping("/cambiar-password")
	public ResponseEntity<Map<String, String>> cambiarPassword(Authentication authentication,
			@RequestBody Map<String, String> payload, HttpSession session) {

		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuario no autenticado."));
		}

		String email = authentication.getName();
		String nuevaPassword = payload.get("nuevaPassword");

		if (nuevaPassword == null || nuevaPassword.trim().length() < 6) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "La nueva contraseña debe tener al menos 6 caracteres."));
		}

		Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
		if (usuarioOptional.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado."));
		}

		boolean cambioExitoso = usuarioService.cambiarPassword(usuarioOptional.get().getIdUsuario(), nuevaPassword);

		if (cambioExitoso) {
			session.invalidate(); // Invalida la sesión tras el cambio de contraseña
			return ResponseEntity
					.ok(Map.of("message", "Contraseña actualizada correctamente. Inicie sesión de nuevo."));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Error al actualizar la contraseña."));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpSession session) {
		session.invalidate();
		return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente."));
	}

	@PostMapping("/limpiarSesion")
	public ResponseEntity<Map<String, String>> limpiarSesion(HttpSession session) {
		session.invalidate();
		return ResponseEntity.ok(Map.of("message", "Sesión limpiada con éxito."));
	}
}
