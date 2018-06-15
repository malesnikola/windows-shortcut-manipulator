package main.java.domain;

public class DuplicateFileInfo {
    private String shortcutFilePath;
    private String originalFilePath;

    public DuplicateFileInfo() {
    }

    public DuplicateFileInfo(String shortcutFilePath, String originalFilePath) {
        this.shortcutFilePath = shortcutFilePath;
        this.originalFilePath = originalFilePath;
    }

    public String getShortcutFilePath() {
        return shortcutFilePath;
    }

    public void setShortcutFilePath(String shortcutFilePath) {
        this.shortcutFilePath = shortcutFilePath;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }
}
