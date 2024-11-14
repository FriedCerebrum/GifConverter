import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicReference;

public class Finder {

    public static void main(String[] args) {
        try {
            // Укажите начальную директорию для поиска
            Path startPath = Paths.get("C:/");
            // Укажите имя файла для поиска
            String fileNameToFind = "ffmpeg.exe";

            // Вызов функции поиска с передачей необходимых параметров
            SearchResult result = findFile(startPath, fileNameToFind);

            // Вывод результатов
            System.out.println("Поиск завершен.");
            System.out.println("Проверено файлов: " + result.getFileCount());
            System.out.printf("Время поиска: %.3f секунд.%n", result.getDurationSeconds());

            if (result.getFoundPath() != null) {
                System.out.println("Полный путь до файла: " + result.getFoundPath());
            } else {
                System.out.println("Файл " + fileNameToFind + " не найден.");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при обходе файловой системы: " + e.getMessage());
        }
    }

    /**
     * Функция для поиска файла с заданным именем, начиная с указанной директории.
     *
     * @param startPath      Начальная директория для поиска.
     * @param fileNameToFind Имя файла, который необходимо найти.
     * @return Объект SearchResult, содержащий результаты поиска.
     * @throws IOException В случае ошибки ввода-вывода.
     */
    public static SearchResult findFile(Path startPath, String fileNameToFind) throws IOException {
        // Засекаем время начала в наносекундах
        long startTime = System.nanoTime();
        // Счётчик проверенных файлов
        int[] fileCount = {0};
        // Используем AtomicReference для хранения найденного пути
        AtomicReference<String> foundPath = new AtomicReference<>(null);

        // Используем Files.walkFileTree для обхода всех файлов и директорий
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Увеличиваем счётчик проверенных файлов
                fileCount[0]++;

                // Если имя файла совпадает с искомым (без учета регистра)
                if (file.getFileName().toString().equalsIgnoreCase(fileNameToFind)) {
                    foundPath.set(file.toAbsolutePath().toString());
                    System.out.println("Найден " + fileNameToFind + ": " + foundPath.get());
                    return FileVisitResult.TERMINATE; // Прерываем обход после нахождения файла
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // Пропускаем ошибки, например, если нет прав доступа
                return FileVisitResult.CONTINUE;
            }
        });

        // Засекаем время окончания в наносекундах
        long endTime = System.nanoTime();
        // Вычисляем длительность в секундах с точностью до 3 знаков после запятой
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;

        // Создаём объект SearchResult с результатами поиска
        return new SearchResult(foundPath.get(), fileCount[0], durationSeconds);
    }
}
