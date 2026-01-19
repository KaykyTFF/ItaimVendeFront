package org.example;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LayoutPadrao {

    private VBox menuLateral;
    private boolean menuAberto = false;

    /**
     * Pega o conteúdo de qualquer tela e envolve com o Menu Lateral e Cabeçalho.
     */
    public Scene criarCena(Stage stage, Region conteudoEspecifico) {

        // 1. Prepara o Menu Lateral
        // Passamos 'this::alternarMenu' para que o botão 'X' funcione
        menuLateral = new MenuLateral().criar(stage, this::alternarMenu);

        // --- CORREÇÃO DO BUG DE SOBREPOSIÇÃO ---
        // Definimos largura FIXA. Sem isso, o StackPane estica o menu para a tela toda.
        menuLateral.setMinWidth(280);
        menuLateral.setMaxWidth(280);

        // Começa escondido na esquerda
        menuLateral.setTranslateX(-280);

        // 2. Barra de Busca Genérica (se usada em outras telas)
        TextField txtBusca = new TextField();
        txtBusca.setOnAction(e -> {
            try { new TelaPrincipal().start(stage); } catch (Exception ex) {}
        });

        // 3. Cria o Cabeçalho
        HBox topo = Cabecalho.criar(stage, txtBusca, this::alternarMenu);

        // 4. Monta o Conteúdo (Topo + O que veio da tela específica)
        VBox layoutConteudo = new VBox(topo, conteudoEspecifico);
        VBox.setVgrow(conteudoEspecifico, Priority.ALWAYS); // O conteúdo estica
        layoutConteudo.setStyle("-fx-background-color: #f0f2f5;");

        // 5. O Grande Segredo: StackPane para o Menu flutuar por cima
        StackPane root = new StackPane();
        root.setAlignment(Pos.CENTER_LEFT); // Alinha tudo à esquerda por padrão

        // A ordem importa: Primeiro o conteúdo (fundo), depois o menu (frente)
        root.getChildren().addAll(layoutConteudo, menuLateral);

        // Garante explicitamente que o menu fique na esquerda
        StackPane.setAlignment(menuLateral, Pos.CENTER_LEFT);

        // 6. Retorna a cena com o tamanho padrão do AppConfig
        Scene scene = new Scene(root, AppConfig.LARGURA_INICIAL, AppConfig.ALTURA_INICIAL);

        // Carrega o CSS global se existir
        try {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {}

        return scene;
    }

    // A animação de deslizar
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
}