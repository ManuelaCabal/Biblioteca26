package com.biblioteca26.controladores;

import com.biblioteca26.modelos.Premio;
import com.biblioteca26.repositorios.AutorPremioRepository;
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
import java.util.Locale;

@Controller
@RequestMapping("/premios")
public class PremioController {

    private final PremioRepository premioRepository;
    private final PaisRepository paisRepository;
    private final AutorPremioRepository autorPremioRepository;

    public PremioController(PremioRepository premioRepository,
                            PaisRepository paisRepository,
                            AutorPremioRepository autorPremioRepository) {
        this.premioRepository = premioRepository;
        this.paisRepository = paisRepository;
        this.autorPremioRepository = autorPremioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "tipo", required = false) String tipo, Model model) {
        List<Premio> items = premioRepository.findAll().stream()
                .filter(premio -> {
                    if (tipo == null || tipo.isBlank()) {
                        return true;
                    }
                    return premio.getTipo() != null && premio.getTipo().equalsIgnoreCase(tipo.trim());
                })
                .sorted((left, right) -> {
                    int tipoCompare = Integer.compare(tipoOrden(left.getTipo()), tipoOrden(right.getTipo()));
                    if (tipoCompare != 0) {
                        return tipoCompare;
                    }
                    String leftNombre = left.getNomPremio() == null ? "" : left.getNomPremio();
                    String rightNombre = right.getNomPremio() == null ? "" : right.getNomPremio();
                    return leftNombre.compareToIgnoreCase(rightNombre);
                })
                .toList();

        String tipoFiltro = "";
        if (tipo != null && !tipo.isBlank()) {
            String normalized = tipo.trim().toUpperCase(Locale.ROOT);
            if ("A".equals(normalized) || "L".equals(normalized)) {
                tipoFiltro = normalized;
            }
        }

        model.addAttribute("tipoFiltro", tipoFiltro);
        model.addAttribute("items", items);
        return "premios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new Premio());
        model.addAttribute("paises", paisRepository.findAll());
        return "premios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") Premio item) {
        premioRepository.save(item);
        return "redirect:/premios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Premio item = premioRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/premios";
        }
        model.addAttribute("item", item);
        model.addAttribute("paises", paisRepository.findAll());
        return "premios/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Integer id, Model model) {
        Premio item = premioRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/premios";
        }

        List<AutorPremioGanadorView> autoresGanadores = autorPremioRepository.findAll().stream()
            .filter(relacion -> relacion.getPremio() != null
                && relacion.getPremio().getId() != null
                && id.equals(relacion.getPremio().getId())
                && relacion.getAutor() != null)
            .map(relacion -> new AutorPremioGanadorView(
                relacion.getAutor().getNombreCompletoListado(),
                relacion.getAutor().getFotoPath(),
                relacion.getPremio().getLogotipoPath()))
            .sorted(Comparator.comparing(
                ganador -> ganador.nombreAutor() == null ? "" : ganador.nombreAutor(),
                String.CASE_INSENSITIVE_ORDER))
            .toList();

        model.addAttribute("item", item);
        model.addAttribute("autoresGanadores", autoresGanadores);
        return "premios/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        premioRepository.deleteById(id);
        return "redirect:/premios";
    }

    private int tipoOrden(String tipo) {
        if (tipo == null) {
            return 99;
        }
        String normalized = tipo.trim().toUpperCase(Locale.ROOT);
        if ("A".equals(normalized)) {
            return 0;
        }
        if ("L".equals(normalized)) {
            return 1;
        }
        return 2;
    }

    private record AutorPremioGanadorView(String nombreAutor, String fotoPath, String logotipoPremioPath) {
    }
}
