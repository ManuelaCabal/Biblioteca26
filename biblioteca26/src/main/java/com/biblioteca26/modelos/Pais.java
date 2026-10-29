package com.biblioteca26.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "paises")
public class Pais {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "nom_pais")
    private String nomPais;

    @Column(name = "bandera")
    private String bandera;

    public Pais() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomPais() {
        return nomPais;
    }

    public void setNomPais(String nomPais) {
        this.nomPais = nomPais;
    }

    public String getBandera() {
        return bandera;
    }

    public void setBandera(String bandera) {
        this.bandera = bandera;
    }

    public String getBanderaPath() {
        if (bandera == null || bandera.isBlank()) {
            return null;
        }

        String valor = bandera.trim().replace("\\", "/");

        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }

        if (valor.startsWith("/")) {
            return valor;
        }

        if (valor.startsWith("static/")) {
            return "/" + valor.substring("static/".length());
        }

        if (valor.startsWith("img/paises/")) {
            return "/" + valor;
        }

        if (valor.contains("/")) {
            return "/" + valor;
        }

        return "/img/paises/" + valor;
    }
}
