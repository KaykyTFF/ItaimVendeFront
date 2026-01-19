package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.example.model.Anuncio;
import org.example.model.ImagemAnuncio;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

public class TelaDetalhes {

    private ImageView imgPrincipalView;

    public void start(Stage stage, Anuncio anuncio) {
        // --- 1. BOTÃO VOLTAR (Círculo Verde no Topo) ---
        Button btnVoltar = new Button("←");
        btnVoltar.getStyleClass().add("btn-voltar-circulo");
        btnVoltar.setOnAction(e -> new TelaPrincipal().start(stage));

        HBox topBarContainer = new HBox(btnVoltar);
        // Margem ajustada para alinhar com o conteúdo abaixo
        topBarContainer.setPadding(new Insets(20, 0, 10, 80));

        // --- 2. COLUNA ESQUERDA (FOTOS) ---
        VBox colFotos = criarGaleriaFotos(anuncio);

        // --- 3. COLUNA CENTRAL (DESCRIÇÃO) ---
        VBox colInfo = criarInfoCentro(anuncio);
        HBox.setHgrow(colInfo, Priority.ALWAYS); // Ocupa o espaço que sobrar

        // --- 4. COLUNA DIREITA (PREÇO E VENDEDOR) ---
        VBox colAcoes = criarCardDireito(anuncio);

        // --- 5. MONTAGEM DO LAYOUT ---
        HBox layoutPrincipal = new HBox(40, colFotos, colInfo, colAcoes);
        layoutPrincipal.setAlignment(Pos.TOP_CENTER);
        layoutPrincipal.setPadding(new Insets(10, 80, 50, 80));

        // Container Geral
        VBox conteudoTotal = new VBox(5, topBarContainer, layoutPrincipal);
        conteudoTotal.getStyleClass().add("fundo-pagina");

        // ScrollPane para telas pequenas
        ScrollPane scroll = new ScrollPane(conteudoTotal);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-pane");

        // Cena
        Scene scene = new LayoutPadrao().criarCena(stage, scroll);

        // Carrega o CSS
        try {
            scene.getStylesheets().add(getClass().getResource("/styles-detalhes.css").toExternalForm());
        } catch (Exception e) { e.printStackTrace(); }

        Navegacao.configurarJanela(stage, scene, anuncio.getTitulo(), true);
    }

    private VBox criarGaleriaFotos(Anuncio anuncio) {
        // Quadro Branco da Foto Principal
        StackPane frameFoto = new StackPane();
        frameFoto.getStyleClass().add("frame-foto-branca");
        frameFoto.setPrefSize(380, 450);

        imgPrincipalView = new ImageView();
        imgPrincipalView.setFitWidth(300);
        imgPrincipalView.setFitHeight(400);
        imgPrincipalView.setPreserveRatio(true);

        // Ícone de Coração (Favorito)
        Label iconHeart = new Label("❤");
        iconHeart.getStyleClass().add("icone-coracao");
        StackPane.setAlignment(iconHeart, Pos.TOP_RIGHT);
        StackPane.setMargin(iconHeart, new Insets(15));

        // Carrega a primeira foto se existir
        if (anuncio.getImagens() != null && !anuncio.getImagens().isEmpty()) {
            carregarImagem(imgPrincipalView, anuncio.getImagens().get(0).getFotoBase64());
        }

        frameFoto.getChildren().addAll(imgPrincipalView, iconHeart);

        // Lista de Miniaturas
        HBox miniaturas = new HBox(10);
        miniaturas.setAlignment(Pos.CENTER_LEFT);
        miniaturas.setPadding(new Insets(10, 0, 0, 0));

        if (anuncio.getImagens() != null) {
            int maxImgs = Math.min(anuncio.getImagens().size(), 5);
            for (int i = 0; i < maxImgs; i++) {
                ImagemAnuncio img = anuncio.getImagens().get(i);

                StackPane thumbBg = new StackPane();
                thumbBg.getStyleClass().add("thumb-foto");

                ImageView imgThumb = new ImageView();
                imgThumb.setFitWidth(50);
                imgThumb.setFitHeight(50);
                imgThumb.setPreserveRatio(true);

                // Recorte arredondado na miniatura
                Rectangle clip = new Rectangle(50, 50);
                clip.setArcWidth(8); clip.setArcHeight(8);
                imgThumb.setClip(clip);

                carregarImagem(imgThumb, img.getFotoBase64());
                thumbBg.getChildren().add(imgThumb);

                // Clique na miniatura troca a foto grande
                thumbBg.setOnMouseClicked(e -> carregarImagem(imgPrincipalView, img.getFotoBase64()));

                miniaturas.getChildren().add(thumbBg);
            }
        }

        return new VBox(frameFoto, miniaturas);
    }

