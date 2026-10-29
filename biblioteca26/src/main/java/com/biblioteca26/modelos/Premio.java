package com.biblioteca26.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "premios")
public class Premio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nom_premio")
    private String nomPremio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "logotipo", length = 30)
    private String logotipo;

    public Premio() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomPremio() {
        return nomPremio;
    }

    public void setNomPremio(String nomPremio) {
        this.nomPremio = nomPremio;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLogotipo() {
        return logotipo;
    }

    public void setLogotipo(String logotipo) {
        this.logotipo = logotipo;
    }

    public String getLogotipoPath() {
        if (logotipo == null || logotipo.isBlank()) {
            return null;
        }

        String valor = logotipo.trim().replace("\\", "/");

        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return valor;
        }

        if (valor.startsWith("/")) {
            return valor;
        }

        if (valor.startsWith("static/")) {
            return "/" + valor.substring("static/".length());
        }

        if (valor.startsWith("img/premios/")) {
            return "/" + valor;
        }

        if (valor.contains("/")) {
            return "/" + valor;
        }

        return "/img/premios/" + valor;
    }
}
