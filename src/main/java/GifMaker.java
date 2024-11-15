import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.bramp.ffmpeg.FFmpegUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GifMaker {

    /**
     * Создает GIF-анимацию из указанного видеофайла с заданным качеством и частотой кадров, добавляя логирование прогресса.
     *
     * @param ffmpeg  экземпляр FFmpeg, инициализированный с путем к ffmpeg.exe
     * @param ffprobe экземпляр FFprobe, инициализированный с путем к ffprobe.exe
     * @param video   файл исходного видео, из которого будет создан GIF
     * @param gif     файл для сохранения результирующего GIF
     * @param quality качество GIF (чем ниже значение, тем выше качество; обычно от 1 до 31)
     * @param fps     частота кадров для GIF (количество кадров в секунду)
     * @throws IOException если возникают ошибки при чтении видео или записи GIF
     */
    public static void makeGifOutVideo(FFmpeg ffmpeg, FFprobe ffprobe, File video, File gif, int quality, int fps) throws IOException {
        Logger logger = Logger.getLogger("GifMaker");
        if (Main.LOG_ENABLED) logger.log(Level.INFO, "Начало конвертации видео в GIF...");

        // Получаем информацию о видео с помощью ffprobe
        FFmpegProbeResult probeResult = ffprobe.probe(video.getAbsolutePath());
        final double durationNs = probeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

        // Если выходной файл имеет расширение .mp4, заменяем его на .gif
        String gifPath = gif.getAbsolutePath().replaceFirst("\\.mp4$", ".gif");

        // Создаем команду для ffmpeg
        FFmpegBuilder builder = new FFmpegBuilder()
                .addInput(video.getAbsolutePath()) // Входное видео
                .overrideOutputFiles(true) // Перезаписываем выходной файл
                .addOutput(gifPath) // Выходной файл GIF
                .setFormat("gif") // Устанавливаем формат
                .addExtraArgs("-vf", String.format("fps=%d,scale=240:-1:flags=lanczos", fps)) // Устанавливаем FPS и масштаб
                .addExtraArgs("-q:v", String.valueOf(quality)) // Устанавливаем качество
                .done();

        // Выполняем команду с отслеживанием прогресса
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                double percentage = (double) progress.out_time_ns / durationNs * 100;

                // Логируем информацию о прогрессе
                if (Main.LOG_ENABLED) logger.log(Level.INFO, String.format(
                        "[%.0f%%] Кадр: %d, FPS: %.0f, Скорость: %.2fx",
                        percentage,
                        progress.frame,
                        progress.fps.doubleValue(),
                        progress.speed
                ));
            }
        });

        // Запускаем выполнение
        job.run();
        if (Main.LOG_ENABLED) logger.log(Level.INFO, "Конвертация завершена! GIF создан: " + gifPath);
    }
}
