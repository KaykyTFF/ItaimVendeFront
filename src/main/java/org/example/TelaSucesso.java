package org.example;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.model.Anuncio;

public class TelaSucesso {

    private final Anuncio anuncioCriado;

    public TelaSucesso(Anuncio anuncioCriado) {
        this.anuncioCriado = anuncioCriado;
    }

    public void start(Stage stage) {
        // --- 1. ÍCONE DE SUCESSO (Igual a imagem) ---
        Circle circuloFundo = new Circle(60);
        circuloFundo.setStyle("-fx-fill: #59AA59;"); // Verde do seu tema

        Text checkMark = new Text("✔");
        checkMark.setStyle("-fx-fill: white; -fx-font-size: 60px; -fx-font-weight: bold;");

        StackPane icone = new StackPane(circuloFundo, checkMark);

        // --- 2. TEXTOS ---
        Label lblTitulo = new Label("Anuncio criado com Sucesso!");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: black;");

        Button btnVerAnuncio = new Button("clique aqui para ver anuncio");
        btnVerAnuncio.setStyle("-fx-background-color: transparent; -fx-text-fill: #59AA59; -fx-font-size: 16px; -fx-cursor: hand; -fx-font-weight: bold;");

        // Ação: Ao clicar, pode ir para a home ou para o detalhe (futuro)
        btnVerAnuncio.setOnAction(e -> {
            // Aqui você direcionaria para TelaDetalhe(anuncioCriado)
            // Por enquanto, vamos para a Principal para não quebrar
            new TelaPrincipal().start(stage);
        });

        // --- 3. LAYOUT ---
        VBox layout = new VBox(20, icone, lblTitulo, btnVerAnuncio);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #E0E0E0;"); // Fundo cinza claro

        // Usa o LayoutPadrao para manter o cabeçalho
        Scene scene = new LayoutPadrao().criarCena(stage, layout);
        stage.setScene(scene);
        stage.setTitle("Sucesso");
    }
}