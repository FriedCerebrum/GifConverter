package service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GopiApiService {
    private static final String API_BASE_URL = "http://localhost:1111";

    public CompletableFuture<String> uploadGif(File gifFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                uploadFile(gifFile.getAbsolutePath());
                return "File successfully uploaded to server.";
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload GIF: " + e.getMessage(), e);
            }
        });
    }

    public static void uploadFile(String filePath) throws IOException {
        // Проверяем, что файл существует и это GIF
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + filePath);
        }
        if (!filePath.toLowerCase().endsWith(".gif")) {
            throw new IOException("Only GIF files are supported. File: " + filePath);
        }

        // Проверяем доступность сервера
        if (!isServerAvailable()) {
            throw new IOException("Server is not available at " + API_BASE_URL);
        }

        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add("--connect-timeout");
        command.add("5"); // Таймаут подключения 5 секунд
        command.add("-X");
        command.add("POST");
        command.add(API_BASE_URL + "/save");
        command.add("-F");
        // Кодируем имя файла для безопасной передачи
        String encodedPath = URLEncoder.encode(file.getAbsolutePath(), StandardCharsets.UTF_8);
        command.add("file=@" + file.getAbsolutePath());

        // Вывод команды для отладки
        System.out.println("Executing command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                System.out.println(line); // Выводим ответ в реальном времени
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Curl command failed with exit code " + exitCode + "\nResponse: " + response);
            }
            
            System.out.println("Server response: " + response);
            System.out.println("File successfully uploaded to server.");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload was interrupted", e);
        }
    }

    private static boolean isServerAvailable() {
        try {
            List<String> command = new ArrayList<>();
            command.add("curl");
            command.add("--connect-timeout");
            command.add("2");
            command.add("-s"); // Silent mode
            command.add("-o");
            command.add("NUL"); // Отбрасываем вывод на Windows
            command.add(API_BASE_URL);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<String> listGifs() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return listFiles();
            } catch (IOException e) {
                throw new RuntimeException("Failed to list GIFs: " + e.getMessage(), e);
            }
        });
    }

    public static String listFiles() throws IOException {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add(API_BASE_URL + "/gifs");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Curl command failed with exit code " + exitCode);
            }

            return response.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("List request was interrupted", e);
        }
    }

    public CompletableFuture<byte[]> getGif(String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getFile(id);
            } catch (IOException e) {
                throw new RuntimeException("Failed to get GIF: " + e.getMessage(), e);
            }
        });
    }

    public static byte[] getFile(String id) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add(API_BASE_URL + "/gif/" + id);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try {
            byte[] response = process.getInputStream().readAllBytes();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new IOException("Curl command failed with exit code " + exitCode);
            }

            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download was interrupted", e);
        }
    }
}
