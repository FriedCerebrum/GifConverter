public class SearchResult {
    private final String foundPath;
    private final int fileCount;
    private final double durationSeconds;

    // Конструктор
    public SearchResult(String foundPath, int fileCount, double durationSeconds) {
        this.foundPath = foundPath;
        this.fileCount = fileCount;
        this.durationSeconds = durationSeconds;
    }

    // Геттеры
    public String getFoundPath() {
        return foundPath;
    }

    public int getFileCount() {
        return fileCount;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }
}
