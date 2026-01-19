package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.Anuncio;
import java.time.format.DateTimeFormatter;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class TelaPrincipal {

    private static final String API_URL = "http://localhost:8089/anuncios";

    // Componentes de Layout
    private TilePane gradeProdutos;
    private VBox menuLateral;
    private boolean menuAberto = false;

    // Cache de dados
    private List<Anuncio> todosAnuncios = new ArrayList<>();

    public void start(Stage stage) {
        // --- 1. PREPARAÇÃO DA BUSCA ---
        TextField txtBusca = new TextField();
        txtBusca.setOnKeyReleased(e -> filtrarLista(txtBusca.getText()));

        // --- 2. MENU LATERAL ---
        menuLateral = new MenuLateral().criar(stage, this::alternarMenu);
        menuLateral.setMinWidth(280);
        menuLateral.setMaxWidth(280);
        menuLateral.setTranslateX(-280);

        Runnable acaoAbrirMenu = this::alternarMenu;

        // --- 3. CABEÇALHO ---
        HBox topo = Cabecalho.criar(stage, txtBusca, acaoAbrirMenu);

        // --- 4. CONTEÚDO (GRID) ---
        gradeProdutos = new TilePane();
        gradeProdutos.setHgap(20);
        gradeProdutos.setVgap(20);
        gradeProdutos.setPadding(new Insets(30));
        gradeProdutos.setPrefColumns(4);
        gradeProdutos.setAlignment(Pos.TOP_LEFT);

        Label lblCarregando = new Label("Carregando ofertas...");
        lblCarregando.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
        gradeProdutos.getChildren().add(lblCarregando);

        ScrollPane scrollPane = new ScrollPane(gradeProdutos);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f0f2f5; -fx-background-color: transparent;");
        scrollPane.setPannable(true);

        VBox conteudoPrincipal = new VBox(topo, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        conteudoPrincipal.setStyle("-fx-background-color: #f0f2f5;");

        // --- 5. ESTRUTURA RAIZ ---
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().addAll(conteudoPrincipal, menuLateral);
        StackPane.setAlignment(menuLateral, Pos.CENTER_LEFT);

        // --- 6. CENA ---
        Scene scene = new Scene(root, AppConfig.LARGURA_INICIAL, AppConfig.ALTURA_INICIAL);
        try { scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm()); } catch (Exception e) {}

        Navegacao.configurarJanela(stage, scene, "Home - ItaimVende", true);

        // --- 7. CARREGA DADOS ---
        carregarAnunciosDoBanco();
    }

    private void alternarMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), menuLateral);
        if (menuAberto) {
            transition.setToX(-280);
            menuAberto = false;
        } else {
            transition.setToX(0);
            menuAberto = true;
        }
        transition.play();
    }

    private void carregarAnunciosDoBanco() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET().build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            todosAnuncios = mapper.readValue(response.body(), new TypeReference<List<Anuncio>>() {});
                            Platform.runLater(() -> atualizarTela(todosAnuncios));
                        } catch (Exception e) { e.printStackTrace(); }
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> gradeProdutos.getChildren().add(new Label("Erro de conexão.")));
                    return null;
                });
    }

    private void filtrarLista(String texto) {
        if (texto == null || texto.isEmpty()) {
            atualizarTela(todosAnuncios);
            return;
        }
        String termo = texto.toLowerCase();
        List<Anuncio> filtrados = todosAnuncios.stream()
                .filter(a -> a.getTitulo().toLowerCase().contains(termo) ||
                        (a.getCidade() != null && a.getCidade().toLowerCase().contains(termo)))
                .collect(Collectors.toList());
        atualizarTela(filtrados);
    }

    private void atualizarTela(List<Anuncio> anuncios) {
        gradeProdutos.getChildren().clear();
        if (anuncios.isEmpty()) {
            Label lblVazio = new Label("Nenhum anúncio encontrado.");
            lblVazio.setStyle("-fx-font-size: 18px; -fx-text-fill: #555;");
            gradeProdutos.getChildren().add(lblVazio);
            return;
        }
        for (Anuncio a : anuncios) {
            gradeProdutos.getChildren().add(criarCardAnuncio(a));
        }
    }

    private VBox criarCardAnuncio(Anuncio anuncio) {
        // VBox do Card (Mantém o estilo cinza arredondado atual)
        VBox card = new VBox(2);
        card.getStyleClass().add("card-produto");

        // --- 1. CONFIGURAÇÃO DA ÁREA DA FOTO ---
        StackPane imgContainer = new StackPane();
        double tamanhoDestino = 200; // Tamanho do quadrado da foto (interno)

        imgContainer.setPrefSize(tamanhoDestino, tamanhoDestino);
        imgContainer.setMinSize(tamanhoDestino, tamanhoDestino);
        imgContainer.setMaxSize(tamanhoDestino, tamanhoDestino);
        imgContainer.setAlignment(Pos.CENTER); // Centraliza para cortar as bordas iguais

        // Recorte Arredondado (Mantém o visual atual)
        Rectangle clip = new Rectangle(tamanhoDestino, tamanhoDestino);
        clip.setArcWidth(25);
        clip.setArcHeight(25);
        imgContainer.setClip(clip);

        ImageView imgView = new ImageView();

        // --- LÓGICA OLX: "CENTER CROP" (PREENCHER SEM ACHATAR) ---
        if (anuncio.getImagens() != null && !anuncio.getImagens().isEmpty()) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(anuncio.getImagens().get(0).getFotoBase64());
                Image imagemOriginal = new Image(new ByteArrayInputStream(imgBytes));
                imgView.setImage(imagemOriginal);

                // 1. Pega tamanho original da foto
                double w = imagemOriginal.getWidth();
                double h = imagemOriginal.getHeight();

                // 2. Calcula o zoom necessário (A Mágica!)
                // Compara a largura da foto com o quadrado e a altura com o quadrado.
                // O Math.max escolhe o MAIOR zoom para garantir que não sobre espaço branco.
                double escala = Math.max(tamanhoDestino / w, tamanhoDestino / h);

                // 3. Aplica o tamanho novo (Zoom)
                imgView.setFitWidth(w * escala);
                imgView.setFitHeight(h * escala);

                // 4. Configurações de qualidade
                imgView.setPreserveRatio(true); // Garante a proporção
                imgView.setSmooth(true);        // Suaviza os serrilhados

            } catch (Exception e) { e.printStackTrace(); }
        } else {
            // Fundo cinza se não tiver foto
            imgContainer.setStyle("-fx-background-color: #cccccc;");
        }

        // Coraçãozinho
        Label btnHeart = new Label("❤");
        btnHeart.getStyleClass().add("card-heart");
        StackPane.setAlignment(btnHeart, Pos.TOP_RIGHT);
        StackPane.setMargin(btnHeart, new Insets(10));

        if (imgView.getImage() != null) imgContainer.getChildren().add(imgView);
        imgContainer.getChildren().add(btnHeart);

        // --- 2. TEXTOS (Mantém seu layout atual) ---
        Label lblTitulo = new Label(anuncio.getTitulo());
        lblTitulo.getStyleClass().add("card-titulo");
        lblTitulo.setWrapText(true);
        lblTitulo.setMaxWidth(tamanhoDestino);

        Label lblPreco = new Label("R$ " + String.format("%.2f", anuncio.getPreco()));
        lblPreco.getStyleClass().add("card-preco");

        String dataFormatada = (anuncio.getCriadoEm() != null) ?
                anuncio.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoje";
        String local = (anuncio.getCidade() != null ? anuncio.getCidade() : "Local") +
                (anuncio.getEstado() != null ? " - " + anuncio.getEstado() : "");

        Label lblFooter = new Label(dataFormatada + "  " + local);
        lblFooter.getStyleClass().add("card-footer");

        card.getChildren().addAll(imgContainer, lblTitulo, lblPreco, lblFooter);

        // Clique para detalhes
        card.setOnMouseClicked(e -> new TelaDetalhes().start((Stage) card.getScene().getWindow(), anuncio));

        return card;
    }

    private void abrirDetalhes(javafx.stage.Window window, Anuncio anuncio) {
        new TelaDetalhes().start((Stage) window, anuncio);
    }
}