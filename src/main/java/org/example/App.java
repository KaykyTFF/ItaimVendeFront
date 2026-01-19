package org.example;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe principal (Main Entry Point) da aplicação ItaimVende Desktop.
 * <p>
 * Responsável apenas por iniciar o ciclo de vida do JavaFX e
 * delegar a interface inicial para a {@link TelaLogin}.
 *
 * @author Kayky Terles
 * @version 1.0
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Simplesmente delega para a tela de login
        new TelaLogin().start(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}