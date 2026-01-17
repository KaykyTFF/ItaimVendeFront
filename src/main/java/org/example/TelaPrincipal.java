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
import org.example.model.ImagemAnuncio;

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
        // Cria o menu passando a ação de fechar
        menuLateral = new MenuLateral().criar(stage, this::alternarMenu);

        // --- CORREÇÃO DO BUG VISUAL (TELA PRETA) ---
        // Definimos largura fixa para o StackPane não esticar o menu na tela toda
        menuLateral.setMinWidth(280);
        menuLateral.setMaxWidth(280);

        // Esconde o menu inicialmente
        menuLateral.setTranslateX(-280);

        // Ação para o botão do cabeçalho
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
        // Alinha tudo à esquerda para o menu não flutuar no meio
        root.setAlignment(Pos.CENTER_LEFT);

        // Adiciona o conteúdo atrás e o menu na frente
        root.getChildren().addAll(conteudoPrincipal, menuLateral);

        // Garante que o menu fique alinhado à esquerda dentro do StackPane
        StackPane.setAlignment(menuLateral, Pos.CENTER_LEFT);

        // --- 6. CENA ---
        // Usa as constantes corrigidas do AppConfig
        Scene scene = new Scene(root, AppConfig.LARGURA_INICIAL, AppConfig.ALTURA_INICIAL);

        try { scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm()); } catch (Exception e) {}

        Navegacao.configurarJanela(stage, scene, "Home - ItaimVende", true);

        // --- 7. CARREGA DADOS ---
        carregarAnunciosDoBanco();
    }

    private void alternarMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), menuLateral);
        if (menuAberto) {
            transition.setToX(-280); // Esconde
            menuAberto = false;
        } else {
            transition.setToX(0);    // Mostra
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
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 0); -fx-background-radius: 12; -fx-cursor: hand;");
        card.setPrefWidth(240);
        card.setMinWidth(240);
        card.setMaxWidth(240);

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(210, 160);
        imageContainer.setMinSize(210, 160);
        imageContainer.setMaxSize(210, 160);

        Rectangle clip = new Rectangle(210, 160);
        clip.setArcWidth(10); clip.setArcHeight(10);
        imageContainer.setClip(clip);

        ImageView imgView = new ImageView();
        String fotoBase64 = null;
        if (anuncio.getImagens() != null && !anuncio.getImagens().isEmpty()) {
            fotoBase64 = anuncio.getImagens().stream().filter(ImagemAnuncio::isPrincipal).map(ImagemAnuncio::getFotoBase64).findFirst().orElse(anuncio.getImagens().get(0).getFotoBase64());
        }

        if (fotoBase64 != null) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(fotoBase64);
                Image imagemReal = new Image(new ByteArrayInputStream(imgBytes));
                imgView.setImage(imagemReal);
                if (imagemReal.getWidth() > 0) {
                    double scale = Math.max(210 / imagemReal.getWidth(), 160 / imagemReal.getHeight());
                    imgView.setFitWidth(imagemReal.getWidth() * scale);
                    imgView.setFitHeight(imagemReal.getHeight() * scale);
                    imgView.setPreserveRatio(true);
                }
            } catch (Exception e) {}
        } else {
            imageContainer.setStyle("-fx-background-color: #f0f2f5;");
            imageContainer.getChildren().add(new Label("Sem foto"));
        }

        imageContainer.getChildren().add(imgView);
        StackPane.setAlignment(imgView, Pos.CENTER);

        Label lblPreco = new Label("R$ " + String.format("%.2f", anuncio.getPreco()));
        lblPreco.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1c1e21;");

        Label lblTitulo = new Label(anuncio.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 15px; -fx-text-fill: #050505;");
        lblTitulo.setWrapText(true);
        lblTitulo.setMaxHeight(40);

        String cidade = anuncio.getCidade() != null ? anuncio.getCidade() : "Local não inf.";
        Label lblCidade = new Label("📍 " + cidade);
        lblCidade.setStyle("-fx-font-size: 12px; -fx-text-fill: #65676b;");

        Button btnVer = new Button("Ver Detalhes");
        btnVer.setMaxWidth(Double.MAX_VALUE);
        btnVer.setStyle("-fx-background-color: #e4e6eb; -fx-text-fill: black; -fx-cursor: hand; -fx-font-weight: bold;");

        btnVer.setOnAction(e -> abrirDetalhes(card.getScene().getWindow(), anuncio));
        card.setOnMouseClicked(e -> abrirDetalhes(card.getScene().getWindow(), anuncio));

        card.getChildren().addAll(imageContainer, lblPreco, lblTitulo, lblCidade, btnVer);
        return card;
    }

    private void abrirDetalhes(javafx.stage.Window window, Anuncio anuncio) {
        new TelaDetalhes().start((Stage) window, anuncio);
    }
}