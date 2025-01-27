package es.prw.controllers;

import java.util.List;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import es.prw.daos.MateriaDao;
import es.prw.daos.TestDao;
import es.prw.models.Materia;
import es.prw.models.Test;


@Controller
public class homeController {

	
	 @GetMapping("/home")
	    public String home(CsrfToken csrfToken, Model model ) {
	           
	    	model.addAttribute("csrfToken", csrfToken);
	        return "views/home";   // Debe existir un template llamado "home.html"
	    }
	 
	 @GetMapping("/materias")
	    @ResponseBody
	    public List<Materia> getMaterias() {
	    	MateriaDao materiaDao = new MateriaDao();
	        return materiaDao.getMaterias(); 
	    }
	 
	 @GetMapping("/tests")
	    @ResponseBody
	    public List<Test> getTests(@RequestParam("idMateria") int idMateria) {
	    	TestDao testDao = new TestDao();
	        return testDao.getTests(idMateria); 
	    }
}
