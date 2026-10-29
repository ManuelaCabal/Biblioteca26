package com.biblioteca26.controladores;

import com.biblioteca26.modelos.Serie;
import com.biblioteca26.repositorios.SerieRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/series")
public class SerieController {

    private final SerieRepository serieRepository;

    public SerieController(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("items", serieRepository.findAll());
        return "series/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new Serie());
        return "series/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") Serie item) {
        serieRepository.save(item);
        return "redirect:/series";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Serie item = serieRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/series";
        }
        model.addAttribute("item", item);
        return "series/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        Serie item = serieRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/series";
        }
        model.addAttribute("item", item);
        return "series/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        serieRepository.deleteById(id);
        return "redirect:/series";
    }
}
