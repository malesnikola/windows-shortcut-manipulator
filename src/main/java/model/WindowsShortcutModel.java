package main.java.model;

import main.java.domain.DuplicateFileInfo;
import main.java.domain.FailedFileDetails;
import main.java.domain.WindowsShortcutWrapper;
import main.java.enums.FileState;
import main.java.enums.WindowsShortcutModelState;
import main.java.workers.CheckAvailabilityWorker;
import main.java.workers.CheckDuplicatesWorker;
import main.java.workers.ImportFilesWorker;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WindowsShortcutModel {
    private static Logger logger = Logger.getLogger(WindowsShortcutModel.class);

    /**
     * Singleton instance.
     */
    private static WindowsShortcutModel windowsShortcutModel;

    /**
     * Last model state represent last executed action (e.g. "AVAILABLE" for available original files).
     */
    private WindowsShortcutModelState lastModelState = WindowsShortcutModelState.NONE;

    /**
     * Contains all imported ".lnk" files. Key is file path (for ".lnk" file), value is WindowsShortcutWrapper.
     */
    private Map<String, WindowsShortcutWrapper> importedFiles = new HashMap<>();

    /**
     * List of duplicate files (files with the same target file)
     */
    private List<DuplicateFileInfo> duplicateFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) which cannot be imported.
     */
    private List<FailedFileDetails> lastFailedLoadingFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) which cannot be saved.
     */
    private List<FailedFileDetails> lastFailedSavingFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) which cannot be removed.
     */
    private List<FailedFileDetails> lastFailedRemovedFiles = new LinkedList<>();

    /**
     * Contains set of registered observers.
     */
    private Set<WindowsShortcutObserver> observers = new HashSet<>();

    public static WindowsShortcutModel getInstance() {
        if (windowsShortcutModel == null) {
            windowsShortcutModel = new WindowsShortcutModel();
        }

        return windowsShortcutModel;
    }

    /**
     * Get all imported files.
     * @return Returns HashMap where key is ".lnk" file path (full file name) and value is Mp3FileWrapper.
     */
    public Map<String, WindowsShortcutWrapper> getImportedFiles() {
        return importedFiles;
    }

    public List<DuplicateFileInfo> getDuplicateFiles() {
        return duplicateFiles;
    }

    public boolean ifSomeFilesAreDuplicates() {
        return !duplicateFiles.isEmpty();
    }

    public List<FailedFileDetails> getLastFailedLoadingFiles() {
        return lastFailedLoadingFiles;
    }

    public List<FailedFileDetails> getLastFailedSavingFiles() {
        return lastFailedSavingFiles;
    }

    public List<FailedFileDetails> getLastFailedRemovedFiles() {
        return lastFailedRemovedFiles;
    }

    public WindowsShortcutModelState getLastModelState() {
        return lastModelState;
    }

    private void populateListWithActualFiles(List<File> originalFiles, List<File> actualFiles) {
        for (File originalFile : originalFiles) {
            if (originalFile.isDirectory()) {
                File[] childrenFiles = originalFile.listFiles();
                populateListWithActualFiles(Arrays.asList(childrenFiles), actualFiles);
            }
            else {
                actualFiles.add(originalFile);
            }
        }
    }

    /**
     * Import ".lnk" files.
     * @param files List of files.
     * @param worker Worker which import files and which is bounded with progress form.
     */
    public void importFiles(List<File> files, ImportFilesWorker worker) {
        if (files != null && !files.isEmpty()) {
            lastModelState = WindowsShortcutModelState.IMPORTED;

            int previousSizeOfImportedFiles = importedFiles.size();
            lastFailedLoadingFiles.clear();

            AtomicInteger progress = new AtomicInteger();

            List<File> actualFiles = new LinkedList<>();
            populateListWithActualFiles(files, actualFiles);

            actualFiles.stream().forEach(file -> {
                String filePath = file.getPath();
                if (!importedFiles.containsKey(filePath)) {
                    try {
                        WindowsShortcutWrapper mp3FileWrapper = new WindowsShortcutWrapper(filePath);
                        importedFiles.put(filePath, mp3FileWrapper);
                    } catch (IOException | ParseException e) {
                        logger.debug("Exception in method importFiles: " + e.getMessage());
                        lastFailedLoadingFiles.add(new FailedFileDetails(filePath, e.getMessage()));
                    } catch (Exception e) {
                        logger.debug("Unexpected excepton in method importFiles: " + e.getMessage());
                        lastFailedLoadingFiles.add(new FailedFileDetails(filePath, "Unexpected exception: " + e.getMessage()));
                    }
                }

                if (worker != null) {
                    worker.updateProgress(progress.incrementAndGet(), actualFiles.size());
                }
            });

            if (previousSizeOfImportedFiles != importedFiles.size() || !lastFailedLoadingFiles.isEmpty()) {
                // notify observers if at least one file is imported, or at list one file cannot be imported
                observers.forEach(WindowsShortcutObserver::onImportedFilesChanged);
            }
        }
    }

    /**
     * Remove imported files.
     * @param filePaths List of ".lnk" file paths which has to be removed.
     */
    public void removeImportedFiles(List<String> filePaths) {
        if (filePaths != null) {
            lastModelState = WindowsShortcutModelState.REMOVED;

            int previousSizeOfImportedFiles = importedFiles.size();

            for (String filePath : filePaths) {
                importedFiles.remove(filePath);
            }

            if (previousSizeOfImportedFiles != importedFiles.size()) {
                // notify observers if at least one file is removed
                observers.forEach(WindowsShortcutObserver::onImportedFilesChanged);
            }
        }
    }

    public void checkAvailability(CheckAvailabilityWorker worker) {
        lastModelState = WindowsShortcutModelState.CHECKED_AVAILABILITY;

        AtomicInteger progress = new AtomicInteger();

        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = shortcut.getRealFilename();
            File originalFile = new File(originalFilePath);
            if (originalFile.exists()) {
                shortcut.setFileState(FileState.AVAILABLE);
            } else {
                shortcut.setFileState(FileState.UNAVAILABLE);
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        if (!importedFiles.isEmpty()) {
            // notify observers if at least one file is checked
            observers.forEach(WindowsShortcutObserver::onCheckedAvailability);
        }
    }

    public void checkDuplicates(CheckDuplicatesWorker worker) {
        lastModelState = WindowsShortcutModelState.CHECKED_DUPLICATES;

        duplicateFiles.clear();
        Map<String, String> foundTargetFiles = new HashMap<>();
        AtomicInteger progress = new AtomicInteger();
        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = shortcut.getRealFilename();
            String shortcutFilePath = shortcut.getFilePath();

            if (foundTargetFiles.containsKey(originalFilePath)){
                duplicateFiles.add(new DuplicateFileInfo(foundTargetFiles.get(originalFilePath), originalFilePath));
                duplicateFiles.add(new DuplicateFileInfo(shortcutFilePath, originalFilePath));
            } else {
                foundTargetFiles.put(originalFilePath, shortcutFilePath);
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        observers.forEach(WindowsShortcutObserver::onCheckedCopies);
    }

    public void registerObserver(WindowsShortcutObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(WindowsShortcutObserver observer) {
        observers.remove(observer);
    }

    public interface WindowsShortcutObserver {
        void onImportedFilesChanged();

        void onCheckedAvailability();

        void onCheckedCopies();

        void onChangedRoot();

        void onCreateCopies();
    }
}
