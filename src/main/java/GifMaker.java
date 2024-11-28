import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GifMaker {

    private static double totalDuration = 0.0; // Общая продолжительность в секундах

    /**
     * Создает GIF-анимацию из указанного видеофайла с заданным качеством и частотой кадров, добавляя логирование прогресса.
     *
     * @param ffmpeg  абсолютный путь до ffmpeg.exe
     * @param video   файл исходного видео, из которого будет создан GIF
     * @param gif     файл для сохранения результирующего GIF
     * @param quality качество GIF (чем ниже значение, тем выше качество; обычно от 1 до 100)
     * @param fps     частота кадров для GIF (количество кадров в секунду)
     * @throws IOException если возникают ошибки при чтении видео или записи GIF
     */
    public static void makeGifOutVideo(String ffmpeg, File video, File gif, int quality, int fps) throws IOException {
        String[] commands = {
                ffmpeg,
                "-i", video.getAbsolutePath(),
                "-y",
                "-filter_complex", String.format("fps=%d,scale=160:-2", fps),
                "-c:v", "gif",
                "-q:v", String.valueOf(quality),
                "-pix_fmt", "rgb8",
                "-progress", "-",
                "-nostats",
                gif.getAbsolutePath()
        }; // ffmpeg -i input.mp4 -filter_complex "fps=5,scale=160:-1" -c:v gif -q:v 100 -pix_fmt rgb8 output.gif справка

        try {
            System.out.println(String.join(" ", commands));

            // Получаем путь к ffprobe, предполагая, что он находится в той же директории, что и ffmpeg
            String ffprobe = new File(ffmpeg).getParent() + File.separator + "ffprobe.exe";

            // Получаем общую продолжительность видео
            totalDuration = getMediaDuration(ffprobe, video.getAbsolutePath());
            System.out.printf("Общая продолжительность файла: %.2f сек%n", totalDuration);

            executeCommand(commands);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Вспомогательный метод для выполнения команды ProcessBuilder.
     *
     * @param args Команда для выполнения
     * @throws IOException          Если произошла ошибка ввода/вывода
     * @throws InterruptedException Если процесс был прерван
     */
    private static void executeCommand(String[] args) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseProgress(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Error, exit code: " + exitCode);
        }
    }

    /**
     * Метод для парсинга строки прогресса и вычисления процента завершения.
     *
     * @param line Строка вывода FFmpeg
     */
    private static void parseProgress(String line) {
        if (line.startsWith("out_time_ms=")) {
            String timeMsStr = line.substring("out_time_ms=".length()).trim();
            long timeMs = Long.parseLong(timeMsStr);
            double timeSec = timeMs / 1_000_000.0; // Здесь делим на 10 миллионов для получения секунд
            double percentage = calculatePercentage(timeSec, totalDuration);
            System.out.printf("Прогресс: %.2f сек (%.2f%%)%n", timeSec, percentage);
        } else if (line.startsWith("progress=")) {
            String progress = line.substring("progress=".length()).trim();
            if (progress.equals("end")) {
                System.out.println("Конвертация завершена.");
            }
        }
    }

    /**
     * Метод для вычисления процента завершения.
     *
     * @param currentTime   Текущее время прогресса в секундах
     * @param totalDuration Общая продолжительность видео в секундах
     * @return Процент завершения
     */
    private static double calculatePercentage(double currentTime, double totalDuration) {
        if (totalDuration > 0) {
            return (currentTime / totalDuration) * 100.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Метод для получения общей продолжительности видеофайла с помощью ffprobe.
     *
     * @param ffprobe        абсолютный путь до ffprobe.exe
     * @param inputFilePath  путь до видеофайла
     * @return Общая продолжительность видео в секундах
     */
    private static double getMediaDuration(String ffprobe, String inputFilePath) {
        double duration = 0.0;
        try {
            String[] command = {
                    ffprobe,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    inputFilePath
            };
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                duration = Double.parseDouble(line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return duration;
    }
}
