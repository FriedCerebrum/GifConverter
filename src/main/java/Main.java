import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Main extends Application {
    Stage primaryStage;
    String ffmpegPath;
    String ffprobePath;

    File selectedFile;
    private double x = 0, y = 0;
    @FXML
    private Pane top_pane;
    @FXML
    private Button close_btn;
    @FXML
    private Rectangle rect;

    @FXML
    private Button bot_left_button;

    @FXML
    private Button bot_right_button;

    @FXML
    private TextFlow gif_title;

    @FXML
    private Button top_left_button;

    @FXML
    private Button top_right_button;

    @FXML
    private ImageView image_tr;
    @FXML
    private Button hide_button;
    @FXML
    private Slider slider1;
    @FXML
    private Slider slider2;
    @FXML
    private Slider slider3;

    @FXML
    private Button roundButton1;

    @FXML
    private Button roundButton2;

    @FXML
    private Button roundButton3;
    @FXML
    private Button renderButton;
    @FXML
    void onRenderButton(ActionEvent event) throws IOException {
        System.out.println("FPS: " + slider1.getValue());
        System.out.println("Bitrate: " + slider2.getValue());
        System.out.println("Quality: " + slider3.getValue());

        AtomicInteger atomicInteger = new AtomicInteger();
        AtomicInteger atomicInteger1 = new AtomicInteger();

        if (ffmpegPath == null) {
            SearchResult ffmpegSearchResult = Finder.findFile(Path.of("C:/"), "ffmpeg.exe");
            FFmpeg fFmpeg = new FFmpeg(ffmpegSearchResult.getFoundPath());
            ffmpegPath = ffmpegSearchResult.getFoundPath();
            String prettyPrint1 = String.format("%.2f секунд для ffmpeg", ffmpegSearchResult.getDurationSeconds());
            System.out.println(prettyPrint1);
        } else{
            System.out.println(ffmpegPath);
        }
        if (ffprobePath == null) {
            SearchResult ffprobeSearchResult = Finder.findFile(Path.of("C:/"), "ffprobe.exe");
            FFprobe fFprobe = new FFprobe(ffprobeSearchResult.getFoundPath());
            ffprobePath = ffprobeSearchResult.getFoundPath();
            String prettyPrint2 = String.format("%.2f секунд для ffprobe", ffprobeSearchResult.getDurationSeconds());
            System.out.println(prettyPrint2);
        } else{
            System.out.println(ffprobePath);
        }
        if (selectedFile == null) {
            System.out.println("Selected file is null");
        } else{

        }
        FFmpegBuilder paletteBuilder = new FFmpegBuilder()
                .setInput(inputVideoPath)
                .addOutput("pallete.png")
                .setFormat("png") // Формат палитры
                .setVideoFilter(String.format("fps=%d,palettegen=max_colors=%d", slider1.getValue(), 10))
                .disableAudio() // Отключение аудио (GIF не поддерживает аудио)
                .setOverwriteOutput(true) // Перезапись существующего файла палитры
                .done();
    }
    @FXML
    void onHideButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);

        System.out.println("Window is hidden");
    }

    @FXML
    void onCloseAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    void blb(ActionEvent event) {

    }

    @FXML
    void brb(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Изображения (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        selectedFile = fileChooser.showOpenDialog(bot_left_button.getScene().getWindow());

        if (selectedFile != null) {
            System.out.println("Выбран файл: " + selectedFile.getAbsolutePath());

            Image image = ImageResizer.resizeImageInMemory(selectedFile.getAbsolutePath(), 150, 150);

            image_tr.setImage(image);

            Rectangle clip = new Rectangle(0, 0, 150, 150);
            clip.setArcWidth(30);
            clip.setArcHeight(30);

            image_tr.setClip(clip);
        }
    }

    @FXML
    void tlb(ActionEvent event) {

    }

    @FXML
    void trb(ActionEvent event) {

    }
    @FXML
    void slider1OnClicked(MouseEvent event) {
        System.out.println(slider1.getValue());
    }
    @FXML
    private ImageView imagePreview;
    public void initialize() {
        Image label1Image = new Image(String.valueOf(getClass().getResource("fps.png")));
        ImageView label1 = new ImageView(label1Image);
        label1.setFitWidth(35);
        label1.setFitHeight(35);
        label1.setTranslateY(-3);
        roundButton1.setGraphic(label1);

        Image label2Image = new Image(String.valueOf(getClass().getResource("bitrate.png")));
        ImageView label2 = new ImageView(label2Image);
        label2.setFitWidth(35);
        label2.setFitHeight(35);
        roundButton2.setGraphic(label2);

        Image label3Image = new Image(String.valueOf(getClass().getResource("quality.png")));
        ImageView label3 = new ImageView(label3Image);
        label3.setFitWidth(30);
        label3.setFitHeight(30);
        roundButton3.setGraphic(label3);

        Image preview = new Image(String.valueOf(getClass().getResource("draganddrop.png")));
        imagePreview.setImage(preview);

        top_pane.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        top_pane.setOnMouseDragged(event -> {
            Stage stage = (Stage) top_pane.getScene().getWindow();

            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
        });

        hide_button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        hide_button.getStyleClass().add("hide-button");

        close_btn.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        close_btn.getStyleClass().add("close-button");

        rect.setArcHeight(20);
        rect.setArcWidth(20);
        rect.setStroke(Color.GRAY);
        rect.setStrokeWidth(2);
        rect.setFill(Color.TRANSPARENT);

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("mtuci.png")));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        imageView.setSmooth(true);
        top_left_button.setGraphic(imageView);

        gif_title.getStylesheets().add(Objects.requireNonNull(getClass().getResource("big_title.css")).toExternalForm());
        gif_title.getStyleClass().add("large-text");
        Text text = new Text("GIF");
        gif_title.getChildren().add(text);

        image_tr.setSmooth(true);
        image_tr.setFitWidth(150);
        image_tr.setFitHeight(150);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("UI.fxml"));
        Pane root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + e.getMessage());
            return;
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Gif Converter");
        stage.show();
        primaryStage = stage;
    }
}
