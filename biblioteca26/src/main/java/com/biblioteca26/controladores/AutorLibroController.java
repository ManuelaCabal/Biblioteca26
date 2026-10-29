package com.biblioteca26.controladores;

import com.biblioteca26.modelos.AutorLibro;
import com.biblioteca26.repositorios.AutorLibroRepository;
import com.biblioteca26.repositorios.AutorRepository;
import com.biblioteca26.repositorios.LibroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/autor-libros")
public class AutorLibroController {

    private final AutorLibroRepository autorLibroRepository;
    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;

    public AutorLibroController(AutorLibroRepository autorLibroRepository,
                                AutorRepository autorRepository,
                                LibroRepository libroRepository) {
        this.autorLibroRepository = autorLibroRepository;
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("items", autorLibroRepository.findAll());
        return "autor-libros/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new AutorLibro());
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("libros", libroRepository.findAll());
        return "autor-libros/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") AutorLibro item) {
        autorLibroRepository.save(item);
        return "redirect:/autor-libros";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        AutorLibro item = autorLibroRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/autor-libros";
        }
        model.addAttribute("item", item);
        model.addAttribute("autores", autorRepository.findAll());
        model.addAttribute("libros", libroRepository.findAll());
        return "autor-libros/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        AutorLibro item = autorLibroRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/autor-libros";
        }
        model.addAttribute("item", item);
        return "autor-libros/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        autorLibroRepository.deleteById(id);
        return "redirect:/autor-libros";
    }
}
