package main.java.model;

import main.java.domain.FailedFileDetails;
import main.java.domain.WindowsShortcutWrapper;
import main.java.enums.FileState;
import main.java.enums.ShortcutActionState;
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
    private Map<String, String> duplicateFiles = new HashMap<>();

    /**
     * Contains list of all files (with error details) which cannot be imported.
     */
    private List<FailedFileDetails> lastFailedLoadingFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) which cannot be saved.
     */
    private List<FailedFileDetails> lastUnreachableFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) which cannot be removed.
     */
    private List<FailedFileDetails> lastFailedRemovedFiles = new LinkedList<>();

    /**
     * Contains set of registered shortcutObservers.
     */
    private Set<WindowsShortcutObserver> shortcutObservers = new HashSet<>();

    private Set<ManipulationWithDuplicatesObserver> manipulationWithDuplicatesObservers = new HashSet<>();

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

    public Map<String, String> getDuplicateFiles() {
        return duplicateFiles;
    }

    public boolean ifSomeFilesAreDuplicates() {
        return !duplicateFiles.isEmpty();
    }

    public List<FailedFileDetails> getLastFailedLoadingFiles() {
        return lastFailedLoadingFiles;
    }

    public List<FailedFileDetails> getLastUnreachableFiles() {
        return lastUnreachableFiles;
    }

    public List<FailedFileDetails> getLastFailedRemovedFiles() {
        return lastFailedRemovedFiles;
    }

    public boolean ifSomeFilesFailedRemoved() { return !lastFailedRemovedFiles.isEmpty(); }

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
                // notify shortcutObservers if at least one file is imported, or at list one file cannot be imported
                shortcutObservers.forEach(WindowsShortcutObserver::onImportedFilesChanged);
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
                // notify shortcutObservers if at least one file is removed
                shortcutObservers.forEach(WindowsShortcutObserver::onImportedFilesChanged);
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
            // notify shortcutObservers if at least one file is checked
            shortcutObservers.forEach(WindowsShortcutObserver::onCheckedAvailability);
        }
    }

    public void checkDuplicates(CheckDuplicatesWorker worker) {
        lastModelState = WindowsShortcutModelState.CHECKED_DUPLICATES;

        duplicateFiles.clear();
        Map<String, String> foundTargetFiles = new HashMap<>();
        Set<String> foundedTargetPaths = new HashSet();
        AtomicInteger progress = new AtomicInteger();
        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = shortcut.getRealFilename();
            String shortcutFilePath = shortcut.getFilePath();

            if (foundTargetFiles.containsKey(originalFilePath)){
                duplicateFiles.put(shortcutFilePath, originalFilePath);
                if (foundedTargetPaths.add(originalFilePath)) {
                    duplicateFiles.put(foundTargetFiles.get(originalFilePath), originalFilePath);
                }
            } else {
                foundTargetFiles.put(originalFilePath, shortcutFilePath);
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        shortcutObservers.forEach(WindowsShortcutObserver::onCheckedCopies);
    }

    public void removeDuplicateFiles(List<String> filePaths) {
        lastFailedRemovedFiles.clear();

        for (String filePath : filePaths) {
            if (tryToRemoveFile(filePath)) {
                duplicateFiles.remove(filePath);
                importedFiles.remove(filePath);
            }
        }

        manipulationWithDuplicatesObservers.forEach(ManipulationWithDuplicatesObserver::onRemovedDuplicates);
    }

    public void finishRemoveDuplicates() {
        lastModelState = WindowsShortcutModelState.REMOVED_DUPLICATES;
    }

    private boolean tryToRemoveFile(String filePath) {
        File fileForRemove = new File(filePath);
        if (!fileForRemove.exists()) {
            lastFailedRemovedFiles.add(new FailedFileDetails(filePath, "It doesn't exist in the first place."));
            return false;
        } else if (!fileForRemove.delete()) {
            lastFailedRemovedFiles.add(new FailedFileDetails(filePath, "File couldn't be deleted."));
            return false;
        }

        return true;
    }

    public void registerWindowsShortcutObserver(WindowsShortcutObserver observer) {
        shortcutObservers.add(observer);
    }

    public void unregisterWindowsShortcutObserver(WindowsShortcutObserver observer) {
        shortcutObservers.remove(observer);
    }

    public void registerManipulationWithDuplicatesObserver(ManipulationWithDuplicatesObserver observer) { manipulationWithDuplicatesObservers.add(observer); }

    public void unregisterManipulationWithDuplicatesObserver(ManipulationWithDuplicatesObserver observer) { manipulationWithDuplicatesObservers.remove(observer); }

    public interface ManipulationWithDuplicatesObserver {
        void onRemovedDuplicates();
    }

    public interface WindowsShortcutObserver {
        void onImportedFilesChanged();

        void onCheckedAvailability();

        void onCheckedCopies();

        void onChangedRoot();

        void onCreateCopies();
    }
}
