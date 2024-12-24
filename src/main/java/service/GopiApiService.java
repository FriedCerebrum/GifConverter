package service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class GopiApiService {
    private static final String API_BASE_URL = "http://localhost:1111";
    private final HttpClient httpClient;

    public GopiApiService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public CompletableFuture<String> uploadGif(File gifFile) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(gifFile.toPath());
        
        String lineBreak = "\r\n";
        String prefix = "--" + boundary + lineBreak +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + gifFile.getName() + "\"" + lineBreak +
                "Content-Type: image/gif" + lineBreak + lineBreak;
        String suffix = lineBreak + "--" + boundary + "--" + lineBreak;

        byte[] prefixBytes = prefix.getBytes();
        byte[] suffixBytes = suffix.getBytes();

        byte[] requestBody = new byte[prefixBytes.length + fileBytes.length + suffixBytes.length];
        System.arraycopy(prefixBytes, 0, requestBody, 0, prefixBytes.length);
        System.arraycopy(fileBytes, 0, requestBody, prefixBytes.length, fileBytes.length);
        System.arraycopy(suffixBytes, 0, requestBody, prefixBytes.length + fileBytes.length, suffixBytes.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/save"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<String> listGifs() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/gifs"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<byte[]> getGif(String id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/gif/" + id))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body);
    }
}
