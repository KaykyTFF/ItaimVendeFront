package org.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import org.example.model.UsuarioDTO;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class TelaCortarFoto {

    // Variáveis de controle de arrasto
    private double inicioX, inicioY;
    private double translateInicialX, translateInicialY;

    // --- DIMENSÕES DA JANELA ---
    private static final double LARGURA_JANELA = 380;
    private static final double LARGURA_AREA_CROP = 340;
    private static final double ALTURA_AREA_CROP = 280;
    private static final double RAIO_CIRCULO = 130;

    public void showModal(StackPane parentContainer, Image imagemOriginal, Runnable aoSalvar) {

        // Validação de segurança
        if (imagemOriginal == null) {
            System.err.println("Erro: Imagem original é nula!");
            return;
        }

        // 1. Overlay (Fundo)
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("modal-overlay");

        // 2. Janela
        VBox modalWindow = new VBox(0);
        modalWindow.getStyleClass().add("modal-window");
        modalWindow.setMaxWidth(LARGURA_JANELA);
        modalWindow.setMaxHeight(Region.USE_PREF_SIZE);
        modalWindow.setPadding(new Insets(20));

        // 2.1 Cabeçalho
        Label lblTitulo = new Label("Editar imagem");
        lblTitulo.getStyleClass().add("titulo-modal");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnFechar = new Button("✕");
        btnFechar.getStyleClass().add("btn-fechar-modal");
        btnFechar.setOnAction(e -> parentContainer.getChildren().remove(overlay));

        HBox header = new HBox(lblTitulo, spacer, btnFechar);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));

        // 2.2 Área de Recorte (Container)
        StackPane areaCrop = new StackPane();
        areaCrop.setPrefSize(LARGURA_AREA_CROP, ALTURA_AREA_CROP);
        areaCrop.setMinSize(LARGURA_AREA_CROP, ALTURA_AREA_CROP);
        areaCrop.setMaxSize(LARGURA_AREA_CROP, ALTURA_AREA_CROP);
        areaCrop.getStyleClass().add("area-crop-container");
        areaCrop.setStyle("-fx-background-color: #2b2d31;");

        // --- IMAGEM ---
        ImageView imageView = new ImageView(imagemOriginal);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        // Escala Inicial
        double diametroCirculo = RAIO_CIRCULO * 2;

        double scaleParaCirculo = Math.max(
                diametroCirculo / imagemOriginal.getWidth(),
                diametroCirculo / imagemOriginal.getHeight()
        );

        double scaleParaContainer = Math.max(
                LARGURA_AREA_CROP / imagemOriginal.getWidth(),
                ALTURA_AREA_CROP / imagemOriginal.getHeight()
        );

        double scaleInit = Math.max(scaleParaCirculo, scaleParaContainer) * 1.05;

        imageView.setFitWidth(imagemOriginal.getWidth() * scaleInit);
        imageView.setFitHeight(imagemOriginal.getHeight() * scaleInit);

        centerImage(imageView, LARGURA_AREA_CROP, ALTURA_AREA_CROP);

        // --- MÁSCARA ESCURA ---
        double tamanhoMascara = Math.max(LARGURA_AREA_CROP, ALTURA_AREA_CROP) * 5;
        Rectangle rectBg = new Rectangle(
                (LARGURA_AREA_CROP - tamanhoMascara) / 2,
                (ALTURA_AREA_CROP - tamanhoMascara) / 2,
                tamanhoMascara,
                tamanhoMascara
        );

        Circle circulo = new Circle(LARGURA_AREA_CROP/2, ALTURA_AREA_CROP/2, RAIO_CIRCULO);

        Shape mascara = Shape.subtract(rectBg, circulo);
        mascara.setFill(Color.rgb(0, 0, 0, 0.5));
        mascara.setMouseTransparent(true);

        // Borda Branca
        Circle borda = new Circle(LARGURA_AREA_CROP/2, ALTURA_AREA_CROP/2, RAIO_CIRCULO);
        borda.setFill(Color.TRANSPARENT);
        borda.setStroke(Color.WHITE);
        borda.setStrokeWidth(2);
        borda.setMouseTransparent(true);

        areaCrop.getChildren().addAll(imageView, new Group(mascara, borda));
        areaCrop.setClip(new Rectangle(LARGURA_AREA_CROP, ALTURA_AREA_CROP));

        // 2.3 Slider de Zoom
        Slider sliderZoom = new Slider(1.0, 3.0, 1.0);
        sliderZoom.getStyleClass().add("slider-zoom");
        sliderZoom.setMaxWidth(300);

        HBox boxSlider = new HBox(sliderZoom);
        boxSlider.setAlignment(Pos.CENTER);
        boxSlider.setPadding(new Insets(15, 0, 15, 0));

        // Eventos
        configurarZoom(sliderZoom, imageView);
        configurarArrasto(areaCrop, imageView);

        // 2.4 Botões de Ação
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn-cancelar");
        btnCancelar.setOnAction(e -> parentContainer.getChildren().remove(overlay));

        Button btnSalvar = new Button("Salvar");
        btnSalvar.getStyleClass().add("btn-salvar");
        btnSalvar.setOnAction(e -> {
            Image corte = realizarCrop(imageView, RAIO_CIRCULO, LARGURA_AREA_CROP, ALTURA_AREA_CROP);
            salvarNoUsuario(corte);
            parentContainer.getChildren().remove(overlay);
            if(aoSalvar != null) aoSalvar.run();
        });

        HBox footer = new HBox(15, btnCancelar, btnSalvar);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 0, 0));

        // Monta a janela modal
        modalWindow.getChildren().addAll(header, areaCrop, boxSlider, footer);
        overlay.getChildren().add(modalWindow);
        parentContainer.getChildren().add(overlay);
    }

    /**
     * Centraliza a imagem dentro do container
     */
    private void centerImage(ImageView iv, double w, double h) {
        if (iv.getImage() != null) {
            double imgWidth = iv.getFitWidth();
            double imgHeight = iv.getFitHeight();

            iv.setTranslateX((w - imgWidth) / 2);
            iv.setTranslateY((h - imgHeight) / 2);
        }
    }

    /**
     * Configura o slider de zoom
     */
    private void configurarZoom(Slider slider, ImageView imageView) {
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            imageView.setScaleX(newVal.doubleValue());
            imageView.setScaleY(newVal.doubleValue());
            forceBounds(imageView);
        });
    }

    /**
     * Configura o arrasto da imagem com o mouse
     */
    private void configurarArrasto(Pane areaInterativa, ImageView imageView) {
        areaInterativa.setOnMousePressed(e -> {
            inicioX = e.getSceneX();
            inicioY = e.getSceneY();
            translateInicialX = imageView.getTranslateX();
            translateInicialY = imageView.getTranslateY();
        });

        areaInterativa.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - inicioX;
            double deltaY = e.getSceneY() - inicioY;

            imageView.setTranslateX(translateInicialX + deltaX);
            imageView.setTranslateY(translateInicialY + deltaY);

            forceBounds(imageView);
        });
    }

    /**
     * Força os limites baseados no CÍRCULO de recorte
     */
    private void forceBounds(ImageView iv) {
        Bounds bounds = iv.getBoundsInParent();

        double centroX = LARGURA_AREA_CROP / 2.0;
        double centroY = ALTURA_AREA_CROP / 2.0;

        double circuloMinX = centroX - RAIO_CIRCULO;
        double circuloMaxX = centroX + RAIO_CIRCULO;
        double circuloMinY = centroY - RAIO_CIRCULO;
        double circuloMaxY = centroY + RAIO_CIRCULO;

        double diffX = 0;
        double diffY = 0;

        if (bounds.getMinX() > circuloMinX) {
            diffX = circuloMinX - bounds.getMinX();
        }
        else if (bounds.getMaxX() < circuloMaxX) {
            diffX = circuloMaxX - bounds.getMaxX();
        }

        if (bounds.getMinY() > circuloMinY) {
            diffY = circuloMinY - bounds.getMinY();
        }
        else if (bounds.getMaxY() < circuloMaxY) {
            diffY = circuloMaxY - bounds.getMaxY();
        }

        iv.setTranslateX(iv.getTranslateX() + diffX);
        iv.setTranslateY(iv.getTranslateY() + diffY);
    }

    /**
     * ⭐ MÉTODO CORRIGIDO - Realiza o recorte circular da imagem
     */
    private Image realizarCrop(ImageView iv, double raio, double wContainer, double hContainer) {
        // Cria um container temporário do tamanho exato do círculo
        StackPane pane = new StackPane();
        pane.setPrefSize(raio * 2, raio * 2);
        pane.setMinSize(raio * 2, raio * 2);
        pane.setMaxSize(raio * 2, raio * 2);
        pane.setStyle("-fx-background-color: transparent");

        // Clona a ImageView com TODAS as transformações
        ImageView clone = new ImageView(iv.getImage());
        clone.setFitWidth(iv.getFitWidth());
        clone.setFitHeight(iv.getFitHeight());
        clone.setPreserveRatio(true);
        clone.setSmooth(true);

        // ⭐ Aplica escala E translação
        clone.setScaleX(iv.getScaleX());
        clone.setScaleY(iv.getScaleY());

        // Calcula o offset para capturar EXATAMENTE o centro do círculo
        double centroCropX = wContainer / 2.0;
        double centroCropY = hContainer / 2.0;

        double centroPaneX = raio;
        double centroPaneY = raio;

        double offsetX = centroCropX - centroPaneX;
        double offsetY = centroCropY - centroPaneY;

        // Ajusta a posição
        clone.setTranslateX(iv.getTranslateX() - offsetX);
        clone.setTranslateY(iv.getTranslateY() - offsetY);

        pane.getChildren().add(clone);

        // Aplica máscara circular
        Circle clipCircle = new Circle(raio, raio, raio);
        pane.setClip(clipCircle);

        // Captura a imagem
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        return pane.snapshot(params, null);
    }

    /**
     * ⭐ MÉTODO CORRIGIDO - Salva a imagem cortada no usuário em formato Base64
     */
    private void salvarNoUsuario(Image img) {
        if (img == null) {
            System.err.println("❌ ERRO: Imagem cortada é nula!");
            return;
        }

        try {
            // Converte para BufferedImage
            java.awt.image.BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);

            if (bImage == null) {
                System.err.println("❌ ERRO: Conversão para BufferedImage falhou!");
                return;
            }

            // Converte para Base64
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            boolean sucesso = ImageIO.write(bImage, "png", s);

            if (!sucesso) {
                System.err.println("❌ ERRO: Falha ao escrever PNG!");
                return;
            }

            String base64Final = "data:image/png;base64," + Base64.getEncoder().encodeToString(s.toByteArray());

            // Salva no usuário
            UsuarioDTO user = Sessao.getUsuario();
            if (user != null) {
                user.setFotoPerfil(base64Final);
                System.out.println("✅ Foto salva com sucesso! Tamanho: " + base64Final.length() + " chars");
                System.out.println("📐 Dimensões da imagem: " + (int)img.getWidth() + "x" + (int)img.getHeight() + "px");
            } else {
                System.err.println("❌ ERRO: Usuário na sessão é nulo!");
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO ao salvar foto:");
            e.printStackTrace();
        }
    }
}