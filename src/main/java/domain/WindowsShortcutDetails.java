package main.java.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import main.java.model.WindowsShortcutWrapper;

/**
 * This class is used to hold data for table view.
 */
public class WindowsShortcutDetails {
    private final StringProperty shortcutFilePath;      // file path for ".lnk" file
    private final StringProperty originalFilePath;      // file path of original file
    private final StringProperty availability;          // availability of file
    private final StringProperty lastAction;            // last user action on file

    private WindowsShortcutDetails(String shortcutFilePath, String originalFilePath, String availability, String lastAction){
        this.shortcutFilePath = new SimpleStringProperty(shortcutFilePath);
        this.originalFilePath = new SimpleStringProperty(originalFilePath);
        this.availability = new SimpleStringProperty(availability);
        this.lastAction = new SimpleStringProperty(lastAction);
    }

    public static WindowsShortcutDetails deserialize(WindowsShortcutWrapper windowsShortcutWrapper) {
        String shortcutPath = windowsShortcutWrapper.getFilePath();
        String originalPath = windowsShortcutWrapper.getTargetFilePath();
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
