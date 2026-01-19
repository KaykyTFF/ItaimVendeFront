package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.example.model.UsuarioDTO;

public class TelaPerfil {

    public void start(Stage stage) {
        // --- 1. TÍTULO ---
        Label lblTitulo = new Label("Meu perfil");
        lblTitulo.getStyleClass().add("titulo-pagina");

        HBox boxTitulo = new HBox(lblTitulo);
        boxTitulo.setAlignment(Pos.CENTER);
        boxTitulo.setPadding(new Insets(40, 0, 40, 0));

        // --- 2. CABEÇALHO ---
        HBox boxUsuario = criarCabecalhoUsuario(stage);

        // --- 3. GRADE DE CARDS ---
        VBox boxCards = criarGradeOpcoes();

        // --- 4. LAYOUT ---
        // VBox.setVgrow garante que os cards empurrem o fundo se necessário
        VBox layoutPrincipal = new VBox(10, boxTitulo, boxUsuario, boxCards);
        layoutPrincipal.setPadding(new Insets(0, 0, 50, 0));
        layoutPrincipal.setAlignment(Pos.TOP_CENTER);

        StackPane root = new StackPane(layoutPrincipal);
        root.getStyleClass().add("fundo-perfil");
        // Garante que o StackPane preencha todo o espaço
        root.setMinHeight(Region.USE_PREF_SIZE);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        // --- [A MÁGICA ESTÁ AQUI] ---
        // Isso força o fundo cinza a ir até o final da tela
        scroll.setFitToHeight(true);
        // ----------------------------

        scroll.getStyleClass().add("scroll-pane-perfil");

        Scene scene = new LayoutPadrao().criarCena(stage, scroll);

        try {
            scene.getStylesheets().add(getClass().getResource("/styles-perfil.css").toExternalForm());
        } catch (Exception e) { e.printStackTrace(); }

        Navegacao.configurarJanela(stage, scene, "Meu Perfil", true);
    }

    private HBox criarCabecalhoUsuario(Stage stage) {
        // Botão Voltar
        Button btnVoltar = new Button("⬅");
        btnVoltar.getStyleClass().add("btn-voltar-verde");
        btnVoltar.setOnAction(e -> new TelaPrincipal().start(stage));

        // Avatar
        ImageView imgAvatar = new ImageView();
        Image imagemCarregada = null;
        try {
            // Tenta carregar o Peba
            imagemCarregada = new Image(getClass().getResourceAsStream("/peba.png"));
        } catch (Exception e) { }

        if (imagemCarregada == null) {
            // Fallback online caso não tenha o arquivo local
            imagemCarregada = new Image("https://cdn-icons-png.flaticon.com/512/847/847969.png");
        }

        imgAvatar.setImage(imagemCarregada);
        imgAvatar.setFitWidth(70);
        imgAvatar.setFitHeight(70);
        imgAvatar.setPreserveRatio(false);

        // Recorte Redondo
        Circle clip = new Circle(35, 35, 35);
        imgAvatar.setClip(clip);

        // Textos
        UsuarioDTO user = Sessao.getUsuario();
        String nome = (user != null) ? user.getNome() : "Peba Sousa Rodrigues";
        String email = (user != null) ? user.getEmail() : "pebasousarodrigues@gmail.com";

        Label lblNome = new Label(nome);
        lblNome.getStyleClass().add("nome-usuario");

        Label lblEmail = new Label(email);
        lblEmail.getStyleClass().add("email-usuario");

        VBox txtDados = new VBox(2, lblNome, lblEmail);
        txtDados.setAlignment(Pos.CENTER_LEFT);

        // Montagem: Botão --(25px)-- Avatar --(15px)-- Texto
        HBox container = new HBox(25, btnVoltar, imgAvatar, txtDados);
        container.setAlignment(Pos.CENTER_LEFT);
        // Padding ajustado para centralizar visualmente com a grade
        container.setPadding(new Insets(0, 0, 50, 150));

        return container;
    }

    private VBox criarGradeOpcoes() {
        // LINHA 1
        HBox linha1 = new HBox(30); // Mais espaço entre os cards
        linha1.setAlignment(Pos.CENTER);

        VBox c1 = criarCard("/icon-info.png", "Informações do seu perfil", "Dados pessoais e da conta.");
            // ADICIONE A AÇÃO DE CLIQUE:
        c1.setOnMouseClicked(e -> {
            // Pega o stage de qualquer elemento da tela atual
            Stage stage = (Stage) c1.getScene().getWindow();
            new TelaInfoPerfil().start(stage);
        });

        VBox c2 = criarCard("/icon-security.png", "Segurança", "Você configurou a segurança da sua conta.");
        VBox c3 = criarCard("/icon-pin.png", "Endereços", "Endereços salvos na sua conta.");

        linha1.getChildren().addAll(c1, c2, c3);

        // LINHA 2
        HBox linha2 = new HBox(30);
        linha2.setAlignment(Pos.CENTER);

        VBox c4 = criarCard("/icon-shield.png", "Privacidade", "Preferências e controle do uso dos seus dados.");
        VBox c5 = criarCard("/icon-chat.png", "Comunicações", "Escolha que tipo de informação você quer receber.");

        linha2.getChildren().addAll(c4, c5);

        VBox grid = new VBox(30, linha1, linha2);
        grid.setAlignment(Pos.TOP_CENTER);
        return grid;
    }

    private VBox criarCard(String imagePath, String titulo, String subtitulo) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card-opcao");

        // --- AUMENTO DO TAMANHO DO CARD ---
        card.setPrefSize(340, 170);
        card.setMaxSize(340, 170);

        // Ícone de Imagem
        ImageView iconView = new ImageView();
        try {
            iconView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            // --- ÍCONES MAIORES E VISÍVEIS ---
            iconView.setFitHeight(48);
            iconView.setFitWidth(48);
            iconView.setPreserveRatio(true);
            // Removida a opacidade para ficarem pretos/nítidos
        } catch (Exception e) {
            // Se falhar, tenta carregar um ícone padrão ou deixa vazio
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("titulo-card");
        lblTitulo.setWrapText(true);
        lblTitulo.setAlignment(Pos.CENTER);

        Label lblSub = new Label(subtitulo);
        lblSub.getStyleClass().add("subtitulo-card");
        lblSub.setWrapText(true);
        lblSub.setAlignment(Pos.CENTER);
        lblSub.setMaxWidth(300); // Texto pode usar quase toda a largura

        card.getChildren().addAll(iconView, lblTitulo, lblSub);

        // Efeito Hover simples
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 5); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle(""));

        return card;
    }
}