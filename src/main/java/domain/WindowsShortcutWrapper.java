package main.java.domain;

import main.java.enums.FileState;
import main.java.enums.ShortcutActionState;
import main.java.mslinks.mslinks.ShellLink;
import main.java.mslinks.mslinks.ShellLinkException;
import main.java.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * This class contains all basic information of shortcut file.
 */
public class WindowsShortcutWrapper {

    private ShellLink shellLink;        // helper shell link
    private String fileName;            // shortcut file name
    private String filePath;            // shortcut file path
    private String targetFilePath;      // target file path
    private FileSize fileSize;          // size of original (targeting) file
    private FileState fileState;        // file state
    private boolean isFolder;           // indicates if shortcut targeting folder
    private int numberOfFiles;          // if shortcut targeting folder this field represent how many files exist below this folder, else this field is 1
    private ShortcutActionState shortcutActionState;    // las user action

    public WindowsShortcutWrapper(File file) throws IOException, ParseException, ShellLinkException {
        shellLink = new ShellLink(file);
        this.targetFilePath = shellLink.getLinkInfo().getLocalBasePath();
        isFolder = FileUtil.ifFolderIsValid(this.targetFilePath);
        this.filePath = file.getPath();
        this.fileName = file.getName();
        updateAvailabilityAndSize();
        shortcutActionState = ShortcutActionState.NONE;
        // little hack (worked previously when I used WindowsShortcut class):
        //  because my file names is serbian latin (best charset for that is "windows-1250"),
        //  and some of my last folders begin with 'Ω' (because I always want for them to be at the end).
        //  I couldn't find solution how to read serbian latin character with 'Ω' character, so I manually changed all '?' with 'Ω'
        //  if (real_file.contains("?")) {
        //      real_file = real_file.replaceAll("\\?", "Ω");
        //  }
    }

    public WindowsShortcutWrapper(String filePath) throws IOException, ParseException, ShellLinkException {
        this(new File(filePath));
    }

    public void updateAvailabilityAndSize() {
        File targetFile = new File(targetFilePath);
        fileState = FileUtil.getFileState(targetFile);
        fileSize = FileSize.getFileSize(targetFile);
        numberOfFiles = FileUtil.getFileCount(targetFile);
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

    public FileSize getFileSize() {
        return fileSize;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }
}
