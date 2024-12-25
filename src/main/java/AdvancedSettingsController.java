import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AdvancedSettingsController {

    @FXML
    private Button cancelButton;

    @FXML
    private void onApply() {
        // Логика для кнопки "Apply"
        System.out.println("Apply clicked");
    }

    @FXML
    private void onClose() {
        // Закрыть текущее окно
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void handleAccept(ActionEvent actionEvent) {
        System.out.println("accept");
    }

    public void handleCancel(ActionEvent actionEvent) {
        // Получаем текущее окно через источник события
        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        // Закрываем окно
        stage.close();
    }
}
