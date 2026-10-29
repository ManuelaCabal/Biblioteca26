package com.biblioteca26.controladores;

import com.biblioteca26.modelos.Autor;
import com.biblioteca26.modelos.AutorLibro;
import com.biblioteca26.modelos.AutorPremio;
import com.biblioteca26.modelos.Libro;
import com.biblioteca26.modelos.LibroPremio;
import com.biblioteca26.modelos.Pais;
import com.biblioteca26.modelos.Premio;
import com.biblioteca26.repositorios.AutorRepository;
import com.biblioteca26.repositorios.AutorLibroRepository;
import com.biblioteca26.repositorios.AutorPremioRepository;
import com.biblioteca26.repositorios.LibroPremioRepository;
import com.biblioteca26.repositorios.PaisRepository;
import com.biblioteca26.repositorios.PremioRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/autores")
public class AutorController {

    private final AutorRepository autorRepository;
    private final AutorLibroRepository autorLibroRepository;
    private final AutorPremioRepository autorPremioRepository;
    private final LibroPremioRepository libroPremioRepository;
    private final PaisRepository paisRepository;
    private final PremioRepository premioRepository;

    public AutorController(AutorRepository autorRepository,
                           AutorLibroRepository autorLibroRepository,
                           AutorPremioRepository autorPremioRepository,
                          LibroPremioRepository libroPremioRepository,
                           PaisRepository paisRepository,
                           PremioRepository premioRepository) {
        this.autorRepository = autorRepository;
        this.autorLibroRepository = autorLibroRepository;
        this.autorPremioRepository = autorPremioRepository;
        this.libroPremioRepository = libroPremioRepository;
        this.paisRepository = paisRepository;
        this.premioRepository = premioRepository;
    }

    @GetMapping
    public String listar(@RequestParam(name = "dir", defaultValue = "asc") String dir,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         @RequestParam(name = "q", defaultValue = "") String q,
                         @RequestParam(name = "qp", defaultValue = "") String qp,
                         Model model) {
        boolean descending = "desc".equalsIgnoreCase(dir);
        String searchQuery = q == null ? "" : q.trim();
        String searchPremioQuery = qp == null ? "" : qp.trim();
        Comparator<Autor> comparator = (left, right) -> {
            if (left.getfNac() == null && right.getfNac() == null) {
                return compareByNameThenId(left, right);
            }
            if (left.getfNac() == null) {
                return 1;
            }
            if (right.getfNac() == null) {
                return -1;
            }

            int result = left.getfNac().compareTo(right.getfNac());
            if (result == 0) {
                return compareByNameThenId(left, right);
            }
            return result;
        };

        List<Autor> items = autorRepository.findAll();
        Set<Integer> autoresConPremioCoincidente = new HashSet<>();
        if (!searchPremioQuery.isEmpty()) {
            List<AutorPremio> relaciones = autorPremioRepository.findAll();
            for (AutorPremio relacion : relaciones) {
                if (relacion.getAutor() == null || relacion.getAutor().getId() == null || relacion.getPremio() == null) {
                    continue;
                }
                String nombrePremio = relacion.getPremio().getNomPremio();
                if (nombrePremio != null && nombrePremio.toLowerCase().contains(searchPremioQuery.toLowerCase())) {
                    autoresConPremioCoincidente.add(relacion.getAutor().getId());
                }
            }
        }

        if (!searchQuery.isEmpty()) {
            List<Autor> filtrados = new ArrayList<>();
            for (Autor autor : items) {
                if (coincideBusquedaAutor(autor, searchQuery) && coincideBusquedaPremioAutor(autor, searchPremioQuery, autoresConPremioCoincidente)) {
                    filtrados.add(autor);
                }
            }
            items = filtrados;
        } else if (!searchPremioQuery.isEmpty()) {
            List<Autor> filtrados = new ArrayList<>();
            for (Autor autor : items) {
                if (coincideBusquedaPremioAutor(autor, searchPremioQuery, autoresConPremioCoincidente)) {
                    filtrados.add(autor);
                }
            }
            items = filtrados;
        }
        items.sort(descending ? comparator.reversed() : comparator);

        int pageSize = 10;
        int totalItems = items.size();
        int totalPages = totalItems == 0 ? 1 : (int) Math.ceil((double) totalItems / pageSize);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = Math.min((currentPage - 1) * pageSize, totalItems);
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<Autor> pagedItems = items.subList(fromIndex, toIndex);

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
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("librosPorAutor", construirLibrosPorAutor(pagedItems));
        model.addAttribute("premiosPorAutor", construirPremiosPorAutor(pagedItems));
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("searchPremioQuery", searchPremioQuery);
        model.addAttribute("sortDir", descending ? "desc" : "asc");
        return "autores/lista";
    }

