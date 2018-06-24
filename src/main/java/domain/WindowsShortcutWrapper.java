package main.java.domain;

import main.java.enums.FileState;
import main.java.enums.ShortcutActionState;
import main.java.mslinks.mslinks.ShellLink;
import main.java.mslinks.mslinks.ShellLinkException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class WindowsShortcutWrapper {

    private ShellLink shellLink;
    private String fileName;
    private String filePath;
    private String targetFilePath;
    private FileState fileState;
    private ShortcutActionState shortcutActionState;

    public WindowsShortcutWrapper(File file) throws IOException, ParseException, ShellLinkException {
        shellLink = new ShellLink(file);
        this.targetFilePath = shellLink.getLinkInfo().getLocalBasePath();
        this.filePath = file.getPath();
        this.fileName = file.getName();
        shortcutActionState = ShortcutActionState.NONE;
        fileState = FileState.UNKNOWN;
        // little hack:
        //  because my file names is serbian latin (best charset for that is "windows-1250"),
        //  and some of my last folders begin with 'Ω' (because I always want for them to be at the end).
        //  I couldn't find solution how to read serbian latin character with 'Ω' character, so I manually changed all '?' with 'Ω'
//        if (real_file.contains("?")) {
//            real_file = real_file.replaceAll("\\?", "Ω");
//        }
    }

    public WindowsShortcutWrapper(String filePath) throws IOException, ParseException, ShellLinkException {
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

    public void setTargetFilePath(String targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public String getTargetFilePath() {return targetFilePath; }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
}
