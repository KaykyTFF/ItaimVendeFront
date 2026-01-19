package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TelaConfirmacao {

    private static final String API_BASE = "http://localhost:8089/auth";

    // Container que troca o conteúdo (Input Código -> Mensagem Sucesso)
    private VBox conteudoDinamico;
    private Label lblStatus;

    // Variável para controlar o timer e poder parar se sair da tela
    private Timeline timeline;

    public void start(Stage stage, String email) {
        // --- 1. TOPO (Branco com Logo) ---
        ImageView logoView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/header-logo.png"));
            logoView.setImage(image);
            logoView.setFitHeight(150);
            logoView.setPreserveRatio(true);
        } catch (Exception e) { logoView.setFitHeight(0); }

        HBox topo = new HBox(logoView);
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(10, 0, 10, 0));
        topo.setStyle("-fx-background-color: white;");

        // --- 2. ESTRUTURA DO PAINEL ESCURO ---
        conteudoDinamico = new VBox(20);
        conteudoDinamico.setAlignment(Pos.CENTER);

        lblStatus = new Label("");
        lblStatus.getStyleClass().add("label-erro");
        lblStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox painelInferior = new VBox(20);
        painelInferior.getStyleClass().add("painel-inferior"); // Fundo escuro com onda
        painelInferior.setAlignment(Pos.CENTER);
        painelInferior.setPadding(new Insets(40));

        painelInferior.getChildren().addAll(conteudoDinamico, lblStatus);
        VBox.setVgrow(painelInferior, Priority.ALWAYS);

        // --- LAYOUT GLOBAL ---
        VBox layoutPrincipal = new VBox(topo, painelInferior);
        layoutPrincipal.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(layoutPrincipal);
        try { scene.getStylesheets().add(getClass().getResource("/styles-login.css").toExternalForm()); } catch (Exception e) {}

        // Inicia na tela de digitar código
        montarTelaCodigo(stage, email);

        stage.setScene(scene);
    }

    // --- TELA 1: DIGITAR CÓDIGO ---
    private void montarTelaCodigo(Stage stage, String email) {
        conteudoDinamico.getChildren().clear();
        lblStatus.setText("");

        Label lblTitulo = new Label("VERIFICAÇÃO DE CONTA");
        lblTitulo.getStyleClass().add("titulo-login");

        Label lblInstrucao = new Label("Enviamos um código de 6 dígitos para:");
        lblInstrucao.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

        Label lblEmail = new Label(email);
        lblEmail.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 0 0 10 0;");

        // Campo Código
        TextField txtCodigo = new TextField();
        txtCodigo.setPromptText("CÓDIGO (Ex: 123456)");
        txtCodigo.getStyleClass().add("campo-texto");
        txtCodigo.setMaxWidth(400);
        txtCodigo.setAlignment(Pos.CENTER);
        txtCodigo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Botão Confirmar
        Button btnConfirmar = new Button("ATIVAR CONTA");
        btnConfirmar.getStyleClass().add("btn-entrar");
        btnConfirmar.setPrefWidth(400);
        btnConfirmar.setPrefHeight(50);

        // Botão Reenviar (Texto pequeno)
        Button btnReenviar = new Button("Aguarde 60s");
        btnReenviar.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdc3c7; -fx-cursor: hand; -fx-font-size: 12px;");
        btnReenviar.setDisable(true);

        // --- ALTERAÇÃO AQUI: Botão Voltar para Cadastro ---
        Hyperlink linkVoltar = new Hyperlink("E-mail errado? Voltar e corrigir");
        linkVoltar.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-underline: true; -fx-cursor: hand;");

        // Ações
        iniciarContagemRegressiva(btnReenviar);

        btnReenviar.setOnAction(e -> {
            reenviarCodigo(email);
            iniciarContagemRegressiva(btnReenviar);
        });

        btnConfirmar.setOnAction(e -> {
            if (txtCodigo.getText().isEmpty()) {
                lblStatus.setText("Por favor, digite o código.");
                lblStatus.setStyle("-fx-text-fill: #e74c3c;"); // Vermelho
            } else {
                confirmarCodigo(stage, email, txtCodigo.getText());
            }
        });

        // Ação do Botão Voltar -> VAI PARA TELA DE CADASTRO
        linkVoltar.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            try {
                // Chama a tela de cadastro (que vai manter os dados digitados pois são static)
                new TelaCadastro().start(stage);
            } catch (Exception ex) {}
        });

        // Adiciona tudo na tela
        conteudoDinamico.getChildren().addAll(
                lblTitulo,
                lblInstrucao,
                lblEmail,
                txtCodigo,
                btnConfirmar,
                btnReenviar,
                linkVoltar
        );

        Platform.runLater(txtCodigo::requestFocus);
    }

    // --- TELA 2: SUCESSO (CONTA ATIVADA) ---
    private void montarTelaSucesso(Stage stage) {
        conteudoDinamico.getChildren().clear();
        lblStatus.setText("");

        // LIMPA OS DADOS DO CADASTRO (Já que deu certo)
        TelaCadastro.limparDados();

        // Título Verde
        Label lblTitulo = new Label("CONTA ATIVADA!");
        lblTitulo.getStyleClass().add("titulo-login");
        lblTitulo.setStyle("-fx-font-size: 24px; -fx-text-fill: #44BD32;");

        // Mensagem
        Label lblMensagem = new Label("Seu cadastro foi concluído com sucesso.\nAgora você pode acessar o sistema.");
        lblMensagem.setStyle("-fx-text-fill: white; -fx-text-alignment: center; -fx-font-size: 16px;");
        lblMensagem.setWrapText(true);

        // Botão Login
        Button btnIrLogin = new Button("FAZER LOGIN");
        btnIrLogin.getStyleClass().add("btn-entrar");
        btnIrLogin.setPrefWidth(300);
        btnIrLogin.setPrefHeight(50);

        btnIrLogin.setOnAction(e -> {
            try { new TelaLogin().start(stage); } catch (Exception ex) {}
        });

        conteudoDinamico.getChildren().addAll(lblTitulo, lblMensagem, btnIrLogin);
    }

    // --- LÓGICA DE BACKEND ---

    private void confirmarCodigo(Stage stage, String email, String codigo) {
        lblStatus.setText("Verificando...");
        lblStatus.setStyle("-fx-text-fill: #f1c40f;"); // Amarelo

        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("email", email);
            dados.put("codigo", codigo);
            String json = new ObjectMapper().writeValueAsString(dados);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/confirmar-cadastro"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.statusCode() == 200) {
                            if (timeline != null) timeline.stop();
                            montarTelaSucesso(stage);
                        } else {
                            lblStatus.setText("Código incorreto ou expirado.");
                            lblStatus.setStyle("-fx-text-fill: #ff6b6b;"); // Vermelho
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> lblStatus.setText("Erro de conexão."));
                        return null;
                    });
        } catch (Exception e) { lblStatus.setText("Erro interno."); }
    }

    private void reenviarCodigo(String email) {
        try {
            Map<String, String> dados = new HashMap<>();
            dados.put("email", email);
            String json = new ObjectMapper().writeValueAsString(dados);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE + "/reenviar-codigo"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.statusCode() == 200) {
                            Alert info = new Alert(Alert.AlertType.INFORMATION);
                            info.setTitle("Reenvio");
                            info.setHeaderText(null);
                            info.setContentText("Novo código enviado para seu e-mail.");
                            info.show();
                        }
                    }));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void iniciarContagemRegressiva(Button btn) {
        if (timeline != null) timeline.stop(); // Garante que não tenha dois timers rodando

        btn.setDisable(true);
        AtomicInteger segundos = new AtomicInteger(60);

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
            int s = segundos.decrementAndGet();
            btn.setText("Reenviar em " + s + "s");
            if (s <= 0) {
                timeline.stop();
                btn.setText("REENVIAR CÓDIGO");
                btn.setDisable(false);
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
}