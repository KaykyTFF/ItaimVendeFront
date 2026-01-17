package org.example;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navegacao {

    public static void configurarJanela(Stage stage, Scene scene, String titulo, boolean maximizadoPadrao) {
        stage.setTitle(titulo + " - " + AppConfig.NOME_APP);

        // --- 1. CAPTURAR O ESTADO ATUAL (ANTES DE TROCAR) ---
        boolean estavaVisivel = stage.isShowing();
        boolean estavaMaximizado = stage.isMaximized();
        double larguraAtual = stage.getWidth();
        double alturaAtual = stage.getHeight();

        // --- 2. TROCAR A CENA ---
        stage.setScene(scene);

        // --- 3. TRAVAS DE TAMANHO MÍNIMO (Sempre aplica) ---
        stage.setMinWidth(AppConfig.LARGURA_MINIMA);
        stage.setMinHeight(AppConfig.ALTURA_MINIMA);

        // --- 4. RESTAURAR OU DEFINIR TAMANHO ---
        if (estavaVisivel) {
            // Se a janela já estava aberta, respeita o tamanho que o usuário deixou
            stage.setWidth(larguraAtual);
            stage.setHeight(alturaAtual);
            stage.setMaximized(estavaMaximizado);
        } else {
            // Se é a primeira tela (Login), usa o padrão do AppConfig
            stage.setWidth(AppConfig.LARGURA_INICIAL);
            stage.setHeight(AppConfig.ALTURA_INICIAL);

            if (maximizadoPadrao) {
                stage.setMaximized(true);
            } else {
                stage.centerOnScreen();
            }
        }

        stage.show();
    }
}