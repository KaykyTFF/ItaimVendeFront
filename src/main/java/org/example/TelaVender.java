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
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.Anuncio;
import org.example.model.ImagemAnuncio;
import org.example.model.UsuarioDTO;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TelaVender {

    // --- VARIÁVEIS DE DADOS (Promovidas para classe para podermos acessar no "Salvar") ---
    private final List<String> imagensBase64 = new ArrayList<>();
    private TextField txtTitulo;
    private TextArea txtDescricao;
    private TextField txtPreco;
    private ComboBox<String> cmbCategoria;
    private ComboBox<String> cmbCondicao;

    // Variáveis de Localização
    private Label lblLocTxt;
    private String cidadeAtual = "Paulistana";
    private String cepAtual = "64750-000";

    // Referências visuais
    private StackPane areaFotoPrincipal;

    public void start(Stage stage) {
        // Inicializa slots de imagem
        for(int i=0; i<10; i++) imagensBase64.add(null);

        // --- 1. TÍTULO E NAVEGAÇÃO ---
        Button btnVoltar = new Button("⬅");
        btnVoltar.getStyleClass().add("btn-voltar");
        btnVoltar.setOnAction(e -> new TelaPrincipal().start(stage));

        Label lblTituloTela = new Label("Criar anúncio");
        lblTituloTela.getStyleClass().add("titulo-tela");

        HBox boxTitulo = new HBox(20, btnVoltar, lblTituloTela);
        boxTitulo.setAlignment(Pos.CENTER_LEFT);
        boxTitulo.setPadding(new Insets(30, 0, 20, 100));

        // --- 2. COLUNAS ---
        VBox colunaEsquerda = criarCardImagens(stage);

        // Inicializa os inputs antes de adicionar no layout
        inicializarInputs();

        VBox colunaDireita = new VBox(25);
        colunaDireita.getChildren().add(criarCardDadosInput());
        colunaDireita.getChildren().add(criarCardDropdowns());
        HBox.setHgrow(colunaDireita, Priority.ALWAYS);

        HBox containerPrincipal = new HBox(40, colunaEsquerda, colunaDireita);
        containerPrincipal.setAlignment(Pos.TOP_CENTER);
        containerPrincipal.setPadding(new Insets(0, 100, 30, 100));

        // --- 3. BOTÃO ANUNCIAR ---
        Button btnAnunciar = new Button("ANUNCIAR");
        btnAnunciar.getStyleClass().add("btn-anunciar-grande");

        // AÇÃO REAL DE SALVAR NO BANCO
        btnAnunciar.setOnAction(e -> processarCadastro(stage));

        VBox boxBotao = new VBox(btnAnunciar);
        boxBotao.setAlignment(Pos.CENTER);
        boxBotao.setPadding(new Insets(20, 0, 50, 0));

        // --- 4. LAYOUT FINAL ---
        VBox layoutConteudo = new VBox(boxTitulo, containerPrincipal, boxBotao);
        layoutConteudo.getStyleClass().add("fundo-pagina");
        layoutConteudo.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(layoutConteudo, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(layoutConteudo);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #D9D9D9; -fx-background-color: #D9D9D9;");

        try {
            String cssPath = getClass().getResource("/styles-vender.css").toExternalForm();
            layoutConteudo.getStylesheets().add(cssPath);
        } catch (Exception e) {}

        Scene scene = new LayoutPadrao().criarCena(stage, scrollPane);
        Navegacao.configurarJanela(stage, scene, "Criar Anúncio", true);
    }

    // =================================================================================
    // LÓGICA DE CADASTRO (BACKEND)
    // =================================================================================

    private void processarCadastro(Stage stage) {
        // 1. Validações
        if (txtTitulo.getText().isEmpty() || txtPreco.getText().isEmpty()) {
            alerta(Alert.AlertType.WARNING, "Preencha o Título e o Preço!");
            return;
        }
        if (imagensBase64.get(0) == null) {
            alerta(Alert.AlertType.WARNING, "A foto principal é obrigatória!");
            return;
        }

        try {
            UsuarioDTO usuario = Sessao.getUsuario();
            if (usuario == null) {
                alerta(Alert.AlertType.ERROR, "Sessão expirada. Faça login novamente.");
                new TelaLogin().start(stage);
                return;
            }

            // 2. Monta o Anúncio
            Anuncio novoAnuncio = new Anuncio();
            novoAnuncio.setUsuarioId(usuario.getId());
            novoAnuncio.setTitulo(txtTitulo.getText());
            novoAnuncio.setDescricao(txtDescricao.getText());
            String precoStr = txtPreco.getText().replace(",", ".");
            novoAnuncio.setPreco(Double.parseDouble(precoStr));
            novoAnuncio.setCategoria(cmbCategoria.getValue());
            novoAnuncio.setTipo(cmbCondicao.getValue());
            novoAnuncio.setCidade(cidadeAtual);
            novoAnuncio.setBairro(cepAtual);

            // Adiciona imagens
            for (int i = 0; i < imagensBase64.size(); i++) {
                String base64 = imagensBase64.get(i);
                if (base64 != null) {
                    novoAnuncio.adicionarImagem(base64, i == 0);
                }
            }

            // 3. Prepara o envio
            ObjectMapper mapper = new ObjectMapper();

            // --- CORREÇÃO DO ERRO DA IMAGEM ---
            // Se você adicionou a dependência no pom.xml, pode descomentar a linha abaixo.
            // Se não, deixe comentado e certifique-se que o Anuncio no front ignora erros de data.
            // mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

            // Isso evita que o app quebre se o backend mandar um campo que a gente não tem (tipo data complexa)
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String json = mapper.writeValueAsString(novoAnuncio);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8089/anuncios"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // 4. Envia e recebe a resposta
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> Platform.runLater(() -> {
                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            try {
                                // SUCESSO! Converte o JSON de volta para objeto
                                Anuncio anuncioSalvo = mapper.readValue(response.body(), Anuncio.class);

                                // --- AQUI CHAMAMOS A TELA DE SUCESSO ---
                                new TelaSucesso(anuncioSalvo).start(stage);

                            } catch (Exception e) {
                                e.printStackTrace();
                                // Se falhar ao ler a resposta, vai pra home
                                new TelaPrincipal().start(stage);
                            }
                        } else {
                            alerta(Alert.AlertType.ERROR, "Erro: " + response.statusCode());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> alerta(Alert.AlertType.ERROR, "Falha na conexão: " + ex.getMessage()));
                        return null;
                    });

        } catch (NumberFormatException e) {
            alerta(Alert.AlertType.ERROR, "Preço inválido!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================================================================================
    // LÓGICA DE LOCALIZAÇÃO (EDITAR)
    // =================================================================================

    private void abrirDialogoLocalizacao() {
        // Cria um Dialog customizado simples
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Localização");
        dialog.setHeaderText("Informe onde está o produto");

        ButtonType btnConfirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtCidade = new TextField(cidadeAtual);
        TextField txtCep = new TextField(cepAtual);

        grid.add(new Label("Cidade/UF:"), 0, 0);
        grid.add(txtCidade, 1, 0);
        grid.add(new Label("CEP:"), 0, 1);
        grid.add(txtCep, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Espera o usuário clicar
        dialog.showAndWait().ifPresent(response -> {
            if (response == btnConfirmar) {
                // Atualiza variáveis
                cidadeAtual = txtCidade.getText();
                cepAtual = txtCep.getText();
                // Atualiza visual
                lblLocTxt.setText(cidadeAtual + " - " + cepAtual);
            }
        });
    }

    // =================================================================================
    // COMPONENTES VISUAIS & INPUTS
    // =================================================================================

    private void inicializarInputs() {
        txtTitulo = new TextField();
        txtTitulo.setPromptText("Título do Anúncio");
        txtTitulo.setPrefHeight(45);
        txtTitulo.getStyleClass().add("input-padrao");

        txtDescricao = new TextArea();
        txtDescricao.setPromptText("Descrição detalhada...");
        txtDescricao.setWrapText(true);
        txtDescricao.setPrefRowCount(6);
        txtDescricao.getStyleClass().add("text-area-padrao");

        txtPreco = new TextField();
        txtPreco.setPromptText("Preço (R$)");
        txtPreco.setPrefHeight(45);
        txtPreco.getStyleClass().add("input-padrao");

        cmbCategoria = new ComboBox<>();
        cmbCategoria.getItems().addAll("Eletrônicos", "Móveis", "Serviços", "Veículos", "Imóveis");
        cmbCategoria.setPromptText("CATEGORIA");
        configurarDropdown(cmbCategoria);

        cmbCondicao = new ComboBox<>();
        cmbCondicao.getItems().addAll("Novo", "Usado");
        cmbCondicao.setPromptText("CONDIÇÃO");
        configurarDropdown(cmbCondicao);
    }

    private VBox criarCardImagens(Stage stage) {
        VBox card = new VBox(20);
        card.getStyleClass().add("card-padrao");
        card.setPadding(new Insets(30));
        card.setPrefWidth(380);
        card.setMinWidth(380);

        // Foto Principal
        areaFotoPrincipal = new StackPane();
        areaFotoPrincipal.setPrefSize(320, 260);
        areaFotoPrincipal.getStyleClass().add("area-foto");

        Label lblIcone = new Label("📷");
        lblIcone.getStyleClass().add("icone-camera");
        areaFotoPrincipal.getChildren().add(lblIcone);
        areaFotoPrincipal.setOnMouseClicked(e -> selecionarImagem(stage, 0, areaFotoPrincipal));

        // Miniaturas
        FlowPane gridFotos = new FlowPane();
        gridFotos.setHgap(15);
        gridFotos.setVgap(15);
        gridFotos.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 3; i++) criarSlotMiniatura(gridFotos, stage, i);

        Button btnMaisFotos = new Button("Mais fotos...");
        btnMaisFotos.setStyle("-fx-background-color: transparent; -fx-text-fill: #59AA59; -fx-cursor: hand;");
        btnMaisFotos.setOnAction(e -> {
            if (gridFotos.getChildren().size() < 9) criarSlotMiniatura(gridFotos, stage, gridFotos.getChildren().size() + 1);
        });

        // Localização
        VBox boxLocal = new VBox(5);
        Label lblLocTit = new Label("Localização");
        lblLocTit.getStyleClass().add("titulo-secao");

        lblLocTxt = new Label(cidadeAtual + " - " + cepAtual);
        lblLocTxt.getStyleClass().add("texto-comum");

        Button btnEditar = new Button("EDITAR");
        btnEditar.getStyleClass().add("btn-editar-pequeno");
        // AÇÃO DO BOTÃO EDITAR
        btnEditar.setOnAction(e -> abrirDialogoLocalizacao());

        boxLocal.getChildren().addAll(lblLocTit, lblLocTxt, btnEditar);
        boxLocal.setPadding(new Insets(20, 0, 0, 0));

        card.getChildren().addAll(areaFotoPrincipal, gridFotos, btnMaisFotos, new Separator(), boxLocal);
        return card;
    }

    private void criarSlotMiniatura(Pane container, Stage stage, int index) {
        StackPane thumb = new StackPane();
        thumb.setPrefSize(90, 90);
        thumb.getStyleClass().add("area-foto");
        Label lblMais = new Label("+");
        lblMais.getStyleClass().add("icone-mais");
        thumb.getChildren().add(lblMais);
        thumb.setOnMouseClicked(e -> selecionarImagem(stage, index, thumb));
        container.getChildren().add(thumb);
    }

    private void selecionarImagem(Stage stage, int index, StackPane containerVisual) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        File arquivo = fileChooser.showOpenDialog(stage);

        if (arquivo != null) {
            try {
                FileInputStream fis = new FileInputStream(arquivo);
                byte[] bytes = new byte[(int) arquivo.length()];
                fis.read(bytes);
                fis.close();

                imagensBase64.set(index, Base64.getEncoder().encodeToString(bytes));

                Image img = new Image(new FileInputStream(arquivo));
                ImageView imgView = new ImageView(img);
                if (index == 0) { imgView.setFitWidth(320); imgView.setFitHeight(260); }
                else { imgView.setFitWidth(90); imgView.setFitHeight(90); }

                imgView.setPreserveRatio(true);
                Rectangle clip = new Rectangle(imgView.getFitWidth(), imgView.getFitHeight());
                clip.setArcWidth(10); clip.setArcHeight(10);
                imgView.setClip(clip);

                containerVisual.getChildren().clear();
                containerVisual.getChildren().add(imgView);
                containerVisual.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private VBox criarCardDadosInput() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card-padrao");
        card.setPadding(new Insets(40));
        // Adicionamos os componentes que já inicializamos lá em cima
        card.getChildren().addAll(txtTitulo, txtDescricao, txtPreco);
        return card;
    }

    private VBox criarCardDropdowns() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card-padrao");
        card.setPadding(new Insets(25, 40, 25, 40));
        card.getChildren().addAll(cmbCategoria, cmbCondicao);
        return card;
    }

    private <T> void configurarDropdown(ComboBox<T> comboBox) {
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setPrefHeight(45);
        comboBox.getStyleClass().add("combo-verde");
    }

    private void alerta(Alert.AlertType tipo, String msg) {
        Alert alert = new Alert(tipo, msg);
        alert.show();
    }
}