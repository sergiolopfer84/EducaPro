package es.prw.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import es.prw.daos.UserDao;
import es.prw.dtos.MateriaProgresoDTO;
import es.prw.dtos.NotaHistorialDTO;
import es.prw.models.Usuario;
import es.prw.services.ProgresoService;

@Controller
public class profileController {
	 
    @Autowired
    private UserDao usuarioDao;

    @Autowired
    private ProgresoService progresoService;

    // ✅ Renderiza perfil.html cuando el usuario accede a /perfil
    @GetMapping("/perfil")
    public String perfilPage(CsrfToken csrfToken, Model model) {
        model.addAttribute("csrfToken", csrfToken);
        return "views/perfil"; // Asegúrate de que perfil.html esté en templates/views/
    }

    @GetMapping("/api/perfil")
    @ResponseBody
    public ResponseEntity<Map<String, String>> obtenerPerfil(@SessionAttribute(name = "usuario", required = false) Usuario usuario) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Map<String, String> perfil = new HashMap<>();
        perfil.put("nombre", usuario.getNombre());
        perfil.put("email", usuario.getEmail());

        return ResponseEntity.ok(perfil);
    }

    @PostMapping("/cambiarPassword")
    public ResponseEntity<String> cambiarPassword(@SessionAttribute(name = "usuario", required = false) Usuario usuario,
                                                  @RequestBody Map<String, String> request) {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado.");
        }

        String nuevaPassword = request.get("nuevaPassword");

        if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("La nueva contraseña no puede estar vacía.");
        }

        boolean cambioExitoso = usuarioDao.cambiarPassword(usuario.getIdUsuario(), nuevaPassword);

        if (cambioExitoso) {
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar la contraseña.");
        }
    }

    @GetMapping("/api/progresoMaterias")
    @ResponseBody
    public List<MateriaProgresoDTO> obtenerProgresoMaterias(@SessionAttribute(name = "usuario", required = false) Usuario usuario) {
        return progresoService.obtenerProgresoMaterias(usuario.getIdUsuario());
    }

    @GetMapping("/api/progresoTests")
    public ResponseEntity<Map<String, List<Double>>> obtenerProgresoTests(@RequestParam int idUsuario) {
        Map<String, List<Double>> progresoTests = progresoService.obtenerProgresoTests(idUsuario);
        return ResponseEntity.ok(progresoTests);
    }


	
}
