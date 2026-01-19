package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
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
import javafx.stage.Stage;
import org.example.model.Anuncio;
import org.example.model.UsuarioDTO;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsável pelo painel de gerenciamento de anúncios do usuário logado.
 * <p>
 * Permite listar, visualizar e excluir os anúncios pertencentes à sessão atual.
 * A interface reutiliza o padrão visual de cards (estilo Polaroid) com a adição
 * de controles administrativos (Editar/Excluir).
 *
 * @author Kayky Terles
 * @version 0.0.1
 */
public class TelaMeusAnuncios {

    private static final String API_URL = "http://localhost:8089/anuncios";

    /** Container flexível para dispor os cards em grade/fluxo. */
    private FlowPane gridAnuncios;

    /**
     * Inicializa a tela de gerenciamento.
     * Configura o cabeçalho global, o título da seção e a área de rolagem para os cards.
     * Ao final, dispara o carregamento assíncrono dos dados.
     *
     * @param stage O palco principal onde a cena será renderizada.
     */
    public void start(Stage stage) {
        // --- 1. CONSTRUÇÃO DO CONTEÚDO (Mantendo suas variáveis) ---

        // Subtítulo da Seção
        Label lblTitulo = new Label("Gerenciar Meus Anúncios");
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

        HBox containerTitulo = new HBox(lblTitulo);
        containerTitulo.setPadding(new Insets(20, 0, 0, 30));

        // --- 2. ÁREA DE CONTEÚDO (GRADE) ---
        gridAnuncios = new FlowPane();
        gridAnuncios.setHgap(20);
        gridAnuncios.setVgap(20);
        gridAnuncios.setPadding(new Insets(30));
        gridAnuncios.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(gridAnuncios);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Montagem do Layout Vertical (Sem o Topo aqui, pois o LayoutPadrao vai colocar)
        VBox layoutConteudo = new VBox(containerTitulo, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // --- 3. CONFIGURAÇÃO DA CENA (USANDO O LAYOUT PADRÃO) ---
        // Aqui usamos a classe LayoutPadrao para envolver seu conteúdo com o Menu Lateral
        Scene scene = new LayoutPadrao().criarCena(stage, layoutConteudo);

        // PADRONIZAÇÃO: true = Modo Dashboard (Janela grande, expansível)
        Navegacao.configurarJanela(stage, scene, "Gerenciar Meus Anúncios", true);

        carregarMeusAnuncios();
    }

    /**
     * Busca os anúncios na API e filtra localmente pelo ID do usuário logado.
     * <p>
     * Nota: A filtragem é feita no cliente (stream filter) baseada na sessão atual.
     * O processamento da resposta HTTP e atualização da UI ocorrem em threads separadas.
     */
    private void carregarMeusAnuncios() {
        UsuarioDTO usuarioLogado = Sessao.getUsuario();

        // Se não houver sessão válida, interrompe o carregamento
        if (usuarioLogado == null) return;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            // Desserializa todos os anúncios
                            List<Anuncio> todos = mapper.readValue(response.body(), new TypeReference<List<Anuncio>>() {});

                            // Filtra apenas os que pertencem ao usuário da sessão
                            List<Anuncio> meus = todos.stream()
                                    .filter(a -> a.getUsuarioId() != null && a.getUsuarioId().equals(usuarioLogado.getId()))
                                    .collect(Collectors.toList());

                            // Atualiza a UI na Thread do JavaFX
                            Platform.runLater(() -> atualizarTela(meus));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Atualiza o container visual (Grid) com os cards dos anúncios.
     * Exibe uma mensagem informativa caso a lista esteja vazia.
     *
     * @param anuncios Lista de anúncios filtrada.
     */
    private void atualizarTela(List<Anuncio> anuncios) {
        gridAnuncios.getChildren().clear();

        if (anuncios.isEmpty()) {
            Label lbl = new Label("Você ainda não tem anúncios ativos.");
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            gridAnuncios.getChildren().add(lbl);
            return;
        }

        for (Anuncio a : anuncios) {
            gridAnuncios.getChildren().add(criarCardGerencia(a));
        }
    }

    /**
     * Factory Method para criação do card de gerenciamento.
     * <p>
     * Diferente do card da Home, este inclui botões de ação (Editar/Excluir).
     * Implementa lógica de corte (clip) e centralização de imagem.
     *
     * @param anuncio Objeto contendo os dados do anúncio.
     * @return VBox configurado representando o card.
     */
    private VBox criarCardGerencia(Anuncio anuncio) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        // Estilo visual: Sombra suave e bordas arredondadas
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 10, 0, 0, 0); -fx-background-radius: 12;");
        card.setPrefWidth(240);
        card.setMinWidth(240);
        card.setMaxWidth(240);

        // Configuração do container da imagem com máscara de corte
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(210, 160);
        imageContainer.setMinSize(210, 160);
        imageContainer.setMaxSize(210, 160);

        Rectangle clip = new Rectangle(210, 160);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        imageContainer.setClip(clip);

        ImageView imgView = new ImageView();
        String capaBase64 = anuncio.getFotoCapa();

        // Processamento da Imagem
        if (capaBase64 != null) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(capaBase64);
                Image imagemReal = new Image(new ByteArrayInputStream(imgBytes));
                imgView.setImage(imagemReal);

                if (imagemReal.getWidth() > 0) {
                    // Calcula escala para preencher o container (Object-fit: Cover logic)
                    double scale = Math.max(210 / imagemReal.getWidth(), 160 / imagemReal.getHeight());
                    imgView.setFitWidth(imagemReal.getWidth() * scale);
                    imgView.setFitHeight(imagemReal.getHeight() * scale);
                    imgView.setPreserveRatio(true);
                }
            } catch (Exception e) {
                // Falha silenciosa na imagem
            }
        } else {
            imageContainer.setStyle("-fx-background-color: #f0f2f5;");
        }

        imageContainer.getChildren().add(imgView);
        StackPane.setAlignment(imgView, Pos.CENTER);

        // Labels informativos
        Label lblTitulo = new Label(anuncio.getTitulo());
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");
        lblTitulo.setWrapText(true);
        lblTitulo.setMaxHeight(40);

        Label lblPreco = new Label("R$ " + anuncio.getPreco());
        lblPreco.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 15px;");

        // Botões de Ação
        HBox boxBotoes = new HBox(10);
        boxBotoes.setAlignment(Pos.CENTER);

        Button btnEditar = new Button("Editar");
        btnEditar.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEditar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnEditar, Priority.ALWAYS);
        btnEditar.setOnAction(e -> { new Alert(Alert.AlertType.INFORMATION, "Editar em breve!").show(); });

        Button btnExcluir = new Button("Excluir");
        btnExcluir.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnExcluir.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnExcluir, Priority.ALWAYS);

        // Gatilho de exclusão
        btnExcluir.setOnAction(e -> deletarAnuncio(anuncio.getId()));

        boxBotoes.getChildren().addAll(btnEditar, btnExcluir);
        card.getChildren().addAll(imageContainer, lblTitulo, lblPreco, boxBotoes);

        return card;
    }

    /**
     * Envia requisição DELETE para a API para remover o anúncio.
     * Atualiza a lista automaticamente em caso de sucesso.
     *
     * @param id ID do anúncio a ser removido.
     */
    private void deletarAnuncio(Long id) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/" + id))
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> Platform.runLater(this::carregarMeusAnuncios));
    }
}