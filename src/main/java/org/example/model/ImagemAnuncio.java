package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImagemAnuncio {

    private Long id;

    @JsonProperty("foto_base64") // Garante que o JSON envie com o nome certo para o backend
    private String fotoBase64;

    // ESTE CAMPO ESTAVA FALTANDO
    @JsonProperty("e_principal") // Mapeia para o campo 'e_principal' do banco/backend
    private boolean principal;

    // --- CONSTRUTORES ---
    public ImagemAnuncio() {}

    public ImagemAnuncio(String fotoBase64, boolean principal) {
        this.fotoBase64 = fotoBase64;
        this.principal = principal;
    }

    // --- GETTERS E SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }

    // O método que estava dando erro
    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }
}