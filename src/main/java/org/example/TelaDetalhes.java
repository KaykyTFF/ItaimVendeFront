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
import javafx.stage.Stage;
import org.example.model.Anuncio;
import org.example.model.ImagemAnuncio;

import java.io.ByteArrayInputStream;
import java.util.Base64;

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
    }
}