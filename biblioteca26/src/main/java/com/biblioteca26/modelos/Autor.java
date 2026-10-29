package com.biblioteca26.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

@Entity
@Table(name = "autores")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nom_autor")
    private String nomAutor;

    @Column(name = "apellido1")
    private String apellido1;

    @Column(name = "apellido2")
    private String apellido2;

    @Column(name = "f_nac")
    private LocalDate fNac;

    @Column(name = "f_def")
    private LocalDate fDef;

    @ManyToOne
    @JoinColumn(name = "pais_id")
    private Pais pais;

    @Column(name = "foto")
    private String foto;

    @Column(name = "localidad")
    private String localidad;

    @Column(name = "biografia", columnDefinition = "MEDIUMTEXT")
    private String biografia;

    public Autor() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomAutor() {
        return nomAutor;
    }

    public void setNomAutor(String nomAutor) {
        this.nomAutor = nomAutor;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public LocalDate getfNac() {
        return fNac;
    }

    public void setfNac(LocalDate fNac) {
        this.fNac = fNac;
    }

    public LocalDate getfDef() {
        return fDef;
    }

    public void setfDef(LocalDate fDef) {
        this.fDef = fDef;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public String getFotoPath() {
        if (foto == null || foto.isBlank()) {
            return null;
        }

        String valor = foto.trim().replace("\\", "/");

        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }

        if (valor.startsWith("/")) {
            return valor;
        }

        if (valor.startsWith("static/")) {
            return "/" + valor.substring("static/".length());
        }

        if (valor.startsWith("img/autores/")) {
            return "/" + valor;
        }

        if (valor.contains("/")) {
            return "/" + valor;
        }

        return "/img/autores/" + valor;
    }

    public String getNombreCompletoListado() {
        StringJoiner apellidos = new StringJoiner(" ");
        if (apellido1 != null && !apellido1.isBlank()) {
            apellidos.add(apellido1.trim());
        }
        if (apellido2 != null && !apellido2.isBlank()) {
            apellidos.add(apellido2.trim());
        }

        String apellidosTexto = apellidos.toString();
        String nombre = nomAutor == null ? "" : nomAutor.trim();

        if (!apellidosTexto.isEmpty() && !nombre.isEmpty()) {
            return apellidosTexto + ", " + nombre;
        }
        if (!apellidosTexto.isEmpty()) {
            return apellidosTexto;
        }
        return nombre;
    }

    public Integer getEdadActual() {
        if (fNac == null || fDef != null) {
            return null;
        }
        LocalDate hoy = LocalDate.now();
        if (fNac.isAfter(hoy)) {
            return null;
        }
        return Period.between(fNac, hoy).getYears();
    }

    public String getFechaNacimientoEuropea() {
        return formatFechaEuropea(fNac);
    }

    public String getFechaDefuncionEuropea() {
        return formatFechaEuropea(fDef);
    }

    private String formatFechaEuropea(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
