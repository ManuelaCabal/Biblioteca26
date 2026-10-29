package com.biblioteca26.controladores;

import com.biblioteca26.modelos.LibroPremio;
import com.biblioteca26.repositorios.LibroPremioRepository;
import com.biblioteca26.repositorios.LibroRepository;
import com.biblioteca26.repositorios.PremioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/libro-premios")
public class LibroPremioController {

    private final LibroPremioRepository libroPremioRepository;
    private final LibroRepository libroRepository;
    private final PremioRepository premioRepository;

    public LibroPremioController(LibroPremioRepository libroPremioRepository,
                                 LibroRepository libroRepository,
                                 PremioRepository premioRepository) {
        this.libroPremioRepository = libroPremioRepository;
        this.libroRepository = libroRepository;
        this.premioRepository = premioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("items", libroPremioRepository.findAll());
        return "libro-premios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new LibroPremio());
        model.addAttribute("libros", libroRepository.findAll());
        model.addAttribute("premios", premioRepository.findAll());
        return "libro-premios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") LibroPremio item) {
        libroPremioRepository.save(item);
        return "redirect:/libro-premios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        LibroPremio item = libroPremioRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/libro-premios";
        }
        model.addAttribute("item", item);
        model.addAttribute("libros", libroRepository.findAll());
        model.addAttribute("premios", premioRepository.findAll());
        return "libro-premios/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        LibroPremio item = libroPremioRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/libro-premios";
        }
        model.addAttribute("item", item);
        return "libro-premios/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        libroPremioRepository.deleteById(id);
        return "redirect:/libro-premios";
    }
}
