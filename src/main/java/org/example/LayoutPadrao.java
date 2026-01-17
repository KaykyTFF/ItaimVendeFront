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

        // 1. Prepara o Menu Lateral (Escondido na esquerda)
        menuLateral = new MenuLateral().criar(stage, this::alternarMenu);
        menuLateral.setTranslateX(-280);

        // 2. Barra de Busca Genérica
        TextField txtBusca = new TextField();
        // Se der Enter na busca em telas secundárias, volta pra Home pesquisando
        txtBusca.setOnAction(e -> {
            try { new TelaPrincipal().start(stage); } catch (Exception ex) {}
        });

        // 3. Cria o Cabeçalho passando a nossa ação de alternar menu
        HBox topo = Cabecalho.criar(stage, txtBusca, this::alternarMenu);

        // 4. Monta o Conteúdo (Topo + O que veio da tela específica)
        VBox layoutConteudo = new VBox(topo, conteudoEspecifico);
        VBox.setVgrow(conteudoEspecifico, Priority.ALWAYS); // O conteúdo estica
        layoutConteudo.setStyle("-fx-background-color: #f0f2f5;");

        // 5. StackPane para o Menu flutuar por cima
        StackPane root = new StackPane(layoutConteudo, menuLateral);
        root.setAlignment(Pos.CENTER_LEFT);

        // --- AQUI ESTÁ A MUDANÇA ---
        // Agora usamos as variáveis globais do AppConfig para definir o tamanho
        return new Scene(root, AppConfig.LARGURA_INICIAL, AppConfig.ALTURA_INICIAL);
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