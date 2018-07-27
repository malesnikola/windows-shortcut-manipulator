package main.java.util;

import main.java.enums.FileState;

import java.io.File;
import java.io.IOException;

/**
 * Util class for manipulation with files.
 */
public class FileUtil {
    private FileUtil() {
        // prevent instantiation
    }

    public static FileState getFileState(String filePath) {
        return getFileState(new File(filePath));
    }

    /**
     * Get state of file. I.e. AVAILABLE
     * @param file Forwarded file.
     * @return Returns FileState of forwarded file.
     */
    public static FileState getFileState(File file) {
        if (file.exists()) {
            try {
                String caseSensitivePath = file.getCanonicalPath();
                // check if forwarded path is equal with canonical path
                if (file.getPath().equals(caseSensitivePath)) {
                    return FileState.AVAILABLE;
                } else {
                    return FileState.CASE_SENSITIVE;
                }
            } catch (IOException e) {
                return FileState.CASE_SENSITIVE;
            }
        } else {
            return FileState.UNAVAILABLE;
        }
    }

    /**
     * Check if forwarded path representing valid existing folder.
     * @param folderPath Folder path.
     * @return True if path represent valid existing folder, otherwise false.
     */
    public static boolean ifFolderIsValid(String folderPath) {
        File file = new File(folderPath);
        return file.isDirectory();
    }

}
