package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.model.UsuarioDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelaLogin {

    private static final String API_URL = "http://localhost:8089/auth/login";

    public void start(Stage stage) {
        // --- PARTE 1: TOPO COM A IMAGEM DO LOGO ---
        ImageView logoView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/header-logo.png"));
            logoView.setImage(image);
            logoView.setFitHeight(290);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            logoView.setFitHeight(0);
        }

        HBox topo = new HBox(logoView);
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(20, 0, 20, 0));
        topo.setStyle("-fx-background-color: white;");

        // --- PARTE 2: FORMULÁRIO (ESCURO) ---
        Label lblTitulo = new Label("LOGIN");
        lblTitulo.getStyleClass().add("titulo-login");

        // Campo Email
        Label lblEmail = new Label("E-mail:");
        lblEmail.getStyleClass().add("label-campo");
        TextField txtEmail = new TextField();
        txtEmail.getStyleClass().add("campo-texto");
        VBox boxEmail = new VBox(5, lblEmail, txtEmail);
        boxEmail.setMaxWidth(320);

        // Campo Senha
        Label lblSenha = new Label("Senha:");
        lblSenha.getStyleClass().add("label-campo");
        PasswordField txtSenha = new PasswordField();
        txtSenha.getStyleClass().add("campo-texto");
        VBox boxSenha = new VBox(5, lblSenha, txtSenha);
        boxSenha.setMaxWidth(320);

        // Link Esqueceu a senha
        Hyperlink linkEsqueceu = new Hyperlink("Esqueceu a senha?");
        linkEsqueceu.getStyleClass().add("link-esqueceu");
        linkEsqueceu.setOnAction(e -> new TelaRecuperarSenha().start(stage));
        HBox boxLink = new HBox(linkEsqueceu);
        boxLink.setAlignment(Pos.CENTER_RIGHT);
        boxLink.setMaxWidth(320);

        // Botões
        Button btnEntrar = new Button("ENTRAR");
        btnEntrar.getStyleClass().add("btn-entrar");
        btnEntrar.setPrefWidth(250);
        btnEntrar.setPrefHeight(45);

        Label lblOu = new Label("ou");
        lblOu.getStyleClass().add("label-ou");

        Button btnCadastrar = new Button("CADASTRE-SE");
        btnCadastrar.getStyleClass().add("btn-cadastrar");
        btnCadastrar.setPrefWidth(250);
        btnCadastrar.setPrefHeight(45);

        Label lblStatus = new Label("");
        lblStatus.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        // --- MUDANÇA PRINCIPAL AQUI ---
        // Aumentei o espaçamento entre os elementos de 10 para 15 para ficar menos apertado
        VBox painelInferior = new VBox(15);
        painelInferior.getStyleClass().add("painel-inferior");
        painelInferior.setAlignment(Pos.TOP_CENTER);

        // AQUI ESTÁ O SEGREDO PARA DESCER:
        // Mudei o primeiro valor (Topo) de 40 para 120.
        // Isso cria um espaço vazio grande no topo do painel escuro, empurrando tudo para baixo.
        painelInferior.setPadding(new Insets(120, 20, 20, 20));

        painelInferior.getChildren().addAll(
                lblTitulo,
                boxEmail,
                boxSenha,
                boxLink,
                btnEntrar,
                lblOu,
                btnCadastrar,
                lblStatus
        );

        VBox.setVgrow(painelInferior, Priority.ALWAYS);

        // --- AÇÕES ---
        btnEntrar.setOnAction(e -> fazerLogin(txtEmail.getText(), txtSenha.getText(), lblStatus, stage));
        btnCadastrar.setOnAction(e -> new TelaCadastro().start(stage));
        txtEmail.setOnAction(e -> txtSenha.requestFocus());
        txtSenha.setOnAction(e -> btnEntrar.fire());

        // --- LAYOUT FINAL ---
        VBox layoutPrincipal = new VBox(topo, painelInferior);
        layoutPrincipal.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(layoutPrincipal);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles-login.css").toExternalForm());
        } catch (Exception e) {}

        // Navegação (Modo Pequeno/Travado)
        Navegacao.configurarJanela(stage, scene, "Login - ItaimVende", false);
        Platform.runLater(painelInferior::requestFocus);
    }

    private void fazerLogin(String email, String senha, Label lblStatus, Stage stage) {
        lblStatus.setText("Verificando...");
        UsuarioDTO loginData = new UsuarioDTO();
        loginData.setEmail(email);
        loginData.setSenha(senha);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(loginData);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                UsuarioDTO user = mapper.readValue(response.body(), UsuarioDTO.class);
                                Sessao.setUsuario(user);
                                new TelaPrincipal().start(stage);
                            } catch (Exception e) {}
                        } else {
                            lblStatus.setText("Dados incorretos.");
                        }
                    }));
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}