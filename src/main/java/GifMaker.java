import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.bramp.ffmpeg.FFmpegUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GifMaker {

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
                "-filter_complex", String.format("fps=%d,scale=180:-2", fps),
                "-c:v", "gif",
                "-q:v", String.valueOf(quality),
                "-pix_fmt", "rgb8",
                "-dither bayer", gif.getAbsolutePath()
        }; // ffmpeg -i input.mp4 -filter_complex "fps=5,scale=160:-1" -c:v gif -q:v 100 -pix_fmt rgb8 -dither bayer output.gif

        try {
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
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Error, exit code: " + exitCode);
        }
    }
}
