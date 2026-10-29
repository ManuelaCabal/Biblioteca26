package com.biblioteca26.controladores;

import com.biblioteca26.modelos.Autor;
import com.biblioteca26.modelos.Pais;
import com.biblioteca26.modelos.Premio;
import com.biblioteca26.repositorios.AutorRepository;
import com.biblioteca26.repositorios.PaisRepository;
import com.biblioteca26.repositorios.PremioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/paises")
public class PaisController {

    private final PaisRepository paisRepository;
    private final AutorRepository autorRepository;
    private final PremioRepository premioRepository;

    public PaisController(PaisRepository paisRepository, AutorRepository autorRepository, PremioRepository premioRepository) {
        this.paisRepository = paisRepository;
        this.autorRepository = autorRepository;
        this.premioRepository = premioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("items", paisRepository.findAll());
        return "paises/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new Pais());
        return "paises/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") Pais item) {
        paisRepository.save(item);
        return "redirect:/paises";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Pais item = paisRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/paises";
        }
        model.addAttribute("item", item);
        return "paises/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id,
                      @RequestParam(value = "autorOrder", defaultValue = "asc") String autorOrder,
                      Model model) {
        Pais item = paisRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/paises";
        }

        Comparator<Autor> comparadorAutores = Comparator.comparing(
                autor -> autor.getNombreCompletoListado() == null ? "" : autor.getNombreCompletoListado(),
                String.CASE_INSENSITIVE_ORDER);

        if ("desc".equalsIgnoreCase(autorOrder)) {
            comparadorAutores = comparadorAutores.reversed();
        }

        List<Autor> autoresDelPais = autorRepository.findAll().stream()
                .filter(autor -> autor.getPais() != null && autor.getPais().getId() != null && id.equals(autor.getPais().getId()))
                .sorted(comparadorAutores)
                .toList();

        List<Premio> premiosDelPais = premioRepository.findAll().stream()
            .filter(premio -> premio.getPais() != null && premio.getPais().getId() != null && id.equals(premio.getPais().getId()))
            .sorted(Comparator.comparing(
                premio -> premio.getNomPremio() == null ? "" : premio.getNomPremio(),
                String.CASE_INSENSITIVE_ORDER))
            .toList();

        model.addAttribute("item", item);
        model.addAttribute("autorOrder", "desc".equalsIgnoreCase(autorOrder) ? "desc" : "asc");
        model.addAttribute("autoresDelPais", autoresDelPais);
        model.addAttribute("premiosDelPais", premiosDelPais);
        return "paises/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        paisRepository.deleteById(id);
        return "redirect:/paises";
    }
}
