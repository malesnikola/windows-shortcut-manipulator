package main.java.domain;

import main.java.enums.FileState;
import main.java.enums.ShortcutActionState;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class WindowsShortcutWrapper extends WindowsShortcut {

    private String filePath;
    private FileState fileState;
    private ShortcutActionState shortcutActionState;

    public WindowsShortcutWrapper(File file) throws IOException, ParseException {
        super(file);
        shortcutActionState = ShortcutActionState.NONE;
        fileState = FileState.UNKNOWN;
        this.filePath = file.getPath();
    }

    public WindowsShortcutWrapper(String filePath) throws IOException, ParseException {
        this(new File(filePath));
    }

    public FileState getFileState() {
        return fileState;
    }

    public void setFileState(FileState fileState) {
        this.fileState = fileState;
    }

    public ShortcutActionState getShortcutActionState() {
        return shortcutActionState;
    }

    public void setShortcutActionState(ShortcutActionState shortcutActionState) {
        this.shortcutActionState = shortcutActionState;
    }

    public String getFilePath() {
        return filePath;
    }
}
