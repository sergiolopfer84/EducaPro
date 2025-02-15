package es.prw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import es.prw.models.Usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.web.csrf.CsrfToken;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    @GetMapping("/perfil")
    public String perfilPage(Model model, HttpServletRequest request) {
    	
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("csrfToken", csrfToken);
        return "views/perfil";
    }

    @GetMapping("/home")
    public String homePage(Model model, HttpServletRequest request) {
       
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("csrfToken", csrfToken);
            model.addAttribute("csrfHeaderName", csrfToken.getHeaderName());
        }
        return "views/home";  // Verifica que tienes una vista llamada "home.html"
    }

    @GetMapping({"/index", "/"})
    public String indexPage(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("csrfToken", csrfToken);
            model.addAttribute("csrfHeaderName", csrfToken.getHeaderName());
        }
        return "views/index";
    }
    @GetMapping("/admin")
    public String adminPage(Model model, HttpServletRequest request) {
    	
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("csrfToken", csrfToken);
        return "views/admin";
    }
}