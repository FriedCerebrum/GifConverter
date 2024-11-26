import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FFmpegExample {
    private static final Logger logger = Logger.getLogger(FFmpegExample.class.getName());

    public static void main(String[] args) {
        String inputPath = "C:\\Games\\Monke.mp4";
        String tempVideoPath = "C:\\Games\\Monke_temp.mp4"; // Промежуточное упрощённое видео
        String palettePath = "C:\\Games\\palette.png";      // Временный файл для палитры
        String outputGifPath = "C:\\Games\\Monke.gif";      // Финальный GIF

// Этап 1: Конвертация видео в низкое качество
        String[] simplifyVideoCommand = {
                "ffmpeg",
                "-i", inputPath,
                "-vf", "fps=10,scale=180:-2", // Снижение FPS и уменьшение разрешения
                "-b:v", "150k",              // Ещё больше уменьшение битрейта
                "-preset", "ultrafast",      // Быстрая конвертация
                "-y", tempVideoPath          // Промежуточное видео
        };

// Этап 2: Генерация палитры из упрощённого видео
        String[] paletteCommand = {
                "ffmpeg",
                "-i", tempVideoPath,
                "-vf", "palettegen=max_colors=16", // Уменьшение цветов до 16
                "-y", palettePath
        };

// Этап 3: Создание GIF с использованием палитры
        String[] gifCommand = {
                "ffmpeg",
                "-i", tempVideoPath,
                "-i", palettePath,
                "-lavfi", "fps=10,scale=180:-2 [x]; [x][1:v] paletteuse=dither=none", // Отключение дизеринга
                "-y", outputGifPath
        };


        try {
            // Конвертация в упрощённое видео
            logger.log(Level.INFO, "Упрощение видео...");
            executeCommand(simplifyVideoCommand);

            // Генерация палитры
            logger.log(Level.INFO, "Генерация палитры...");
            executeCommand(paletteCommand);

            // Создание GIF
            logger.log(Level.INFO, "Создание GIF...");
            executeCommand(gifCommand);

            logger.log(Level.INFO, "Конвертация завершена успешно! Финальный GIF создан: " + outputGifPath);
        } catch (IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Произошла ошибка при выполнении команды FFmpeg", e);
        }
    }

    /**
     * Вспомогательный метод для выполнения команды ProcessBuilder.
     *
     * @param command Команда для выполнения
     * @throws IOException          Если произошла ошибка ввода/вывода
     * @throws InterruptedException Если процесс был прерван
     */
    private static void executeCommand(String[] command) throws IOException, InterruptedException {
        System.out.println(Arrays.toString(command));
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true); // Объединяем вывод ошибок и обычный вывод
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Выводим лог процесса в консоль
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Процесс завершился с ошибкой. Код выхода: " + exitCode);
        }
    }
}
