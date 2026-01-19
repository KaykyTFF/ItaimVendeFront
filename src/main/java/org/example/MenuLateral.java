package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuLateral {

    public VBox criar(Stage stage, Runnable acaoFechar) {
        VBox menu = new VBox();
        menu.setPrefWidth(280);

        // --- AQUI ESTÁ A MÁGICA ---
        // 1. Fundo: #253238
        // 2. Borda Direita (A listra verde): 4 pixels de largura, solida, cor #4c984f
        menu.setStyle("""
            -fx-background-color: #253238;
            -fx-padding: 0;
            -fx-border-color: transparent #4c984f transparent transparent;
            -fx-border-width: 0 4 0 0;
        """);

        // === TOPO DO MENU ===
        StackPane headerStack = new StackPane();
        headerStack.setPadding(new Insets(30, 10, 30, 10));
        headerStack.setStyle("-fx-background-color: #253238;");

        ImageView logoView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/header1-logo.png"));
            logoView.setImage(image);
            logoView.setFitWidth(220);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            logoView.setFitWidth(0);
        }
        StackPane.setAlignment(logoView, Pos.CENTER);

        // Botão Fechar (X)
        Label btnFechar = new Label("×");
        btnFechar.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 32px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 5px 0 0;");
        btnFechar.setOnMouseEntered(e -> btnFechar.setStyle(btnFechar.getStyle().replace("#bdc3c7", "white")));
        btnFechar.setOnMouseExited(e -> btnFechar.setStyle(btnFechar.getStyle().replace("white", "#bdc3c7")));
        btnFechar.setOnMouseClicked(e -> acaoFechar.run());
        StackPane.setAlignment(btnFechar, Pos.TOP_RIGHT);

        headerStack.getChildren().addAll(logoView, btnFechar);

        // === ITENS DO MENU ===
        VBox itensMenu = new VBox(5);
        itensMenu.setPadding(new Insets(20));

        itensMenu.getChildren().addAll(
                criarItemMenu("Favoritos", ">"),
                criarItemMenu("Conversas", ">"),
                criarItemMenu("Categorias", ">")
        );

        VBox espacador = new VBox();
        VBox.setVgrow(espacador, Priority.ALWAYS);

        // === RODAPÉ ===
        VBox rodapeMenu = new VBox(5);
        rodapeMenu.setPadding(new Insets(20));
        rodapeMenu.getChildren().addAll(
                new Separator(),
                criarItemMenu("Configurações", ">"),
                criarItemMenu("Sair", ">")
        );

        menu.getChildren().addAll(headerStack, itensMenu, espacador, rodapeMenu);

        return menu;
    }

    private HBox criarItemMenu(String texto, String iconeDireita) {
        Label lblTexto = new Label(texto);
        lblTexto.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblIcone = new Label(iconeDireita);
        lblIcone.setStyle("-fx-text-fill: #90a4ae; -fx-font-size: 16px;");

        HBox item = new HBox(lblTexto, lblIcone);
        HBox.setHgrow(lblTexto, Priority.ALWAYS);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-radius: 5; -fx-cursor: hand;");

        // Hover Verde (#4c984f)
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #4c984f; -fx-background-radius: 5; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

        return item;
    }
}