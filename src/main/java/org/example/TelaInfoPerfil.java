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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.UsuarioDTO;

import java.io.File;


public class TelaInfoPerfil {

    private StackPane root;
    private ImageView imgAvatar;

    public void start(Stage stage) {
        // --- 1. CABEÇALHO ---
        HBox header = criarCabecalho(stage);

        // --- 2. CONTEÚDO (Cards) ---
        HBox conteudo = criarAreaCards(stage);

        // --- 3. LAYOUT FINAL ---
        VBox layoutPrincipal = new VBox(30, header, conteudo);
        layoutPrincipal.setPadding(new Insets(40, 60, 40, 60));
        layoutPrincipal.setAlignment(Pos.TOP_CENTER);

        root = new StackPane(layoutPrincipal);
        root.getStyleClass().add("fundo-perfil");
        root.setMinHeight(Region.USE_PREF_SIZE);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.getStyleClass().add("scroll-pane-info");

        Scene scene = new LayoutPadrao().criarCena(stage, scroll);

        try {
            scene.getStylesheets().add(getClass().getResource("/styles-info-perfil.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles-crop.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Navegacao.configurarJanela(stage, scene, "Informações Pessoais", true);
    }

    private HBox criarCabecalho(Stage stage) {
        Button btnVoltar = new Button("⬅");
        btnVoltar.getStyleClass().add("btn-voltar-verde");
        btnVoltar.setOnAction(e -> new TelaPerfil().start(stage));

        Label lblTitulo = new Label("Informações do seu perfil");
        lblTitulo.getStyleClass().add("titulo-pagina");

        Label lblSub = new Label("Você pode adicionar, alterar ou corrigir suas informações pessoais e os dados da conta.");
        lblSub.getStyleClass().add("subtitulo-pagina");

        VBox boxTextos = new VBox(5, lblTitulo, lblSub);

        HBox header = new HBox(20, btnVoltar, boxTextos);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox criarAreaCards(Stage stage) {
        UsuarioDTO user = Sessao.getUsuario();

        // ==========================================
        // CARD ESQUERDA (FOTO EDITÁVEL + NOME)
        // ==========================================
        VBox cardEsquerda = new VBox(20);
        cardEsquerda.getStyleClass().add("card-info-branco");
        cardEsquerda.setPrefSize(350, 400);
        cardEsquerda.setAlignment(Pos.CENTER);

        // --- LÓGICA DA FOTO COM HOVER ---
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(200, 200);
        avatarContainer.setMaxSize(200, 200);
        avatarContainer.setCursor(javafx.scene.Cursor.HAND);

        // 1. A Imagem (Camada de Baixo)
        imgAvatar = new ImageView();
        carregarFotoAtual(); // ⭐ Carrega a foto com as configurações corretas

        // 2. O Overlay "Editar" (Camada de Cima)
        Label iconEdit = new Label("✎");
        iconEdit.getStyleClass().add("icone-editar");
        Label lblEdit = new Label("Editar");
        lblEdit.getStyleClass().add("texto-editar");

        VBox overlay = new VBox(5, iconEdit, lblEdit);
        overlay.setAlignment(Pos.CENTER);
        overlay.getStyleClass().add("avatar-overlay");
        overlay.setOpacity(0);

        avatarContainer.getChildren().addAll(imgAvatar, overlay);

        // Recorte Redondo
        Circle clip = new Circle(100, 100, 100);
        avatarContainer.setClip(clip);

        // --- EVENTOS DE HOVER E CLIQUE ---
        avatarContainer.setOnMouseEntered(e -> overlay.setOpacity(1));
        avatarContainer.setOnMouseExited(e -> overlay.setOpacity(0));

        avatarContainer.setOnMouseClicked(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                Image imgRaw = new Image(file.toURI().toString());
                new TelaCortarFoto().showModal(root, imgRaw, () -> {
                    carregarFotoAtual();
                });
            }
        });

        String nomeDisplay = (user != null) ? user.getNome() : "Peba Souza";
        Label lblNomeDestaque = new Label(nomeDisplay);
        lblNomeDestaque.getStyleClass().add("nome-destaque");

        cardEsquerda.getChildren().addAll(avatarContainer, lblNomeDestaque);

        // ==========================================
        // CARD DIREITA (LISTA NAVEGÁVEL)
        // ==========================================
        VBox cardDireita = new VBox(0);
        cardDireita.getStyleClass().add("card-info-branco");
        cardDireita.setPrefWidth(600);
        cardDireita.setAlignment(Pos.CENTER_LEFT);
        cardDireita.setPadding(new Insets(20, 30, 20, 30));

        String nomeUsuario = "Peba Souza";
        String nomeCompleto = (user != null) ? user.getNome() : "Peba Sousa Rodrigues";
        String email = (user != null) ? user.getEmail() : "Kaykyterlesff@gmail.com";
        String dataNasc = "04/07/2003";
        String cpf = "***.456.789-**";
        String telefone = "(89) 9 99460-1686";

        cardDireita.getChildren().addAll(
                criarItemEditavel("Nome de Usuario:", nomeUsuario, () -> System.out.println("Editar Usuario")),
                criarItemEditavel("Nome:", nomeCompleto, () -> System.out.println("Editar Nome")),
                criarItemEditavel("Data de nascimento:", dataNasc, () -> System.out.println("Editar Data")),
                criarItemEditavel("CPF:", cpf, () -> System.out.println("Editar CPF")),
                criarItemEditavel("E-mail:", email, () -> System.out.println("Editar Email")),
                criarItemEditavel("Telefone:", telefone, () -> System.out.println("Editar Telefone"))
        );

        HBox container = new HBox(40, cardEsquerda, cardDireita);
        container.setAlignment(Pos.TOP_CENTER);
        return container;
    }

    private HBox criarItemEditavel(String titulo, String valor, Runnable acao) {
        Label lblTit = new Label(titulo);
        lblTit.getStyleClass().add("label-campo");

        Label lblVal = new Label(valor);
        lblVal.getStyleClass().add("valor-campo");

        VBox textos = new VBox(3, lblTit, lblVal);
        textos.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label seta = new Label("›");
        seta.getStyleClass().add("seta-navegacao");

        HBox container = new HBox(textos, spacer, seta);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("item-editavel");
        container.setPadding(new Insets(15, 5, 15, 5));

        container.setOnMouseClicked(e -> {
            if (acao != null) acao.run();
        });

        return container;
    }

    // ⭐ MÉTODO CORRIGIDO
    private void carregarFotoAtual() {
        UsuarioDTO user = Sessao.getUsuario();

        if (user != null && user.getFotoPerfil() != null && !user.getFotoPerfil().isEmpty()) {
            try {
                String base64 = user.getFotoPerfil();

                // Limpa o prefixo "data:image..." se existir
                if (base64.contains(",")) {
                    base64 = base64.split(",")[1];
                }

                // Decodifica
                byte[] imgBytes = java.util.Base64.getDecoder().decode(base64);
                Image imagemCarregada = new Image(new java.io.ByteArrayInputStream(imgBytes));

                // ⭐ CONFIGURAÇÃO CORRETA
                imgAvatar.setImage(imagemCarregada);
                imgAvatar.setPreserveRatio(true);  // Mantém proporção circular
                imgAvatar.setFitWidth(200);
                imgAvatar.setFitHeight(200);
                imgAvatar.setSmooth(true);

                System.out.println("✅ Foto carregada: " + (int)imagemCarregada.getWidth() + "x" + (int)imagemCarregada.getHeight() + "px");

            } catch (Exception e) {
                System.err.println("❌ ERRO ao carregar foto salva:");
                e.printStackTrace();
                carregarImagemPadrao();
            }
        } else {
            carregarImagemPadrao();
        }
    }

    // ⭐ MÉTODO AUXILIAR NOVO
    private void carregarImagemPadrao() {
        try {
            Image imagemPadrao = new Image(getClass().getResourceAsStream("/peba.png"));
            imgAvatar.setImage(imagemPadrao);
            imgAvatar.setPreserveRatio(true);
            imgAvatar.setFitWidth(200);
            imgAvatar.setFitHeight(200);
            imgAvatar.setSmooth(true);
        } catch (Exception e) {
            Image imagemWeb = new Image("https://cdn-icons-png.flaticon.com/512/847/847969.png");
            imgAvatar.setImage(imagemWeb);
            imgAvatar.setPreserveRatio(true);
            imgAvatar.setFitWidth(200);
            imgAvatar.setFitHeight(200);
            imgAvatar.setSmooth(true);
        }
    }
}