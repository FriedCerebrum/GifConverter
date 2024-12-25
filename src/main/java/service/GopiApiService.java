package service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private void uploadFile(String filePath) throws IOException {
        String url = API_BASE_URL + "/upload";
        String boundary = Long.toHexString(System.currentTimeMillis());
        String CRLF = "\r\n";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {

            Path path = new File(filePath).toPath();
            String mimeType = Files.probeContentType(path);
            String fileName = path.getFileName().toString();

            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(fileName).append("\"").append(CRLF);
            writer.append("Content-Type: ").append(mimeType).append(CRLF);
            writer.append(CRLF).flush();

            Files.copy(path, output);
            output.flush();

            writer.append(CRLF).append("--").append(boundary).append("--").append(CRLF).flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned error code: " + responseCode);
        }
    }
}
