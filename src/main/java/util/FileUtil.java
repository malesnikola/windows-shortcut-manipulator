package main.java.util;

import main.java.enums.FileState;
import main.java.workers.CreateCopiesWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * Get size of files in bytes.
     * If forwarded file is real file, this method will return size of that file.
     * If forwarded file is directory, this method will return sum of all files which are underneath that directory (deep summing).
     * @param file Forwarded file.
     * @return Size of files in bytes.
     */
    public static long getFileSizeInBytes(File file) {
        long length = 0;
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                length += getFileSizeInBytes(subFile);
            }
        } else {
            length += file.length();
        }

        return length;
    }

    /**
     * Get number of files.
     * If forwarded file is real file, this method will return one.
     * If forwarded file is directory, this method will return number of files which are underneath that directory (deep counting).
     * @param file Forwarded file.
     * @return Total number of files.
     */
    public static int getFileCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                count += getFileCount(subFile);
            }
        } else {
            count++;
        }

        return count;
    }

    /**
     * Copy folder from sourcePath to destinationPath with all content inside that folder (deep copy)
     * @param sourcePath Path of source folder.
     * @param destinationPath Path of destination folder.
     * @param progress Current progress in copying.
     * @param totalNumberOfFiles Total number of files for copying.
     * @param worker Worker.
     * @throws IOException
     */
    public static void copyFolderWithContents(String sourcePath, String destinationPath, AtomicInteger progress, int totalNumberOfFiles, CreateCopiesWorker worker) throws IOException {
        copyFolderWithContents(new File(sourcePath), new File(destinationPath), progress, totalNumberOfFiles, worker);
    }

    /**
     * Copy folder from source directory to destination directory with all content inside that directory (deep copy)
     * @param source Source directory.
     * @param destination Destination directory.
     * @param progress Current progress in copying.
     * @param totalNumberOfFiles Total number of files for copying.
     * @param worker Worker.
     * @throws IOException
     */
    public static void copyFolderWithContents(File source, File destination, AtomicInteger progress, int totalNumberOfFiles, CreateCopiesWorker worker) throws IOException {
        if (source.isDirectory())
        {
            if (!destination.exists())
            {
                destination.mkdirs();
            }

            String files[] = source.list();
            for (String file : files)
            {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);

                copyFolderWithContents(srcFile, destFile, progress, totalNumberOfFiles, worker);
            }
        } else {
            Files.copy(Paths.get(source.getPath()), Paths.get(destination.getPath()), StandardCopyOption.REPLACE_EXISTING);

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), totalNumberOfFiles);
            }
        }
    }

}
