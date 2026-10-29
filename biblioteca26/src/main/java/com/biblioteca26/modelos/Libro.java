package com.biblioteca26.modelos;

import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nom_libro")
    private String nomLibro;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "portada")
    private String portada;

    @Column(name = "nom_archivo")
    private String nomArchivo;

    @Column(name = "pelicula")
    private String pelicula;

    @Column(name = "comentario", columnDefinition = "MEDIUMTEXT")
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "serie_id")
    private Serie serie;

    @Column(name = "indice_serie", nullable = false)
    private Integer indiceSerie;

    public Libro() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomLibro() {
        return nomLibro;
    }

    public void setNomLibro(String nomLibro) {
        this.nomLibro = nomLibro;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    public String getNomArchivo() {
        return nomArchivo;
    }

    public void setNomArchivo(String nomArchivo) {
        this.nomArchivo = nomArchivo;
    }

    public String getPelicula() {
        return pelicula;
    }

    public void setPelicula(String pelicula) {
        this.pelicula = pelicula;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    public Integer getIndiceSerie() {
        return indiceSerie;
    }

    public void setIndiceSerie(Integer indiceSerie) {
        this.indiceSerie = indiceSerie;
    }

    public String getPortadaPath() {
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

    public String getArchivoPdfPath() {
        if (nomArchivo == null || nomArchivo.isBlank() || "0".equals(nomArchivo.trim())) {
            return null;
        }

        String valor = nomArchivo.trim().replace("\\", "/");
        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }

        if (valor.startsWith("/")) {
            return valor;
        }

        if (valor.startsWith("static/")) {
            return "/" + valor.substring("static/".length());
        }

        if (valor.startsWith("pdfs/")) {
            return "/" + valor;
        }

        if (valor.contains("/")) {
            return "/" + valor;
        }

        String nombreFinal = valor.toLowerCase(Locale.ROOT).endsWith(".pdf") ? valor : valor + ".pdf";
        return "/pdfs/" + nombreFinal;
    }
}
