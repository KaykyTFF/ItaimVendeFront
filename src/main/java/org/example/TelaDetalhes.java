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
<<<<<<< HEAD
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
=======
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
import javafx.stage.Stage;
import org.example.model.Anuncio;
import org.example.model.ImagemAnuncio;

import java.io.ByteArrayInputStream;
import java.util.Base64;
<<<<<<< HEAD
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
=======

/**
 * Controller responsável pela visualização detalhada de um anúncio específico.
 */
public class TelaDetalhes {

    public void start(Stage stage, Anuncio anuncio) {
        // --- 1. ESTRUTURA E NAVEGAÇÃO ---

        // NOTA: Removemos a criação manual do Cabecalho aqui.
        // O LayoutPadrao vai cuidar disso e do Menu Lateral.

        Button btnVoltar = new Button("⬅ Voltar para a lista");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: #1877F2; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Retorna para o feed principal ao clicar
        btnVoltar.setOnAction(e -> new TelaPrincipal().start(stage));

        HBox boxVoltar = new HBox(btnVoltar);
        boxVoltar.setPadding(new Insets(10, 0, 10, 0));

        // --- 2. GALERIA DE MÍDIA ---

        HBox galeriaContainer = new HBox(15);
        galeriaContainer.setPadding(new Insets(10));
        galeriaContainer.setAlignment(Pos.CENTER_LEFT);

        if (anuncio.getImagens() == null || anuncio.getImagens().isEmpty()) {
            StackPane placeholder = new StackPane(new Label("Sem fotos"));
            placeholder.setPrefSize(400, 300);
            placeholder.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 10;");
            galeriaContainer.getChildren().add(placeholder);
        } else {
            for (ImagemAnuncio imgObj : anuncio.getImagens()) {
                if (imgObj.getFotoBase64() != null) {
                    try {
                        byte[] imgBytes = Base64.getDecoder().decode(imgObj.getFotoBase64());
                        Image img = new Image(new ByteArrayInputStream(imgBytes));

                        ImageView imgView = new ImageView(img);
                        imgView.setFitHeight(300);
                        imgView.setPreserveRatio(true);

                        StackPane frame = new StackPane(imgView);
                        frame.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 0);");
                        frame.setPadding(new Insets(5));

                        galeriaContainer.getChildren().add(frame);
                    } catch (Exception e) {}
                }
            }
        }

        ScrollPane scrollGaleria = new ScrollPane(galeriaContainer);
        scrollGaleria.setFitToHeight(true);
        scrollGaleria.setPannable(true);
        scrollGaleria.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollGaleria.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollGaleria.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // --- 3. INFORMAÇÕES DO ANÚNCIO ---

        Label lblTitulo = new Label(anuncio.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1c1e21;");
        lblTitulo.setWrapText(true);

        Label lblPreco = new Label("R$ " + anuncio.getPreco());
        lblPreco.setStyle("-fx-font-size: 24px; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        Label lblTag = new Label(anuncio.getTipo() + " > " + anuncio.getCategoria());
        lblTag.setStyle("-fx-background-color: #e4e6eb; -fx-text-fill: #555; -fx-padding: 5 10; -fx-background-radius: 15;");

        String localTexto = (anuncio.getBairro() != null ? anuncio.getBairro() + ", " : "") +
                (anuncio.getCidade() != null ? anuncio.getCidade() : "Local não informado");
        Label lblLocal = new Label("📍 " + localTexto);
        lblLocal.setStyle("-fx-font-size: 14px; -fx-text-fill: #65676b;");

        Label lblDescTitulo = new Label("Descrição");
        lblDescTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        Label lblDescricao = new Label(anuncio.getDescricao());
        lblDescricao.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        lblDescricao.setWrapText(true);
        lblDescricao.setMaxWidth(700);

        // Card de Contato
        VBox boxContato = new VBox(5);
        boxContato.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #c8e6c9; -fx-border-radius: 10;");
        Label lblContatoTitulo = new Label("Interessou? Entre em contato:");
        lblContatoTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1b5e20;");
        Label lblZap = new Label("📞 " + anuncio.getContato());
        lblZap.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1b5e20;");

        boxContato.getChildren().addAll(lblContatoTitulo, lblZap);
        boxContato.setMaxWidth(400);

        // --- 4. MONTAGEM DO CONTEÚDO PRINCIPAL ---

        VBox conteudoInterno = new VBox(15);
        conteudoInterno.setPadding(new Insets(30));
        conteudoInterno.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-background-radius: 15;");
        conteudoInterno.setMaxWidth(900);

        conteudoInterno.getChildren().addAll(
                boxVoltar,
                lblTitulo,
                lblPreco,
                lblTag,
                scrollGaleria,
                boxContato,
                lblLocal,
                new Separator(),
                lblDescTitulo,
                lblDescricao
        );

        // Scroll apenas para o conteúdo central
        ScrollPane scrollGeral = new ScrollPane();
        StackPane fundoCentralizado = new StackPane(conteudoInterno);
        fundoCentralizado.setPadding(new Insets(30));
        fundoCentralizado.setStyle("-fx-background-color: #f0f2f5;"); // Fundo cinza

        scrollGeral.setContent(fundoCentralizado);
        scrollGeral.setFitToWidth(true);
        scrollGeral.setStyle("-fx-background-color: #f0f2f5;");

        // --- 5. APLICAÇÃO DO LAYOUT PADRÃO (MENU LATERAL) ---
        // Aqui usamos a classe LayoutPadrao para envolver nosso scrollGeral
        // com o Cabeçalho e o Menu Lateral automaticamente.
        Scene scene = new LayoutPadrao().criarCena(stage, scrollGeral);

        Navegacao.configurarJanela(stage, scene, "Detalhes - " + anuncio.getTitulo(), true);
>>>>>>> 60bc3d42dce0ffcc32571410f2cbfface5535d0d
    }
}