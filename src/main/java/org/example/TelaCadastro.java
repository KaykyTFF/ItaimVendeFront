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
import org.example.model.UsuarioDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.InputMismatchException;

public class TelaCadastro {

    private static final String API_URL = "http://localhost:8089/auth";
    private static final int LARGURA_CAMPO = 600;

    // --- CAMPOS ESTÁTICOS ---
    private static TextField txtNome;
    private static TextField txtNascimento;
    private static TextField txtCpf;
    private static ComboBox<String> comboCidade;
    private static TextField txtBairro;
    private static TextField txtTelefone;
    private static TextField txtUsername;
    private static TextField txtEmail;
    private static PasswordField txtSenha;
    private static PasswordField txtConfirma;

    private Label lblStatus;

    public void start(Stage stage) {
        if (txtNome == null) {
            inicializarCampos();
        }

        // --- 1. TOPO ---
        ImageView logoView = new ImageView();
        try {
            logoView.setImage(new Image(getClass().getResourceAsStream("/header-logo.png")));
            logoView.setFitHeight(150);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {}

        HBox topo = new HBox(logoView);
        topo.setAlignment(Pos.CENTER);
        topo.setPadding(new Insets(15));
        topo.setStyle("-fx-background-color: white;");

        // --- 2. FORMULÁRIO ---
        lblStatus = new Label("");
        lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14px;");
        lblStatus.setWrapText(true);
        lblStatus.setMaxWidth(LARGURA_CAMPO);

        VBox formulario = new VBox(15);
        formulario.setAlignment(Pos.TOP_CENTER);
        formulario.setStyle("-fx-background-color: transparent;");

        formulario.getChildren().addAll(
                criarTituloSecao("DADOS PESSOAIS"),
                criarGrupoInput("Nome Completo:", txtNome),
                criarGrupoInput("Data de Nascimento:", txtNascimento),
                criarGrupoInput("CPF:", txtCpf),

                criarTituloSecao("LOCALIZAÇÃO E CONTATO"),
                criarGrupoInput("Cidade:", comboCidade),
                criarGrupoInput("Bairro:", txtBairro),
                criarGrupoInput("WhatsApp / Contato:", txtTelefone),

                criarTituloSecao("DADOS DA CONTA"),
                criarGrupoInput("Nome de Usuário (@):", txtUsername),
                criarGrupoInput("E-mail:", txtEmail),
                criarGrupoInput("Senha:", txtSenha),
                criarGrupoInput("Confirmar Senha:", txtConfirma)
        );

        Button btnCadastrar = new Button("CONCLUIR CADASTRO");
        btnCadastrar.getStyleClass().add("btn-entrar");
        btnCadastrar.setPrefWidth(LARGURA_CAMPO);
        btnCadastrar.setPrefHeight(50);
        btnCadastrar.setOnAction(e -> cadastrar(stage));

        Button btnVoltar = new Button("Voltar ao Login");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-underline: true; -fx-cursor: hand; -fx-font-size: 14px;");
        btnVoltar.setOnAction(e -> {
            try {
                limparDados();
                new TelaLogin().start(stage);
            } catch (Exception ex) {}
        });

        VBox boxBotoes = new VBox(15, lblStatus, btnCadastrar, btnVoltar);
        boxBotoes.setAlignment(Pos.CENTER);
        boxBotoes.setPadding(new Insets(20, 0, 0, 0));

        formulario.getChildren().add(boxBotoes);

        // --- 3. SCROLL E LAYOUT ---
        ScrollPane scroll = new ScrollPane(formulario);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPannable(true);

        VBox painelInferior = new VBox(scroll);
        painelInferior.getStyleClass().add("painel-inferior");
        painelInferior.setPadding(new Insets(30, 20, 20, 20));

        VBox.setVgrow(painelInferior, Priority.ALWAYS);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox layout = new VBox(topo, painelInferior);
        layout.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(layout);
        try { scene.getStylesheets().add(getClass().getResource("/styles-login.css").toExternalForm()); } catch (Exception e) {}

        Navegacao.configurarJanela(stage, scene, "Novo Cadastro", false);
    }

    private void inicializarCampos() {
        txtNome = new TextField(); txtNome.setPromptText("Seu nome completo");

        txtNascimento = new TextField();
        txtNascimento.setPromptText("DD/MM/AAAA");
        configurarMascaraData(txtNascimento);

        txtCpf = new TextField();
        txtCpf.setPromptText("000.000.000-00");
        configurarMascaraCPF(txtCpf);

        comboCidade = new ComboBox<>();
        comboCidade.getItems().addAll("Paulistana", "Acauã", "Betânia do Piauí", "Curral Novo", "Jacobina", "Queimada Nova", "Patos", "Simões", "Caridade", "Marcolândia", "Jaicós", "Massapê", "Picos", "Petrolina");
        comboCidade.setMaxWidth(LARGURA_CAMPO);
        comboCidade.setPrefHeight(45);

        txtBairro = new TextField(); txtBairro.setPromptText("Seu Bairro");

        txtTelefone = new TextField(); txtTelefone.setPromptText("(00) 00000-0000");
        configurarMascaraTelefone(txtTelefone);

        txtUsername = new TextField(); txtUsername.setPromptText("ex: joaosilva");
        txtEmail = new TextField(); txtEmail.setPromptText("email@exemplo.com");

        txtSenha = new PasswordField();
        txtSenha.setPromptText("Mín 8 chars, 1 Maiúscula, 1 Símbolo");

        txtConfirma = new PasswordField();
    }

    public static void limparDados() {
        txtNome = null; txtNascimento = null; txtCpf = null;
        comboCidade = null; txtBairro = null; txtTelefone = null;
        txtUsername = null; txtEmail = null; txtSenha = null; txtConfirma = null;
    }

    // --- LÓGICA DE VALIDAÇÃO (NOVA) ---

    // 1. Validador Matemático de CPF (Receita Federal)
    private boolean isCPFValido(String cpf) {
        // Remove máscara
        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != 11) return false;
        // Evita sequências conhecidas inválidas
        if (cpf.equals("00000000000") || cpf.equals("11111111111")) return false;

        try {
            // Calculo do 1º Dígito Verificador
            int soma = 0, peso = 10;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            int r = 11 - (soma % 11);
            char dig10 = (r == 10 || r == 11) ? '0' : (char) (r + '0');

            // Calculo do 2º Dígito Verificador
            soma = 0; peso = 11;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            r = 11 - (soma % 11);
            char dig11 = (r == 10 || r == 11) ? '0' : (char) (r + '0');

            return (dig10 == cpf.charAt(9)) && (dig11 == cpf.charAt(10));
        } catch (InputMismatchException e) {
            return false;
        }
    }

