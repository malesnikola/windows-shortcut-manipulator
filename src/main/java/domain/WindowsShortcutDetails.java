package main.java.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WindowsShortcutDetails {
    private final StringProperty shortcutFilePath;      // file path for ".lnk" file
    private final StringProperty originalFilePath;      // file path of original file
    private final StringProperty availability;
    private final StringProperty lastAction;

    private WindowsShortcutDetails(String shortcutFilePath, String originalFilePath, String availability, String lastAction){
        this.shortcutFilePath = new SimpleStringProperty(shortcutFilePath);
        this.originalFilePath = new SimpleStringProperty(originalFilePath);
        this.availability = new SimpleStringProperty(availability);
        this.lastAction = new SimpleStringProperty(lastAction);
    }

    public static WindowsShortcutDetails deserialize(WindowsShortcutWrapper windowsShortcutWrapper) {
        String shortcutPath = windowsShortcutWrapper.getFilePath();
        String originalPath = windowsShortcutWrapper.getRealFilename();
        String fileState = windowsShortcutWrapper.getFileState().toString();
        String shortcutActionState = windowsShortcutWrapper.getShortcutActionState().toString();
        return new WindowsShortcutDetails(shortcutPath, originalPath, fileState, shortcutActionState);
    }

    public String getShortcutFilePath() {
        return shortcutFilePath.get();
    }

    public StringProperty shortcutFilePathProperty() {
        return shortcutFilePath;
    }

    public void setShortcutFilePath(String shortcutFilePath) {
        this.shortcutFilePath.set(shortcutFilePath);
    }

    public String getOriginalFilePath() {
        return originalFilePath.get();
    }

    public StringProperty originalFilePathProperty() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath.set(originalFilePath);
    }

    public String getAvailability() {
        return availability.get();
    }

    public StringProperty availabilityProperty() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability.set(availability);
    }

    public String getLastAction() {
        return lastAction.get();
    }

    public StringProperty lastActionProperty() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction.set(lastAction);
    }
}
