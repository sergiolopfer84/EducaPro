package es.prw.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import es.prw.models.Usuario;
import es.prw.repositories.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UsuarioRepository usuarioRepository;

    public GlobalControllerAdvice(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute
    public void addUserToModel(Model model, @AuthenticationPrincipal User springUser) {
        if (springUser != null) {
            System.out.println("Usuario autenticado: " + springUser.getUsername()); // LOG para depuración

            Usuario usuario = usuarioRepository.findByEmail(springUser.getUsername()).orElse(null);

            if (usuario == null) {
                System.out.println("⚠️ ERROR: No se encontró el usuario en la base de datos.");
            } else {
                System.out.println("✅ Usuario cargado correctamente: " + usuario.getEmail());
            }

            model.addAttribute("usuario", usuario);
        }
    }
    @ModelAttribute("httpServletRequest")
    public HttpServletRequest getRequest(HttpServletRequest request) {
        return request;
    }
}
