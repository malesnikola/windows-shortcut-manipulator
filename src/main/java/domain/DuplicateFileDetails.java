package main.java.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;

public class DuplicateFileDetails {
    private final StringProperty shortcutFilePath;      // file path for ".lnk" file
    private final StringProperty originalFilePath;      // file path of original file
    private BooleanProperty isSelected;

    public DuplicateFileDetails(String shortcutFilePath, String originalFilePath, boolean isSelected) {
        this.shortcutFilePath = new SimpleStringProperty(shortcutFilePath);
        this.originalFilePath = new SimpleStringProperty(originalFilePath);
        this.isSelected = new SimpleBooleanProperty(isSelected);
    }

    public static DuplicateFileDetails deserialize(DuplicateFileInfo duplicateFileInfo) {
        return new DuplicateFileDetails(duplicateFileInfo.getShortcutFilePath(), duplicateFileInfo.getOriginalFilePath(), false);
    }

    public static DuplicateFileDetails deserialize(DuplicateFileInfo duplicateFileInfo, boolean isSelected) {
        return new DuplicateFileDetails(duplicateFileInfo.getShortcutFilePath(), duplicateFileInfo.getOriginalFilePath(), isSelected);
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

    public boolean isIsSelected() {
        return isSelected.get();
    }

    public BooleanProperty isSelectedProperty() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected.set(isSelected);
    }
}