    // 2. Validador de Senha Forte
    private boolean isSenhaForte(String senha) {
        if (senha.length() < 8) return false;
        boolean temMaiuscula = false;
        boolean temMinuscula = false;
        boolean temSimbolo = false;

        for (char c : senha.toCharArray()) {
            if (Character.isUpperCase(c)) temMaiuscula = true;
            else if (Character.isLowerCase(c)) temMinuscula = true;
            else if (!Character.isDigit(c)) temSimbolo = true; // Se não é letra nem número, é símbolo
        }
        return temMaiuscula && temMinuscula && temSimbolo;
    }

    private String converterDataParaAPI(String dataBR) {
        try {
            String[] partes = dataBR.split("/");
            return partes[2] + "-" + partes[1] + "-" + partes[0];
        } catch (Exception e) { return null; }
    }

    private void cadastrar(Stage stage) {
        // Validação de Campos Vazios
        if (txtNome.getText().isEmpty() || txtCpf.getText().isEmpty() || txtUsername.getText().isEmpty() ||
                txtEmail.getText().isEmpty() || txtSenha.getText().isEmpty()) {
            lblStatus.setText("Preencha todos os campos.");
            return;
        }

        // Validação de CPF
        if (!isCPFValido(txtCpf.getText())) {
            lblStatus.setText("CPF inválido. Verifique os números.");
            return;
        }

        // Validação de Senha Forte
        if (!isSenhaForte(txtSenha.getText())) {
            lblStatus.setText("Senha fraca: use 8 caracteres, 1 maiúscula e 1 símbolo (#, @, $...)");
            return;
        }

        if (!txtSenha.getText().equals(txtConfirma.getText())) {
            lblStatus.setText("As senhas não coincidem.");
            return;
        }

        String dataFormatada = converterDataParaAPI(txtNascimento.getText());
        if (dataFormatada == null) {
            lblStatus.setText("Data inválida. Use DD/MM/AAAA");
            return;
        }

        lblStatus.setText("Verificando dados...");

        UsuarioDTO u = new UsuarioDTO();
        u.setNome(txtNome.getText());
        u.setDataNascimento(dataFormatada);
        u.setCpf(txtCpf.getText());
        u.setCidade(comboCidade.getValue() != null ? comboCidade.getValue() : "");
        u.setBairro(txtBairro.getText());
        u.setTelefone(txtTelefone.getText());
        u.setUsername(txtUsername.getText());
        u.setEmail(txtEmail.getText());
        u.setSenha(txtSenha.getText());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(u);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cadastro"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(res -> Platform.runLater(() -> {
                        if (res.statusCode() == 200) {
                            new TelaConfirmacao().start(stage, txtEmail.getText());
                        } else {
                            // Exibe a mensagem de erro específica do Backend (ex: "CPF já existe")
                            lblStatus.setText("Erro: " + res.body());
                        }
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(() -> lblStatus.setText("Erro de conexão com o servidor."));
                        return null;
                    });
        } catch (Exception e) {
            lblStatus.setText("Erro interno.");
        }
    }

    // --- MÁSCARAS E LAYOUT ---
    private void configurarMascaraCPF(TextField tf) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.equals(oldV)) return;
            String d = newV.replaceAll("[^0-9]", "");
            if (d.length() > 11) d = d.substring(0, 11);
            String fmt = "";
            if (d.length() > 0) {
                fmt = d.substring(0, Math.min(3, d.length()));
                if (d.length() > 3) fmt += "." + d.substring(3, Math.min(6, d.length()));
                if (d.length() > 6) fmt += "." + d.substring(6, Math.min(9, d.length()));
                if (d.length() > 9) fmt += "-" + d.substring(9);
            }
            if (!fmt.equals(tf.getText())) {
                final String f = fmt;
                Platform.runLater(() -> { tf.setText(f); tf.positionCaret(f.length()); });
            }
        });
    }

    private void configurarMascaraData(TextField tf) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.equals(oldV)) return;
            String d = newV.replaceAll("[^0-9]", "");
            if (d.length() > 8) d = d.substring(0, 8);
            String fmt = "";
            if (d.length() > 0) {
                fmt = d.substring(0, Math.min(2, d.length()));
                if (d.length() > 2) fmt += "/" + d.substring(2, Math.min(4, d.length()));
                if (d.length() > 4) fmt += "/" + d.substring(4);
            }
            if (!fmt.equals(tf.getText())) {
                final String f = fmt;
                Platform.runLater(() -> { tf.setText(f); tf.positionCaret(f.length()); });
            }
        });
    }

    private void configurarMascaraTelefone(TextField tf) {
        tf.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.equals(oldV)) return;
            String d = newV.replaceAll("[^0-9]", "");
            if (d.length() > 11) d = d.substring(0, 11);
            String fmt = "";
            if (d.length() > 0) {
                fmt = "(" + (d.length() > 2 ? d.substring(0, 2) + ") " : d);
                if (d.length() > 2) fmt += (d.length() > 7 ? d.substring(2, 7) + "-" + d.substring(7) : d.substring(2));
            }
            if (!fmt.equals(tf.getText())) {
                final String f = fmt;
                Platform.runLater(() -> { tf.setText(f); tf.positionCaret(f.length()); });
            }
        });
    }

    private Label criarTituloSecao(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: #44BD32; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 20 0 5 0;");
        return l;
    }

    private VBox criarGrupoInput(String label, Control input) {
        Label l = new Label(label);
        l.getStyleClass().add("label-campo");
        input.getStyleClass().add("campo-texto");
        if(input instanceof TextField) ((TextField)input).setPrefHeight(45);
        if(input instanceof ComboBox) ((ComboBox)input).setPrefHeight(45);
        VBox v = new VBox(5, l, input);
        v.setMaxWidth(LARGURA_CAMPO);
        return v;
    }
}