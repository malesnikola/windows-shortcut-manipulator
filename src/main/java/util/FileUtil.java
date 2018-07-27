package main.java.util;

import main.java.enums.FileState;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;

public class FileUtil {
    private FileUtil() {
        // prevent instantiation
    }

    public static FileState getFileState(String filePath) {
        return getFileState(new File(filePath));
    }

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

}
