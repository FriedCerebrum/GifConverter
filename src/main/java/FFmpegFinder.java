import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FFmpegFinder {

    public static void main(String[] args) {
        try {
            // Запуск поиска по C:\
            Path startPath = Paths.get("C:/");
            long startTime = System.nanoTime(); // Засекаем время начала
            int[] fileCount = {0}; // Счётчик проверенных файлов (оборачиваем в массив для изменения в лямбда-выражении)

            // Поиск файла с использованием Files.walkFileTree
            findFFmpeg(startPath, fileCount);

            // Засекаем время окончания
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Время в миллисекундах
            System.out.println("Поиск завершен. Проверено файлов: " + fileCount[0]);
            System.out.println("Время поиска: " + duration + " миллисекунд.");

        } catch (IOException e) {
            System.err.println("Ошибка при обходе файловой системы: " + e.getMessage());
        }
    }

    public static void findFFmpeg(Path startPath, int[] fileCount) throws IOException {
        // Используем Files.walk() для обхода всех файлов и директорий
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Увеличиваем счётчик проверенных файлов
                fileCount[0]++;

                // Если файл называется ffmpeg.exe, выводим его путь
                if (file.getFileName().toString().equalsIgnoreCase("ffmpeg.exe")) {
                    System.out.println("Найден ffmpeg.exe: " + file.toString());
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
    }
}
