package com.biblioteca26.controladores;

import com.biblioteca26.modelos.AutorPremio;
import com.biblioteca26.repositorios.AutorPremioRepository;
import com.biblioteca26.repositorios.AutorRepository;
import com.biblioteca26.repositorios.PremioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/autor-premios")
public class AutorPremioController {

    private final AutorPremioRepository autorPremioRepository;
    private final AutorRepository autorRepository;
    private final PremioRepository premioRepository;

    public AutorPremioController(AutorPremioRepository autorPremioRepository,
                                 AutorRepository autorRepository,
                                 PremioRepository premioRepository) {
        this.autorPremioRepository = autorPremioRepository;
        this.autorRepository = autorRepository;
        this.premioRepository = premioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("items", autorPremioRepository.findAll());
        return "autor-premios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new AutorPremio());
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("premios", premioRepository.findAll());
        return "autor-premios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") AutorPremio item) {
        autorPremioRepository.save(item);
        return "redirect:/autor-premios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        AutorPremio item = autorPremioRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/autor-premios";
        }
        model.addAttribute("item", item);
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("premios", premioRepository.findAll());
        return "autor-premios/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        AutorPremio item = autorPremioRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/autor-premios";
        }
        model.addAttribute("item", item);
        return "autor-premios/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        autorPremioRepository.deleteById(id);
        return "redirect:/autor-premios";
    }
}
