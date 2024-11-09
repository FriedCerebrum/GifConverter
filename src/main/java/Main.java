import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // установка надписи
        Text text = new Text("Hello METANIT.COM!");
        text.setLayoutY(80);    // установка положения надписи по оси Y
        text.setLayoutX(80);   // установка положения надписи по оси X

        Group group = new Group(text);

        Scene scene = new Scene(group);
        stage.setScene(scene);
        stage.setTitle("JavaFX Application");
        stage.setWidth(300);
        stage.setHeight(250);
        stage.show();
        System.out.println("Пасхалко: ");
        System.out.println("ОС: " + System.getProperty("os.name"));
        System.out.println("Версия ОС: " + System.getProperty("os.version"));
        System.out.println("Архитектура: " + System.getProperty("os.arch"));
        System.out.println("Имя пользователя: " + System.getProperty("user.name"));
        System.out.println("Текущая рабочая директория: " + System.getProperty("user.dir"));

        // Дополнительная информация, если требуется
        System.out.println("Количество процессоров: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Объем доступной памяти (в байтах): " + Runtime.getRuntime().freeMemory());
    }
}
