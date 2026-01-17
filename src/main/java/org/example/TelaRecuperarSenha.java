package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class TelaRecuperarSenha {

    private static final String API_URL = "http://localhost:8089/auth";
    private static final int LARGURA_CAMPO = 500;

    private VBox conteudoDinamico;
    private Label lblStatus;

    public void start(Stage stage) {
        // --- 1. TOPO ---
        ImageView logoView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/header-logo.png"));
            logoView.setImage(image);
            logoView.setFitHeight(290);
            logoView.setPreserveRatio(true);
        } catch (Exception e) { logoView.setFitHeight(0); }

        HBox topo = new HBox(logoView);
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(10, 0, 10, 0));
        topo.setStyle("-fx-background-color: white;");

        // --- 2. ESTRUTURA DO PAINEL ---
        conteudoDinamico = new VBox(20);
        conteudoDinamico.setAlignment(Pos.TOP_CENTER);

        lblStatus = new Label("");
        lblStatus.getStyleClass().add("label-erro");
        lblStatus.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox painelInferior = new VBox(20);
        painelInferior.getStyleClass().add("painel-inferior");
        painelInferior.setAlignment(Pos.TOP_CENTER);
        painelInferior.setPadding(new Insets(100, 20, 20, 20));

        painelInferior.getChildren().addAll(conteudoDinamico, lblStatus);
        VBox.setVgrow(painelInferior, Priority.ALWAYS);

        VBox layoutPrincipal = new VBox(topo, painelInferior);
        layoutPrincipal.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(layoutPrincipal);
        try { scene.getStylesheets().add(getClass().getResource("/styles-login.css").toExternalForm()); } catch (Exception e) {}

        // Inicia na Fase 1
        montarEtapaEmail(stage);

        Navegacao.configurarJanela(stage, scene, "Recuperar Senha - ItaimVende", false);
    }

    // --- FASE 1: PEDIR O E-MAIL ---
    private void montarEtapaEmail(Stage stage) {
        conteudoDinamico.getChildren().clear();
        lblStatus.setText("");

        Label lblTitulo = new Label("RECUPERAR SENHA");
        lblTitulo.getStyleClass().add("titulo-login");

        Label lblInstrucao = new Label("Digite seu e-mail para receber o código de verificação.");
        lblInstrucao.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Seu e-mail cadastrado");
        VBox boxEmail = criarGrupoInput("E-mail:", txtEmail);

        Button btnEnviar = new Button("ENVIAR CÓDIGO");
        btnEnviar.getStyleClass().add("btn-entrar");
        btnEnviar.setPrefWidth(LARGURA_CAMPO);
        btnEnviar.setPrefHeight(50);

        Hyperlink linkVoltar = new Hyperlink("Voltar para o Login");
        linkVoltar.getStyleClass().add("link-voltar-branco");

        btnEnviar.setOnAction(e -> enviarSolicitacao(txtEmail.getText()));
        linkVoltar.setOnAction(e -> { try { new TelaLogin().start(stage); } catch (Exception ex) {} });
        txtEmail.setOnAction(e -> btnEnviar.fire());

        conteudoDinamico.getChildren().addAll(lblTitulo, lblInstrucao, boxEmail, btnEnviar, linkVoltar);
    }

    // --- FASE 2: PEDIR CÓDIGO E NOVA SENHA ---
    private void montarEtapaCodigo(Stage stage, String emailConfirmado) {
        conteudoDinamico.getChildren().clear();
        lblStatus.setText("");

        Label lblTitulo = new Label("DEFINIR NOVA SENHA");
        lblTitulo.getStyleClass().add("titulo-login");

        Label lblInstrucao = new Label("Enviamos um código para: " + emailConfirmado);
        lblInstrucao.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

        TextField txtToken = new TextField();
        txtToken.setPromptText("Ex: A1B2C3");
        txtToken.textProperty().addListener((ov, oldV, newV) -> txtToken.setText(newV.toUpperCase()));
        VBox boxToken = criarGrupoInput("Código Recebido:", txtToken);

        PasswordField txtSenha = new PasswordField();
        txtSenha.setPromptText("Nova senha segura");
        VBox boxSenha = criarGrupoInput("Nova Senha:", txtSenha);

        Button btnAlterar = new Button("ALTERAR SENHA");
        btnAlterar.getStyleClass().add("btn-entrar");
        btnAlterar.setPrefWidth(LARGURA_CAMPO);
        btnAlterar.setPrefHeight(50);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-cadastrar");
        btnCancelar.setPrefWidth(LARGURA_CAMPO);
        btnCancelar.setPrefHeight(40);

        btnAlterar.setOnAction(e -> confirmarTroca(stage, emailConfirmado, txtToken.getText(), txtSenha.getText()));
        btnCancelar.setOnAction(e -> { try { new TelaLogin().start(stage); } catch (Exception ex) {} });

        conteudoDinamico.getChildren().addAll(lblTitulo, lblInstrucao, boxToken, boxSenha, btnAlterar, btnCancelar);
        Platform.runLater(txtToken::requestFocus);
    }

    // --- FASE 3: SUCESSO (NOVA TELA SEM ALERT) ---
    private void montarEtapaSucesso(Stage stage) {
        conteudoDinamico.getChildren().clear();
        lblStatus.setText(""); // Limpa erros antigos

        // Título Verde
        Label lblTitulo = new Label("SENHA ALTERADA!");
        lblTitulo.getStyleClass().add("titulo-login");
        lblTitulo.setStyle("-fx-text-fill: #44BD32; -fx-font-size: 24px;");

        // Mensagem
        Label lblInstrucao = new Label("Sua senha foi redefinida com sucesso.\nAgora você pode acessar sua conta.");
        lblInstrucao.setStyle("-fx-text-fill: white; -fx-text-alignment: center; -fx-font-size: 16px;");
        lblInstrucao.setWrapText(true);

        // Botão Login
        Button btnIrLogin = new Button("FAZER LOGIN");
        btnIrLogin.getStyleClass().add("btn-entrar");
        btnIrLogin.setPrefWidth(LARGURA_CAMPO);
        btnIrLogin.setPrefHeight(50);

        btnIrLogin.setOnAction(e -> {
            try { new TelaLogin().start(stage); } catch (Exception ex) {}
        });

        conteudoDinamico.getChildren().addAll(lblTitulo, lblInstrucao, btnIrLogin);
    }

    // --- API 1: Enviar Email ---
    private void enviarSolicitacao(String email) {
        if (!email.contains("@")) {
            lblStatus.setText("Por favor, digite um e-mail válido.");
            return;
        }
        lblStatus.setText("Enviando solicitação...");
        lblStatus.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("email", email);
            String json = new ObjectMapper().writeValueAsString(dados);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/esqueceu-senha"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.statusCode() == 200) {
                            Stage stage = (Stage) conteudoDinamico.getScene().getWindow();
                            montarEtapaCodigo(stage, email);
                        } else {
                            lblStatus.setStyle("-fx-text-fill: #ff6b6b;");
                            lblStatus.setText("Erro: " + res.body());
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> lblStatus.setText("Erro de conexão."));
                        return null;
                    });
        } catch (Exception e) { lblStatus.setText("Erro interno."); }
    }

    // --- API 2: Confirmar Troca ---
    private void confirmarTroca(Stage stage, String email, String token, String novaSenha) {
        if (token.isEmpty() || novaSenha.isEmpty()) {
            lblStatus.setText("Preencha todos os campos.");
            return;
        }
        lblStatus.setText("Validando...");
        lblStatus.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("email", email);
            dados.put("token", token);
            dados.put("novaSenha", novaSenha);
            String json = new ObjectMapper().writeValueAsString(dados);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/redefinir-senha"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.statusCode() == 200) {
                            // --- SUBSTITUI O ALERT PELA TELA DE SUCESSO ---
                            montarEtapaSucesso(stage);
                        } else {
                            lblStatus.setStyle("-fx-text-fill: #ff6b6b;");
                            lblStatus.setText("Erro: " + res.body());
                        }
                    }));
        } catch (Exception e) { lblStatus.setText("Erro interno."); }
    }

    private VBox criarGrupoInput(String textoLabel, Control input) {
        Label lbl = new Label(textoLabel);
        lbl.getStyleClass().add("label-campo");
        input.getStyleClass().add("campo-texto");
        if (input instanceof TextField) ((TextField) input).setPrefHeight(45);
        VBox box = new VBox(5, lbl, input);
        box.setMaxWidth(LARGURA_CAMPO);
        return box;
    }
}