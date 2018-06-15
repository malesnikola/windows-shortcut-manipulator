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
        // little hack:
        //  because my file names is serbian latin (best charset for that is "windows-1250"),
        //  and some of my last folders begin with 'Ω' (because I always want for them to be at the end).
        //  I couldn't find solution how to read serbian latin character with 'Ω' character, so I manually changed all '?' with 'Ω'
        if (real_file.contains("?")) {
            real_file = real_file.replaceAll("\\?", "Ω");
        }
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
