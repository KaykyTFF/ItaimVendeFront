package org.example.model;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Anuncio {


    public void adicionarImagem(String base64, boolean principal) {
        // Cria a imagem e adiciona na lista
        ImagemAnuncio img = new ImagemAnuncio(base64, principal);
        this.imagens.add(img);
    }

    private Long id;            // Long permite ser nulo (ainda não salvo)
    private Long usuarioId;

    private String titulo;
    private String descricao;
    private Double preco;       // Double permite nulo e é melhor para JSON

    private String categoria;
    private String tipo;
    private String bairro;
    private String cidade;
    private String estado;
    private String contato;
    private Boolean ativo;
    private LocalDateTime criadoEm;

    // --- LISTA DE IMAGENS (NOVO) ---
    private List<ImagemAnuncio> imagens = new ArrayList<>();

    public Anuncio() {}

    // --- GETTERS  ---
    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Double getPreco() { return preco; }
    public String getCategoria() { return categoria; }
    public String getTipo() { return tipo; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getContato() { return contato; }
    public Boolean getAtivo() { return ativo; }

    // Getter da Lista de Imagens
    public List<ImagemAnuncio> getImagens() { return imagens; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setPreco(Double preco) { this.preco = preco; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setContato(String contato) { this.contato = contato; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    // Setter da Lista de Imagens
    public void setImagens(List<ImagemAnuncio> imagens) { this.imagens = imagens; }

    // --- MÉTODO ÚTIL PARA O CARD ---
    public String getFotoCapa() {
        if (imagens != null && !imagens.isEmpty()) {
            return imagens.get(0).getFotoBase64();
        }
        return null;
    }
}