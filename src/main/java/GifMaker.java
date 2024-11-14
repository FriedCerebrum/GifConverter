import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.bramp.ffmpeg.FFmpegUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
        System.out.println("Начало конвертации видео в GIF...");

        // Пробуем получить продолжительность видео с помощью ffprobe
        net.bramp.ffmpeg.probe.FFmpegProbeResult probeResult = ffprobe.probe(video.getAbsolutePath());
        final double durationNs = probeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

        // Создаем команду для ffmpeg
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(probeResult)
                .addOutput(gif.getAbsolutePath())
                .addExtraArgs(
                        "-vf", "fps=" + fps + ",scale=240:-1:flags=lanczos", // Уменьшаем разрешение до 240 пикселей
                        "-q:v", String.valueOf(quality)                        // Качество GIF
                )
                .setFormat("gif")
                .done();

        // Выполняем команду с отслеживанием прогресса
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                double percentage = (double) progress.out_time_ns / durationNs * 100;

                // Логируем информацию о прогрессе
                System.out.println(String.format(
                        "[%.0f%%] Статус: %s, Кадр: %d, Время: %s, FPS: %.0f, Скорость: %.2fx",
                        percentage,
                        progress.status,
                        progress.frame,
                        FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
                        progress.fps.doubleValue(),
                        progress.speed
                ));
            }
        });

        // Запускаем выполнение
        job.run();
        System.out.println("Конвертация завершена! GIF создан: " + gif.getAbsolutePath());
    }
}
