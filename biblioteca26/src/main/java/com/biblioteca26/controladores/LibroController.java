package com.biblioteca26.controladores;

import com.biblioteca26.modelos.AutorLibro;
import com.biblioteca26.modelos.LibroPremio;
import com.biblioteca26.modelos.Libro;
import com.biblioteca26.repositorios.AutorLibroRepository;
import com.biblioteca26.repositorios.LibroRepository;
import com.biblioteca26.repositorios.LibroPremioRepository;
import com.biblioteca26.repositorios.SerieRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.lang.NonNull;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import java.text.Collator;

@Controller
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;
    private final SerieRepository serieRepository;
    private final AutorLibroRepository autorLibroRepository;
    private final LibroPremioRepository libroPremioRepository;

    public LibroController(LibroRepository libroRepository,
                           SerieRepository serieRepository,
                           AutorLibroRepository autorLibroRepository,
                           LibroPremioRepository libroPremioRepository) {
        this.libroRepository = libroRepository;
        this.serieRepository = serieRepository;
        this.autorLibroRepository = autorLibroRepository;
        this.libroPremioRepository = libroPremioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(name = "sort", defaultValue = "titulo") String sort,
                         @RequestParam(name = "dir", defaultValue = "asc") String dir,
                         @RequestParam(name = "q", defaultValue = "") String q,
                         @RequestParam(name = "qa", defaultValue = "") String qa,
                         @RequestParam(name = "qp", defaultValue = "") String qp,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         Model model) {
        boolean descending = "desc".equalsIgnoreCase(dir);
        String sortField = "ano".equalsIgnoreCase(sort) ? "ano" : "titulo";
        String searchTitulo = q == null ? "" : q.trim();
        String searchAutor = qa == null ? "" : qa.trim();
        String searchPremio = qp == null ? "" : qp.trim();

        List<Libro> items = libroRepository.findAll();
        Set<Integer> librosConAutorCoincidente = new HashSet<>();
        Set<Integer> librosConPremioCoincidente = new HashSet<>();

        if (!searchAutor.isEmpty()) {
            for (AutorLibro relacion : autorLibroRepository.findAll()) {
                if (relacion.getLibro() == null || relacion.getLibro().getId() == null || relacion.getAutor() == null) {
                    continue;
                }
                String nombreAutor = construirNombreAutor(relacion);
                if (!nombreAutor.isEmpty() && nombreAutor.toLowerCase().contains(searchAutor.toLowerCase())) {
                    librosConAutorCoincidente.add(relacion.getLibro().getId());
                }
            }
        }

        if (!searchPremio.isEmpty()) {
            for (LibroPremio relacion : libroPremioRepository.findAll()) {
                if (relacion.getLibro() == null || relacion.getLibro().getId() == null || relacion.getPremio() == null) {
                    continue;
                }
                String nombrePremio = relacion.getPremio().getNomPremio();
                if (nombrePremio != null && nombrePremio.toLowerCase().contains(searchPremio.toLowerCase())) {
                    librosConPremioCoincidente.add(relacion.getLibro().getId());
                }
            }
        }

        if (!searchTitulo.isEmpty() || !searchAutor.isEmpty() || !searchPremio.isEmpty()) {
            List<Libro> filtrados = new ArrayList<>();
            for (Libro libro : items) {
                if (coincideBusquedaTitulo(libro, searchTitulo)
                        && coincideBusquedaAutor(libro, searchAutor, librosConAutorCoincidente)
                        && coincideBusquedaPremio(libro, searchPremio, librosConPremioCoincidente)) {
                    filtrados.add(libro);
                }
            }
            items = filtrados;
        }

        Comparator<Libro> comparator = crearComparador(sortField, descending);

        items.sort(comparator);

        int pageSize = 10;
        int totalItems = items.size();
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / pageSize);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Libro> pagedItems = items.subList(fromIndex, toIndex);

        int windowStart = Math.max(1, currentPage - 2);
        int windowEnd = Math.min(totalPages, windowStart + 4);
        if (windowEnd - windowStart < 4) {
            windowStart = Math.max(1, windowEnd - 4);
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = windowStart; i <= windowEnd; i++) {
            pageNumbers.add(i);
        }

        model.addAttribute("items", pagedItems);
        model.addAttribute("autoresPorLibro", construirAutoresPorLibro(pagedItems));
        model.addAttribute("premiosPorLibro", construirPremiosPorLibro(pagedItems));
        model.addAttribute("archivosPorLibro", construirArchivosPorLibro(pagedItems));
        model.addAttribute("searchTitulo", searchTitulo);
        model.addAttribute("searchAutor", searchAutor);
        model.addAttribute("searchPremio", searchPremio);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", descending ? "desc" : "asc");
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        return "libros/lista";
    }

    private boolean coincideBusquedaTitulo(Libro libro, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String titulo = libro.getNomLibro() == null ? "" : libro.getNomLibro().toLowerCase();
        return titulo.contains(query.toLowerCase());
    }

    private boolean coincideBusquedaAutor(Libro libro, String query, Set<Integer> librosConAutorCoincidente) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return libro.getId() != null && librosConAutorCoincidente.contains(libro.getId());
    }

    private boolean coincideBusquedaPremio(Libro libro, String query, Set<Integer> librosConPremioCoincidente) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return libro.getId() != null && librosConPremioCoincidente.contains(libro.getId());
    }

    private Map<Integer, List<LibroAutorView>> construirAutoresPorLibro(List<Libro> librosFiltrados) {
        List<AutorLibro> relaciones = autorLibroRepository.findAll();
        Map<Integer, List<LibroAutorView>> autoresPorLibro = new HashMap<>();

        for (Libro libro : librosFiltrados) {
            if (libro.getId() != null) {
                autoresPorLibro.put(libro.getId(), new ArrayList<>());
            }
        }

        for (AutorLibro relacion : relaciones) {
            if (relacion.getLibro() == null || relacion.getLibro().getId() == null || relacion.getAutor() == null) {
                continue;
            }

            Integer libroId = relacion.getLibro().getId();
            if (!autoresPorLibro.containsKey(libroId)) {
                continue;
            }

            autoresPorLibro.get(libroId).add(new LibroAutorView(construirNombreAutor(relacion)));
        }

        for (List<LibroAutorView> autores : autoresPorLibro.values()) {
            autores.sort((left, right) -> left.nombre().compareToIgnoreCase(right.nombre()));
        }

        return autoresPorLibro;
    }

    private Map<Integer, List<LibroPremioView>> construirPremiosPorLibro(List<Libro> librosFiltrados) {
        List<LibroPremio> relaciones = libroPremioRepository.findAll();
        Map<Integer, List<LibroPremioView>> premiosPorLibro = new HashMap<>();

        for (Libro libro : librosFiltrados) {
            if (libro.getId() != null) {
                premiosPorLibro.put(libro.getId(), new ArrayList<>());
            }
        }

        for (LibroPremio relacion : relaciones) {
            if (relacion.getLibro() == null || relacion.getLibro().getId() == null || relacion.getPremio() == null) {
                continue;
            }

            Integer libroId = relacion.getLibro().getId();
            if (!premiosPorLibro.containsKey(libroId)) {
                continue;
            }

            premiosPorLibro.get(libroId).add(new LibroPremioView(
                    relacion.getPremio().getNomPremio(),
                    relacion.getPremio().getLogotipoPath()));
        }

        for (List<LibroPremioView> premios : premiosPorLibro.values()) {
            premios.sort((left, right) -> {
                String leftNombre = left.nombre() == null ? "" : left.nombre();
                String rightNombre = right.nombre() == null ? "" : right.nombre();
                return leftNombre.compareToIgnoreCase(rightNombre);
            });
        }

        return premiosPorLibro;
    }

    private Map<Integer, ArchivoLibroView> construirArchivosPorLibro(List<Libro> librosFiltrados) {
        Map<Integer, ArchivoLibroView> archivosPorLibro = new HashMap<>();
        for (Libro libro : librosFiltrados) {
            if (libro.getId() == null) {
                continue;
            }
            String archivoPdf = resolverArchivoPdf(libro.getNomArchivo());
            if (archivoPdf != null) {
                archivosPorLibro.put(libro.getId(), new ArchivoLibroView(archivoPdf, "PDF"));
            }
        }
        return archivosPorLibro;
    }

    private String resolverArchivoPdf(String nomArchivo) {
        if (nomArchivo == null || nomArchivo.isBlank() || "0".equals(nomArchivo.trim())) {
            return null;
        }

        String valor = nomArchivo.trim().replace("\\", "/");
        List<String> candidatos = new ArrayList<>();
        candidatos.add(valor);
        if (!valor.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            candidatos.add(valor + ".pdf");
        }

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (String candidato : candidatos) {
            String normalized = candidato.startsWith("/") ? candidato.substring(1) : candidato;
            Resource resource = resolver.getResource("classpath:/static/pdfs/" + normalized);
            if (resource.exists()) {
                return normalized;
            }
        }

        return null;
    }

    private String construirNombreAutor(AutorLibro relacion) {
        if (relacion.getAutor() == null) {
            return "";
        }

        StringBuilder nombre = new StringBuilder();
        if (relacion.getAutor().getNomAutor() != null) {
            nombre.append(relacion.getAutor().getNomAutor().trim());
        }
        if (relacion.getAutor().getApellido1() != null && !relacion.getAutor().getApellido1().isBlank()) {
            if (!nombre.isEmpty()) {
                nombre.append(' ');
            }
            nombre.append(relacion.getAutor().getApellido1().trim());
        }
        if (relacion.getAutor().getApellido2() != null && !relacion.getAutor().getApellido2().isBlank()) {
            if (!nombre.isEmpty()) {
                nombre.append(' ');
            }
            nombre.append(relacion.getAutor().getApellido2().trim());
        }
        return nombre.toString().trim();
    }

    private record LibroAutorView(String nombre) {
    }

    private record LibroPremioView(String nombre, String logoPath) {
    }

    private record ArchivoLibroView(String path, String label) {
    }

    private Comparator<Libro> crearComparador(String sortField, boolean descending) {
        Collator collator = Collator.getInstance(new Locale("es", "ES"));
        collator.setStrength(Collator.PRIMARY);

        if ("ano".equals(sortField)) {
            Comparator<Libro> base = Comparator.comparing(
                    (Libro libro) -> libro == null ? null : libro.getAno(),
                    LibroController::compareNullableInteger)
                    .thenComparing(
                            libro -> libro == null ? null : libro.getNomLibro(),
                            (left, right) -> compareNullableString(left, right, collator));
            return descending ? base.reversed() : base;
        }

        Comparator<Libro> base = Comparator.comparing(
                (Libro libro) -> libro == null ? null : libro.getNomLibro(),
                (left, right) -> compareNullableString(left, right, collator))
                .thenComparing(
                        libro -> libro == null ? null : libro.getAno(),
                        LibroController::compareNullableInteger);
        return descending ? base.reversed() : base;
    }

    private static int compareNullableInteger(Integer left, Integer right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private static int compareNullableString(String left, String right, Collator collator) {
        String leftValue = left == null ? "" : left;
        String rightValue = right == null ? "" : right;
        return collator.compare(leftValue, rightValue);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new Libro());
        model.addAttribute("series", serieRepository.findAll());
        return "libros/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") @NonNull Libro item) {
        libroRepository.save(item);
        return "redirect:/libros";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable @NonNull Integer id, Model model) {
        Libro item = libroRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/libros";
        }
        model.addAttribute("item", item);
        model.addAttribute("series", serieRepository.findAll());
        return "libros/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable @NonNull Integer id, Model model) {
        Libro item = libroRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/libros";
        }
        model.addAttribute("item", item);
        return "libros/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable @NonNull Integer id) {
        libroRepository.deleteById(id);
        return "redirect:/libros";
    }
}