    private VBox criarInfoCentro(Anuncio anuncio) {
        VBox box = new VBox(10);
        box.setMaxWidth(500); // Largura máxima do texto

        // Título
        Label titulo = new Label(anuncio.getTitulo());
        titulo.getStyleClass().add("titulo-principal");
        titulo.setWrapText(true);

        // Localização
        String textoLocal = (anuncio.getCidade() != null ? anuncio.getCidade() : "Local n/d")
                + (anuncio.getEstado() != null ? " - " + anuncio.getEstado() : "");
        Label local = new Label("📍 " + textoLocal);
        local.getStyleClass().add("texto-localizacao");

        Separator sep1 = new Separator();

        // Descrição
        Label lblDescTitulo = new Label("Descrição do produto");
        lblDescTitulo.getStyleClass().add("titulo-secao");

        Label descricao = new Label(anuncio.getDescricao());
        descricao.getStyleClass().add("texto-descricao");
        descricao.setWrapText(true);

        Separator sep2 = new Separator();

        // Informações Extras / Condição
        Label lblInfoTitulo = new Label("Informações do produto");
        lblInfoTitulo.getStyleClass().add("titulo-secao");

        Label lblCondicao = new Label("condição do produto");
        lblCondicao.getStyleClass().add("subtitulo-info");

        // Tag Cinza (Novo/Usado)
        String condicaoTexto = (anuncio.getTipo() != null) ? anuncio.getTipo().toLowerCase() : "usado";
        Label tagCondicao = new Label(condicaoTexto);
        tagCondicao.getStyleClass().add("tag-condicao");

        box.getChildren().addAll(
                titulo, local, sep1,
                lblDescTitulo, descricao,
                new VBox(15, sep2), // Espaço extra
                lblInfoTitulo, lblCondicao, tagCondicao
        );

        return box;
    }

    private VBox criarCardDireito(Anuncio anuncio) {
        VBox containerDireita = new VBox(15); // Espaçamento entre os blocos
        containerDireita.setPrefWidth(300);
        containerDireita.setMaxWidth(300);
        containerDireita.setAlignment(Pos.TOP_CENTER); // Centraliza tudo

        // --- 1. CARD CINZA DO PREÇO (Flutuante) ---
        VBox cardPreco = new VBox(5);
        cardPreco.getStyleClass().add("card-preco-box");
        cardPreco.setAlignment(Pos.CENTER); // Texto centralizado no card

        Label lblPreco = new Label("R$ " + String.format("%.2f", anuncio.getPreco()));
        lblPreco.getStyleClass().add("preco-grande");

        Label lblNegociavel = new Label("Preço negociável com o vendedor");
        lblNegociavel.getStyleClass().add("texto-negociavel");

        cardPreco.getChildren().addAll(lblPreco, lblNegociavel);

        // --- 2. BOTÕES COM ÍCONES ---
        VBox boxBotoes = new VBox(15); // Espaço entre os botões
        boxBotoes.setPadding(new Insets(10, 0, 10, 0));

        // Botão CHAT
        Button btnChat = new Button("CHAT COM O VENDEDOR");
        btnChat.getStyleClass().add("btn-chat");
        btnChat.setMaxWidth(Double.MAX_VALUE); // Esticar
        // Carrega o ícone chat.png
        ImageView iconChat = carregarIcone("/chat.png", 20); // 20px de tamanho
        if (iconChat != null) {
            btnChat.setGraphic(iconChat);
            btnChat.setGraphicTextGap(10); // Espaço entre ícone e texto
        }

        // Botão WHATSAPP
        Button btnWhats = new Button("WHATSAPP");
        btnWhats.getStyleClass().add("btn-whatsapp");
        btnWhats.setMaxWidth(Double.MAX_VALUE); // Esticar
        // Carrega o ícone wpp.png
        ImageView iconWpp = carregarIcone("/wpp.png", 20);
        if (iconWpp != null) {
            btnWhats.setGraphic(iconWpp);
            btnWhats.setGraphicTextGap(10);
        }

        boxBotoes.getChildren().addAll(btnChat, btnWhats);

        // --- 3. SEÇÃO DO VENDEDOR ---
        HBox boxVendedor = new HBox(15);
        boxVendedor.setAlignment(Pos.CENTER_LEFT);
        boxVendedor.setPadding(new Insets(10, 0, 0, 0));

        // Avatar (Círculo Cinza com Ícone Padrão)
        StackPane avatarPane = new StackPane();
        Circle circulo = new Circle(28); // Um pouco maior
        circulo.getStyleClass().add("avatar-circulo");

        // Se quiser usar uma imagem de avatar real, carregue aqui.
        // Por enquanto, usamos um ícone de usuário svg/texto cinza escuro simulando a foto
        Label iconUser = new Label("👤");
        iconUser.setStyle("-fx-text-fill: #808080; -fx-font-size: 28px;");

        avatarPane.getChildren().addAll(circulo, iconUser);

        // Textos
        VBox txtVendedor = new VBox(0);
        Label lblNomeVendedor = new Label("Vendedor Celular");
        lblNomeVendedor.getStyleClass().add("nome-vendedor");

        Label lblVendas = new Label("Realizou 10 vendas");
        lblVendas.getStyleClass().add("info-vendas");

        txtVendedor.getChildren().addAll(lblNomeVendedor, lblVendas);
        boxVendedor.getChildren().addAll(avatarPane, txtVendedor);

        containerDireita.getChildren().addAll(cardPreco, boxBotoes, boxVendedor);
        return containerDireita;
    }

    // Método auxiliar para carregar ícones sem quebrar se o arquivo faltar
    private ImageView carregarIcone(String caminho, double tamanho) {
        try {
            // Tenta carregar do resources
            Image img = new Image(getClass().getResourceAsStream(caminho));
            ImageView view = new ImageView(img);
            view.setFitHeight(tamanho);
            view.setFitWidth(tamanho);
            view.setPreserveRatio(true);
            return view;
        } catch (Exception e) {
            System.out.println("⚠️ Ícone não encontrado: " + caminho);
            return null;
        }
    }

    private void carregarImagem(ImageView view, String base64) {
        try {
            byte[] imgBytes = Base64.getDecoder().decode(base64);
            Image img = new Image(new ByteArrayInputStream(imgBytes));
            view.setImage(img);
        } catch (Exception e) {}
    }
}