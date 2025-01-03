import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import service.GopiApiService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final boolean LOG_ENABLED = true;
    public static File lastGif;
    public static boolean isLastGifUploaded;

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
    private Button advancedSettingsButton;

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

    @FXML
    private ImageView videoThumbnail;

    private GopiApiService apiService;

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

        for (Button button : new Button[]{top_right_button, top_left_button, bot_right_button, bot_left_button}) {
            button.setOnMousePressed(event -> button.setTranslateY(3));
            button.setOnMouseReleased(event -> button.setTranslateY(0)); // очевидное
        }

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

    @FXML
    public void onAdvancedSettings(ActionEvent event) {
        try {
            // Загружаем FXML для нового окна
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Advanced.fxml"));
            Parent root = loader.load();

            // Создаем новое окно
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Модальное окно
            stage.initStyle(StageStyle.TRANSPARENT); // Убираем рамку и делаем фон прозрачным

            // Создаем сцену с прозрачным фоном
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Устанавливаем прозрачный цвет фона

            // Устанавливаем сцену и показываем окно
            stage.setScene(scene);
            stage.showAndWait(); // Ждем закрытия окна
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        lastGif = new File(newPath);
        GifMaker.makeGifOutVideo(ffmpeg, selectedFile, lastGif, quality, fps);
        if (lastGif.exists() && !isLastGifUploaded){
            uploadButton.setDisable(false);
        }

        if (LOG_ENABLED) LOGGER.log(Level.INFO, "GIF создан: " + lastGif.getAbsolutePath());
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

        if (LOG_ENABLED) {
            LOGGER.log(Level.INFO, "Application closed");
        }

        Platform.exit();
        System.exit(0);
    }

    @FXML
    void blb(ActionEvent event) {
        if (LOG_ENABLED) LOGGER.log(Level.INFO, "Bottom left button clicked");
    }

    @FXML
    void brb(ActionEvent event) throws IOException {
        chooseFile();
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
        if (lastGif == null || !lastGif.exists() ) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please render a GIF first!");
            return;
        }
        if (isLastGifUploaded){
            showAlert(Alert.AlertType.INFORMATION, "GIF already uploaded!", "You have already uploaded this gif to the server.");
            return;
        }

        String gifPath = selectedFile.getAbsolutePath().substring(0, selectedFile.getAbsolutePath().lastIndexOf('.')) + ".gif";
        File gifFile = new File(gifPath);

        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Uploading GIF to server...");
                apiService.uploadGif(gifFile)
                        .thenAccept(response -> {
                            Platform.runLater(() -> {
                                isLastGifUploaded = true;
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

    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv", "*.mov")
        );

        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            // Отображаем название файла
            Text titleText = new Text(selectedFile.getName());
            titleText.setFill(Color.WHITE);
            titleText.setFont(Font.font("System", FontWeight.NORMAL, 12)); // Устанавливаем размер шрифта 12
            gif_title.getChildren().clear();
            gif_title.getChildren().add(titleText);

            // Показываем временную заглушку пока создается превью
            Image placeholder = new Image(getClass().getResourceAsStream("video-placeholder.png"));
            imagePreview.setImage(placeholder);

            // Создаем превью в отдельном потоке
            Task<Void> thumbnailTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (ffmpeg != null) {
                        // Создаем временный файл для превью
                        String thumbnailPath = System.getProperty("java.io.tmpdir") + "temp_thumb_" + 
                                             System.currentTimeMillis() + ".jpg";
                        File thumbnailFile = new File(thumbnailPath);
                        
                        try {
                            // Оптимизированная команда для быстрого извлечения кадра
                            ProcessBuilder pb = new ProcessBuilder(
                                ffmpeg,
                                "-ss", "0", // Перемещаем -ss перед -i для быстрого поиска
                                "-i", selectedFile.getAbsolutePath(),
                                "-vframes", "1",
                                "-an",
                                "-s", "264x372",
                                "-f", "image2", // Принудительно указываем формат
                                "-q:v", "2", // Высокое качество (2-31, где 2 - лучшее)
                                "-y", // Перезаписывать файл без вопросов
                                thumbnailPath
                            );
                            
                            Process p = pb.start();
                            p.waitFor(3, TimeUnit.SECONDS); // Уменьшаем время ожидания

                            // Загружаем и отображаем превью в UI потоке
                            if (thumbnailFile.exists()) {
                                Image thumbnail = new Image(thumbnailFile.toURI().toString());
                                Platform.runLater(() -> imagePreview.setImage(thumbnail));
                            }
                        } finally {
                            // Удаляем временный файл
                            thumbnailFile.delete();
                        }
                    }
                    return null;
                }
            };

            // Обработка ошибок
            thumbnailTask.setOnFailed(e -> {
                LOGGER.log(Level.SEVERE, "Error creating video preview: " + 
                          thumbnailTask.getException().getMessage());
                // В случае ошибки показываем стандартное изображение
                Platform.runLater(() -> {
                    Image defaultImage = new Image(getClass().getResourceAsStream("draganddrop.png"));
                    imagePreview.setImage(defaultImage);
                });
            });

            // Запускаем задачу в пуле потоков
            executor.submit(thumbnailTask);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}
