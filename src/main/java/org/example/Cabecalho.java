package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class Cabecalho {

    public static HBox criar(Stage stage, TextField txtBuscaExterno) {
        return criar(stage, txtBuscaExterno, null);
    }
<<<<<<< HEAD

=======
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
    public static HBox criar(Stage stage, TextField txtBuscaExterno, Runnable acaoMenu) {
        // --- 1. ESQUERDA (Menu + Logo) ---

        Button btnMenu = new Button("☰");
        btnMenu.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 24px; -fx-cursor: hand;");

<<<<<<< HEAD
        // Ação do Menu Lateral
=======
        // 2. Adicionamos a ação aqui: Quando clicar, executa o comando que veio da TelaPrincipal
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
        btnMenu.setOnAction(e -> {
            if (acaoMenu != null) acaoMenu.run();
        });

        // Logo
        ImageView logoView = new ImageView();
        try {
            logoView.setImage(new Image(Cabecalho.class.getResourceAsStream("/header1-logo.png")));
<<<<<<< HEAD
            logoView.setFitHeight(55);
            logoView.setPreserveRatio(true);
        } catch (Exception e) { /* Ignora se não achar */ }

        // Clique na logo volta para Home
=======
            logoView.setFitHeight(55); // Altura ajustada
            logoView.setPreserveRatio(true);
        } catch (Exception e) { /* Ignora se não achar */ }
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
        logoView.setOnMouseClicked(e -> new TelaPrincipal().start(stage));

        HBox boxEsquerda = new HBox(15, btnMenu, logoView);
        boxEsquerda.setAlignment(Pos.CENTER_LEFT);

        // --- 2. CENTRO (Barra de Busca) ---

        TextField txtBusca = (txtBuscaExterno != null) ? txtBuscaExterno : new TextField();
        txtBusca.setPromptText("Busque 'celular'...");
        txtBusca.getStyleClass().add("campo-busca");
<<<<<<< HEAD
=======
        // Faz o campo de texto crescer para ocupar o espaço da barra
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
        HBox.setHgrow(txtBusca, Priority.ALWAYS);

        Button btnLupa = new Button("🔍");
        btnLupa.getStyleClass().add("btn-lupa");
        btnLupa.setOnAction(e -> txtBusca.fireEvent(new javafx.event.ActionEvent()));

        HBox boxBusca = new HBox(txtBusca, btnLupa);
        boxBusca.getStyleClass().add("barra-busca-container");
        boxBusca.setAlignment(Pos.CENTER_LEFT);
<<<<<<< HEAD
        boxBusca.setMaxHeight(40);

        // --- 3. DIREITA (Links e Ícones) ---

        Button btnMeusAnuncios = new Button("⊞ Meus Anúncios");
=======
        boxBusca.setMaxHeight(40); // Altura fixa da barra

        // --- 3. DIREITA (Links e Ícones) ---

        Button btnMeusAnuncios = new Button("⊞ Meus Anúncios"); // Usei caractere unicode para o grid
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
        btnMeusAnuncios.getStyleClass().add("btn-texto-nav");
        btnMeusAnuncios.setOnAction(e -> new TelaMeusAnuncios().start(stage));

        Button btnVender = new Button("Vender");
        btnVender.getStyleClass().add("btn-vender-header");
        btnVender.setOnAction(e -> new TelaVender().start(stage));

        // --- ÍCONES DE IMAGEM ---
<<<<<<< HEAD
        Button btnCarrinho = criarBotaoImagem("/icon-carrinho.png", 24);
        Button btnSino = criarBotaoImagem("/icon-sino.png", 24);

        // Ícone de Perfil
        Button btnPerfil = criarBotaoImagem("/icon-perfil.png", 28);

        // --- [ALTERAÇÃO AQUI] ---
        // Agora leva para a tela de Perfil estilo "Meu Painel"
        btnPerfil.setOnAction(e -> new TelaPerfil().start(stage));
=======
        // Ajuste o tamanho (24) conforme a qualidade da sua imagem
        Button btnCarrinho = criarBotaoImagem("/icon-carrinho.png", 24);
        Button btnSino = criarBotaoImagem("/icon-sino.png", 24);
        Button btnPerfil = criarBotaoImagem("/icon-perfil.png", 28); // Perfil um pouco maior

        // Ação do Perfil (Logout)
        btnPerfil.setOnAction(e -> {
            Sessao.limpar();
            try { new TelaLogin().start(stage); } catch (Exception ex) {}
        });
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d

        HBox boxDireita = new HBox(20, btnMeusAnuncios, btnVender, btnCarrinho, btnSino, btnPerfil);
        boxDireita.setAlignment(Pos.CENTER_RIGHT);

        // --- 4. MONTAGEM FINAL ---

        Region spacerEsq = new Region();
        HBox.setHgrow(spacerEsq, Priority.ALWAYS);

        Region spacerDir = new Region();
        HBox.setHgrow(spacerDir, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.getStyleClass().add("header-container");
        header.setAlignment(Pos.CENTER);

        try {
            header.getStylesheets().add(Cabecalho.class.getResource("/styles-header.css").toExternalForm());
        } catch (Exception e) {}

        header.getChildren().addAll(boxEsquerda, spacerEsq, boxBusca, spacerDir, boxDireita);

        return header;
    }

    /**
<<<<<<< HEAD
     * Método auxiliar para criar botões com ícones de imagem.
=======
     * Método auxiliar para criar botões com ícones de imagem de forma limpa.
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
     */
    private static Button criarBotaoImagem(String caminhoRecurso, int altura) {
        Button btn = new Button();
        btn.getStyleClass().add("btn-imagem-icone");
        try {
            ImageView img = new ImageView(new Image(Cabecalho.class.getResourceAsStream(caminhoRecurso)));
            img.setFitHeight(altura);
            img.setPreserveRatio(true);
            btn.setGraphic(img);
        } catch (Exception e) {
<<<<<<< HEAD
=======
            // Se a imagem falhar, coloca um texto de fallback
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
            btn.setText("?");
            btn.setStyle("-fx-text-fill: white;");
        }
        return btn;
    }
}