    private boolean coincideBusquedaAutor(Autor autor, String query) {
        String termino = query.toLowerCase();

        String nombre = autor.getNomAutor() == null ? "" : autor.getNomAutor().toLowerCase();
        String apellido1 = autor.getApellido1() == null ? "" : autor.getApellido1().toLowerCase();
        String apellido2 = autor.getApellido2() == null ? "" : autor.getApellido2().toLowerCase();

        return nombre.contains(termino) || apellido1.contains(termino) || apellido2.contains(termino);
    }

    private boolean coincideBusquedaPremioAutor(Autor autor, String query, Set<Integer> autoresConPremioCoincidente) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return autor.getId() != null && autoresConPremioCoincidente.contains(autor.getId());
    }

    private Map<Integer, List<LibroAutorView>> construirLibrosPorAutor(List<Autor> autoresPaginados) {
        List<AutorLibro> relaciones = autorLibroRepository.findAll();
        Map<Integer, List<LibroAutorView>> librosPorAutor = new HashMap<>();

        for (Autor autor : autoresPaginados) {
            if (autor.getId() != null) {
                librosPorAutor.put(autor.getId(), new ArrayList<>());
            }
        }

        for (AutorLibro relacion : relaciones) {
            if (relacion.getAutor() == null || relacion.getAutor().getId() == null || relacion.getLibro() == null) {
                continue;
            }

            Integer autorId = relacion.getAutor().getId();
            if (!librosPorAutor.containsKey(autorId)) {
                continue;
            }

            String titulo = relacion.getLibro().getNomLibro();
            Integer ano = relacion.getLibro().getAno();
            librosPorAutor.get(autorId).add(new LibroAutorView(titulo, ano));
        }

        for (List<LibroAutorView> libros : librosPorAutor.values()) {
            libros.sort((left, right) -> {
                Integer leftAno = left.ano();
                Integer rightAno = right.ano();
                if (leftAno == null && rightAno == null) {
                    String leftTitulo = left.titulo() == null ? "" : left.titulo();
                    String rightTitulo = right.titulo() == null ? "" : right.titulo();
                    return leftTitulo.compareToIgnoreCase(rightTitulo);
                }
                if (leftAno == null) {
                    return 1;
                }
                if (rightAno == null) {
                    return -1;
                }

                int result = leftAno.compareTo(rightAno);
                if (result != 0) {
                    return result;
                }

                String leftTitulo = left.titulo() == null ? "" : left.titulo();
                String rightTitulo = right.titulo() == null ? "" : right.titulo();
                return leftTitulo.compareToIgnoreCase(rightTitulo);
            });
        }

        return librosPorAutor;
    }

    private record LibroAutorView(String titulo, Integer ano) {
    }

    private Map<Integer, List<PremioAutorView>> construirPremiosPorAutor(List<Autor> autoresPaginados) {
        List<AutorPremio> relaciones = autorPremioRepository.findAll();
        Map<Integer, List<PremioAutorView>> premiosPorAutor = new HashMap<>();

        for (Autor autor : autoresPaginados) {
            if (autor.getId() != null) {
                premiosPorAutor.put(autor.getId(), new ArrayList<>());
            }
        }

        for (AutorPremio relacion : relaciones) {
            if (relacion.getAutor() == null
                    || relacion.getAutor().getId() == null
                    || relacion.getPremio() == null) {
                continue;
            }

            Integer autorId = relacion.getAutor().getId();
            if (!premiosPorAutor.containsKey(autorId)) {
                continue;
            }

            String nombre = relacion.getPremio().getNomPremio();
            Integer anio = relacion.getAnio();
            String logoPath = relacion.getPremio().getLogotipoPath();
            premiosPorAutor.get(autorId).add(new PremioAutorView(nombre, anio, logoPath));
        }

        for (List<PremioAutorView> premios : premiosPorAutor.values()) {
            premios.sort((left, right) -> {
                Integer leftAnio = left.anio();
                Integer rightAnio = right.anio();
                if (leftAnio == null && rightAnio == null) {
                    String leftNombre = left.nombre() == null ? "" : left.nombre();
                    String rightNombre = right.nombre() == null ? "" : right.nombre();
                    return leftNombre.compareToIgnoreCase(rightNombre);
                }
                if (leftAnio == null) {
                    return 1;
                }
                if (rightAnio == null) {
                    return -1;
                }

                int result = leftAnio.compareTo(rightAnio);
                if (result != 0) {
                    return result;
                }

                String leftNombre = left.nombre() == null ? "" : left.nombre();
                String rightNombre = right.nombre() == null ? "" : right.nombre();
                return leftNombre.compareToIgnoreCase(rightNombre);
            });
        }

        return premiosPorAutor;
    }

    private record PremioAutorView(String nombre, Integer anio, String logoPath) {
    }

    private int compareByNameThenId(Autor left, Autor right) {
        String leftNombre = left.getNombreCompletoListado() == null ? "" : left.getNombreCompletoListado();
        String rightNombre = right.getNombreCompletoListado() == null ? "" : right.getNombreCompletoListado();
        int result = leftNombre.compareToIgnoreCase(rightNombre);
        if (result != 0) {
            return result;
        }

        Integer leftId = left.getId();
        Integer rightId = right.getId();
        if (leftId == null && rightId == null) {
            return 0;
        }
        if (leftId == null) {
            return -1;
        }
        if (rightId == null) {
            return 1;
        }
        return leftId.compareTo(rightId);
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("item", new Autor());
        model.addAttribute("paises", paisesOrdenados());
        model.addAttribute("fotosAutores", fotosAutoresOrdenadas());
        model.addAttribute("premiosTipoA", premiosTipoAOrdenados());
        model.addAttribute("premiosActuales", new ArrayList<>());
        return "autores/form";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("item") Autor item,
                         @RequestParam(value = "premioId", required = false) Integer[] premioIds,
                         @RequestParam(value = "premioAnio", required = false) Integer[] premioAnios) {
        Autor autorGuardado = autorRepository.save(item);

        if (autorGuardado.getId() != null) {
            List<AutorPremio> premiosActuales = autorPremioRepository.findAll().stream()
                    .filter(relacion -> relacion.getAutor() != null
                            && autorGuardado.getId().equals(relacion.getAutor().getId()))
                    .toList();
            if (!premiosActuales.isEmpty()) {
                autorPremioRepository.deleteAll(premiosActuales);
            }
        }

        if (premioIds != null && premioIds.length > 0) {
            List<AutorPremio> nuevasRelaciones = new ArrayList<>();
            for (int i = 0; i < premioIds.length; i++) {
                Integer premioId = premioIds[i];
                if (premioId == null || premioId <= 0) {
                    continue;
                }
                Premio premio = premioRepository.findById(premioId).orElse(null);
                if (premio == null || premio.getTipo() == null || !"A".equalsIgnoreCase(premio.getTipo())) {
                    continue;
                }

                Integer anio = null;
                if (premioAnios != null && i < premioAnios.length) {
                    anio = premioAnios[i];
                }

                AutorPremio relacion = new AutorPremio();
                relacion.setAutor(autorGuardado);
                relacion.setPremio(premio);
                relacion.setAnio(anio);
                nuevasRelaciones.add(relacion);
            }

            if (!nuevasRelaciones.isEmpty()) {
                autorPremioRepository.saveAll(nuevasRelaciones);
            }
        }

        return "redirect:/autores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Autor item = autorRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/autores";
        }
        model.addAttribute("item", item);
        model.addAttribute("paises", paisesOrdenados());
        model.addAttribute("fotosAutores", fotosAutoresOrdenadas());
        model.addAttribute("premiosTipoA", premiosTipoAOrdenados());
        model.addAttribute("premiosActuales", premiosActualesDelAutor(item.getId()));
        return "autores/form";
    }

    @GetMapping("/ver/{id}")
    public String ver(@PathVariable @NonNull Integer id, Model model) {
        Autor item = autorRepository.findById(id).orElse(null);
        if (item == null) {
            return "redirect:/autores";
        }

        List<LibroDetalleAutorView> librosDelAutor = construirLibrosDetalleDelAutor(item.getId());
        List<PremioDetalleAutorView> premiosDelAutor = construirPremiosDetalleDelAutor(item.getId());

        model.addAttribute("item", item);
        model.addAttribute("librosDelAutor", librosDelAutor);
        model.addAttribute("premiosDelAutor", premiosDelAutor);
        return "autores/view";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable @NonNull Integer id) {
        autorRepository.deleteById(id);
        return "redirect:/autores";
    }

    private List<LibroDetalleAutorView> construirLibrosDetalleDelAutor(Integer autorId) {
        if (autorId == null) {
            return new ArrayList<>();
        }

        List<AutorLibro> relacionesAutorLibro = autorLibroRepository.findAll().stream()
                .filter(relacion -> relacion.getAutor() != null
                        && relacion.getAutor().getId() != null
                        && autorId.equals(relacion.getAutor().getId())
                        && relacion.getLibro() != null)
                .toList();

        Set<Integer> librosIds = new HashSet<>();
        for (AutorLibro relacion : relacionesAutorLibro) {
            Integer libroId = relacion.getLibro().getId();
            if (libroId != null) {
                librosIds.add(libroId);
            }
        }

        Map<Integer, List<PremioLibroDetalleView>> premiosPorLibro = new HashMap<>();
        if (!librosIds.isEmpty()) {
            for (LibroPremio relacion : libroPremioRepository.findAll()) {
                if (relacion.getLibro() == null
                        || relacion.getLibro().getId() == null
                        || !librosIds.contains(relacion.getLibro().getId())
                        || relacion.getPremio() == null) {
                    continue;
                }

                Integer libroId = relacion.getLibro().getId();
                Premio premio = relacion.getPremio();
                premiosPorLibro.computeIfAbsent(libroId, ignored -> new ArrayList<>())
                        .add(new PremioLibroDetalleView(premio.getNomPremio(), premio.getLogotipoPath()));
            }
        }

        for (List<PremioLibroDetalleView> premios : premiosPorLibro.values()) {
            premios.sort((left, right) -> {
                String leftNombre = left.nombre() == null ? "" : left.nombre();
                String rightNombre = right.nombre() == null ? "" : right.nombre();
                return leftNombre.compareToIgnoreCase(rightNombre);
            });
        }

        List<LibroDetalleAutorView> resultado = new ArrayList<>();
        for (AutorLibro relacion : relacionesAutorLibro) {
            Libro libro = relacion.getLibro();
            Integer libroId = libro.getId();
            String titulo = libro.getNomLibro();
            Integer anio = libro.getAno();
            String portadaPath = resolverPortadaLibro(libro.getPortada());
            String serie = libro.getSerie() != null ? libro.getSerie().getDenominacion() : null;
            List<PremioLibroDetalleView> premios = libroId == null
                    ? new ArrayList<>()
                    : premiosPorLibro.getOrDefault(libroId, new ArrayList<>());

            resultado.add(new LibroDetalleAutorView(libroId, titulo, anio, portadaPath, serie, premios));
        }

        resultado.sort((left, right) -> {
            Integer leftAnio = left.anio();
            Integer rightAnio = right.anio();
            if (leftAnio == null && rightAnio != null) {
                return 1;
            }
            if (leftAnio != null && rightAnio == null) {
                return -1;
            }
            if (leftAnio != null) {
                int result = leftAnio.compareTo(rightAnio);
                if (result != 0) {
                    return result;
                }
            }
            String leftTitulo = left.titulo() == null ? "" : left.titulo();
            String rightTitulo = right.titulo() == null ? "" : right.titulo();
            return leftTitulo.compareToIgnoreCase(rightTitulo);
        });

        return resultado;
    }

    private String resolverPortadaLibro(String portada) {
        if (portada == null || portada.isBlank()) {
            return null;
        }

        String valor = portada.trim().replace("\\", "/");

        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }

        if (valor.startsWith("/")) {
            return valor;
        }

        if (valor.startsWith("static/")) {
            return "/" + valor.substring("static/".length());
        }

        if (valor.startsWith("img/libros/")) {
            return "/" + valor;
        }

        if (valor.contains("/")) {
            return "/" + valor;
        }

        return "/img/libros/" + valor;
    }

    private List<PremioDetalleAutorView> construirPremiosDetalleDelAutor(Integer autorId) {
        if (autorId == null) {
            return new ArrayList<>();
        }

        List<PremioDetalleAutorView> resultado = new ArrayList<>();
        for (AutorPremio relacion : autorPremioRepository.findAll()) {
            if (relacion.getAutor() == null
                    || relacion.getAutor().getId() == null
                    || !autorId.equals(relacion.getAutor().getId())
                    || relacion.getPremio() == null) {
                continue;
            }

            Premio premio = relacion.getPremio();
            resultado.add(new PremioDetalleAutorView(
                    premio.getNomPremio(),
                    premio.getLogotipoPath(),
                    relacion.getAnio()));
        }

        resultado.sort((left, right) -> {
            Integer leftAnio = left.anio();
            Integer rightAnio = right.anio();
            if (leftAnio == null && rightAnio != null) {
                return 1;
            }
            if (leftAnio != null && rightAnio == null) {
                return -1;
            }
            if (leftAnio != null) {
                int result = leftAnio.compareTo(rightAnio);
                if (result != 0) {
                    return result;
                }
            }
            String leftNombre = left.nombre() == null ? "" : left.nombre();
            String rightNombre = right.nombre() == null ? "" : right.nombre();
            return leftNombre.compareToIgnoreCase(rightNombre);
        });

        return resultado;
    }

    private record PremioLibroDetalleView(String nombre, String logoPath) {
    }

    private record LibroDetalleAutorView(
            Integer id,
            String titulo,
            Integer anio,
            String portadaPath,
            String serie,
            List<PremioLibroDetalleView> premios) {
    }

    private record PremioDetalleAutorView(String nombre, String logoPath, Integer anio) {
    }

    private List<Pais> paisesOrdenados() {
        return paisRepository.findAll().stream()
                .sorted(Comparator.comparing(Pais::getNomPais,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private List<String> fotosAutoresOrdenadas() {
        try {
            File carpeta = new ClassPathResource("static/img/autores").getFile();
            if (!carpeta.exists() || !carpeta.isDirectory()) {
                return new ArrayList<>();
            }
            File[] archivos = carpeta.listFiles();
            if (archivos == null) {
                return new ArrayList<>();
            }
            return java.util.Arrays.stream(archivos)
                    .filter(File::isFile)
                    .map(File::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private List<Premio> premiosTipoAOrdenados() {
        return premioRepository.findAll().stream()
                .filter(premio -> premio != null && premio.getTipo() != null && "A".equalsIgnoreCase(premio.getTipo()))
                .sorted(Comparator.comparing(Premio::getNomPremio,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private List<AutorPremio> premiosActualesDelAutor(Integer autorId) {
        if (autorId == null) {
            return new ArrayList<>();
        }
        return autorPremioRepository.findAll().stream()
                .filter(relacion -> relacion.getAutor() != null && autorId.equals(relacion.getAutor().getId()))
                .sorted(Comparator.comparing(AutorPremio::getAnio,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }
}
