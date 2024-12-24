import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import service.GopiApiService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final boolean LOG_ENABLED = true;

    Stage primaryStage;

    private volatile String ffmpeg;
    private volatile String ffprobe;

    private ExecutorService executor;

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
    private Button uploadButton;

    private GopiApiService apiService;

    @FXML
    void onRenderButton(ActionEvent event) throws IOException { // кнопка рендера
        if (ffmpeg == null || ffprobe == null) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "FFmpeg или FFprobe не найдены.");
            if (LOG_ENABLED) LOGGER.log(Level.WARNING, "FFmpeg или FFprobe не найдены.");
            return;
        }

        int fps = (int) slider1.getValue();
        int quality = (int) slider3.getValue();

        String newPath = selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().lastIndexOf('.')) + ".gif";

        // Создаем новый файл
        File gif = new File(newPath);
        GifMaker.makeGifOutVideo(ffmpeg, selectedFile, gif, quality, fps);

        if (LOG_ENABLED) LOGGER.log(Level.INFO, "GIF создан: " + gif.getAbsolutePath());
    }

    @FXML
    void onHideButton(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);

        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Window is hidden");
    }

    @FXML
    void onCloseAction(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Application closed");
    }

    @FXML
    void blb(ActionEvent event) {
        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Bottom left button clicked");
    }

    @FXML
    void brb(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Видео", "*.mp4");
        fileChooser.getExtensionFilters().add(imageFilter);

        selectedFile = fileChooser.showOpenDialog(bot_left_button.getScene().getWindow());

        if (selectedFile != null) {
            if (LOG_ENABLED) LOGGER.log(Level.INFO, "Selected file: {0}", selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void tlb(ActionEvent event) {
        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Top left button clicked");
    }

    @FXML
    void trb(ActionEvent event) {
        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Top right button clicked");
    }

    @FXML
    void slider1OnClicked(MouseEvent event) {
        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Slider1 value: {0}", slider1.getValue());
    }

    @FXML
    private ImageView imagePreview;

    public void initialize() {
        apiService = new GopiApiService();
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

     /*   Image gifbutton = new Image(Objects.requireNonNull(getClass().getResourceAsStream("upload.png")));
        ImageView gifButton = new ImageView(gifbutton);
        gifButton.setPreserveRatio(false);
        gifButton.setFitWidth(40);
        gifButton.setFitHeight(40);
        gifButton.setSmooth(true);
        bot_right_button.setGraphic(gifButton);*/


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

        executor = Executors.newFixedThreadPool(2);
        startSearchTasks();
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
            LOGGER.log(Level.SEVERE, "Error loading FXML: {0}", e.getMessage());
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

        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Application started");
    }

    private void startSearchTasks() {
        // Создание задач для поиска ffmpeg и ffprobe
        Task<String> findFfmpegTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                SearchResult result = Finder.findFile(Paths.get("C:/"), "ffmpeg.exe");
                if (result.getFoundPath() != null) {
                    return result.getFoundPath();
                } else {
                    return null;
                }
            }
        };

        Task<String> findFfprobeTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                SearchResult result = Finder.findFile(Paths.get("C:/"), "ffprobe.exe");
                if (result.getFoundPath() != null) {
                    return result.getFoundPath();
                } else {
                    return null;
                }
            }
        };

        // Обработка успешного завершения задачи поиска ffmpeg
        findFfmpegTask.setOnSucceeded(event -> {
            ffmpeg = findFfmpegTask.getValue();
            if (ffmpeg != null) {
                LOGGER.log(Level.INFO, "FFmpeg найден по пути: {0}", ffmpeg);
                // Можно обновить UI, например, показать статус
            } else {
                LOGGER.log(Level.WARNING, "FFmpeg не найден.");
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Предупреждение", "FFmpeg не найден."));
            }
        });

        // Обработка успешного завершения задачи поиска ffprobe
        findFfprobeTask.setOnSucceeded(event -> {
            ffprobe = findFfprobeTask.getValue();
            if (ffprobe != null) {
                LOGGER.log(Level.INFO, "FFprobe найден по пути: {0}", ffprobe);
                // Можно обновить UI, например, показать статус
            } else {
                LOGGER.log(Level.WARNING, "FFprobe не найден.");
                Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Предупреждение", "FFprobe не найден."));
            }
        });

        // Обработка ошибок при поиске ffmpeg
        findFfmpegTask.setOnFailed(event -> {
            Throwable e = findFfmpegTask.getException();
            LOGGER.log(Level.SEVERE, "Ошибка при поиске FFmpeg: {0}", e.getMessage());
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при поиске FFmpeg."));
        });

        // Обработка ошибок при поиске ffprobe
        findFfprobeTask.setOnFailed(event -> {
            Throwable e = findFfprobeTask.getException();
            LOGGER.log(Level.SEVERE, "Ошибка при поиске FFprobe: {0}", e.getMessage());
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Ошибка", "Ошибка при поиске FFprobe."));
        });

        // Запуск задач в ExecutorService
        executor.submit(findFfmpegTask);
        executor.submit(findFfprobeTask);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleUploadToServer() {
        if (selectedFile == null || !selectedFile.exists()) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file first!");
            return;
        }

        // Получаем путь к GIF файлу
        String gifPath = selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().lastIndexOf('.')) + ".gif";
        File gifFile = new File(gifPath);

        if (!gifFile.exists()) {
            showAlert(Alert.AlertType.WARNING, "GIF Not Found", "Please render the GIF first!");
            return;
        }

        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Uploading GIF to server...");
                apiService.uploadGif(gifFile)
                        .thenAccept(response -> {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Upload Success",
                                    "GIF successfully uploaded to server!\nResponse: " + response);
                            });
                        })
                        .exceptionally(throwable -> {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Upload Error",
                                    "Failed to upload GIF: " + throwable.getMessage());
                            });
                            return null;
                        });
                return null;
            }
        };

        executor.submit(uploadTask);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
