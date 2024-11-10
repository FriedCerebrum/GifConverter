import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
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
    void blb(ActionEvent event) {

    }

    @FXML
    void brb(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Изображения (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        File selectedFile = fileChooser.showOpenDialog(bot_left_button.getScene().getWindow());

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
    public void initialize() {
        rect.setArcHeight(20);
        rect.setArcWidth(20);
        rect.setStroke(Color.GRAY);
        rect.setStrokeWidth(2);
        rect.setFill(Color.TRANSPARENT);

        top_left_button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("4buttons.css")).toExternalForm());
        top_left_button.getStyleClass().add("button");

        top_right_button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("4buttons.css")).toExternalForm());
        top_right_button.getStyleClass().add("button");

        bot_left_button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("4buttons.css")).toExternalForm());
        bot_left_button.getStyleClass().add("button");

        bot_right_button.getStylesheets().add(Objects.requireNonNull(getClass().getResource("4buttons.css")).toExternalForm());
        bot_right_button.getStyleClass().add("button");

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("mtuci.png")));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        imageView.setSmooth(true);
        top_left_button.setGraphic(imageView);

        top_left_button.setOnMousePressed(event -> imageView.setTranslateY(2));
        top_left_button.setOnMouseReleased(event -> imageView.setTranslateY(0));


        gif_title.getStylesheets().add(getClass().getResource("big_title.css").toExternalForm());
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
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle("Gif Converter");
        stage.show();
    }
}
