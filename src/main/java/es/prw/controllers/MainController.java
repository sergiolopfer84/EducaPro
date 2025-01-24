package es.prw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class MainController {
	
    // Controlador para cargar la página index.html
    @GetMapping("/")
    public String index() {
        return "views/index";  // Debe existir un template llamado "index.html"
    }

    // Controlador para cargar la página home.html
    @GetMapping("/home")
    public String home() {
        return "views/home";   // Debe existir un template llamado "home.html"
    }
}
