package es.prw.controllers;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/perfil")
    public String perfilPage(CsrfToken csrfToken, Model model) {
        model.addAttribute("csrfToken", csrfToken);
        return "views/perfil";
    }

    @GetMapping("/home")
    public String homePage(CsrfToken csrfToken, Model model) {
        model.addAttribute("csrfToken", csrfToken);
        return "views/home";
    }
    
    
}
