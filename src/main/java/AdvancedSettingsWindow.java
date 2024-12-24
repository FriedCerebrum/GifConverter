import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AdvancedSettingsWindow {

    public void display() {
        try {
            // Загружаем FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Advanced.fxml"));
            Parent root = loader.load();

            // Создаем новое окно
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Advanced Settings");

            // Устанавливаем сцену
            Scene scene = new Scene(root, 400, 300);
            stage.setScene(scene);

            // Показываем окно
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
