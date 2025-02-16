package es.prw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import es.prw.models.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@Controller
public class MainController {

    @GetMapping("/perfil")
    public String perfilPage(Model model, HttpServletRequest request) {
    	
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("csrfToken", csrfToken);
        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);
        return "views/perfil";
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpServletRequest request) {
       
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("csrfToken", csrfToken);
            model.addAttribute("csrfHeaderName", csrfToken.getHeaderName());
        }
        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);
        return "views/home";  // Verifica que tienes una vista llamada "home.html"
    }

    @GetMapping({"/index", "/"})
    public String indexPage(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("csrfToken", csrfToken);
            model.addAttribute("csrfHeaderName", csrfToken.getHeaderName());
        }
        String currentUri = request.getRequestURI();
        model.addAttribute("currentUri", currentUri);
        return "views/index";
    }
    @GetMapping("/admin")
    public String adminPage(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("csrfToken", csrfToken);
            model.addAttribute("csrfHeaderName", csrfToken.getHeaderName());
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .toString();
        System.out.println(roles);
        return "views/admin";
    }
 // Agrega esto en cualquier controlador accesible
  
    @GetMapping("/debug/roles")
    public ResponseEntity<String> getUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                .toString();
        return ResponseEntity.ok("Roles del usuario autenticado: " + roles);
    }
    @PostMapping("/admin/debug")
    public String debugPost() {
    	   return "OK from admin debug";
    	}


}