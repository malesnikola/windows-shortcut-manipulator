package main.java.model;

import main.java.domain.FailedFileDetails;
import main.java.domain.WindowsShortcutWrapper;
import main.java.enums.FileState;
import main.java.enums.ShortcutActionState;
import main.java.enums.WindowsShortcutModelState;
import main.java.mslinks.mslinks.ShellLink;
import main.java.workers.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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
    private List<FailedFileDetails> lastFailedSavedFiles = new LinkedList<>();

    /**
     * Contains list of all files (with error details) which cannot be removed.
     */
    private List<FailedFileDetails> lastFailedRemovedFiles = new LinkedList<>();

    private List<String> minimumParentList = new LinkedList<>();

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

    public List<FailedFileDetails> getLastFailedSavedFiles() {
        return lastFailedSavedFiles;
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
            shortcut.checkAvailabilityAndSize();

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
            String originalFilePath = shortcut.getTargetFilePath();
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

    public List<String> getMinimumMatchingParents(boolean ifCheckingTargetPaths) {
        List<String> response = new LinkedList<>();
        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String path = (ifCheckingTargetPaths ? shortcut.getTargetFilePath() : shortcut.getFilePath());
            String patternSeparator = Pattern.quote(System.getProperty("file.separator"));
            String[] splittedPath = path.split(patternSeparator);

            if (response.isEmpty()) {
                response = new LinkedList<String>(Arrays.asList(splittedPath));
                response.remove(response.size() - 1);
            } else {
                Iterator<String> iterator = response.iterator();
                for (int cnt = 0; (cnt < splittedPath.length) && iterator.hasNext(); cnt++) {
                    String parent = iterator.next();
                    if (!splittedPath[cnt].equals(parent)) {
                        iterator.remove();
                        while (iterator.hasNext()) {
                            iterator.next();
                            iterator.remove();
                        }

                        break;
                    }
                }

                if (response.isEmpty()) {
                    return response;
                }
            }
        }

        return response;
    }

    public boolean ifFolderIsValid(String folderPath) {
        File file = new File(folderPath);
        return file.isDirectory();
    }

    private String replaceBeginningPath(String path, String oldParents, String newParents) {
        String fileSeparator = System.getProperty("file.separator");
        String patternSeparator = Pattern.quote(fileSeparator);
        String[] splittedPath = path.split(patternSeparator);
        String[] splittedOldParents = oldParents.split(patternSeparator);
        String[] splittedNewParents = newParents.split(patternSeparator);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splittedNewParents.length; i++) {
            sb.append(splittedNewParents[i] + fileSeparator);
        }

        for(int i = splittedOldParents.length; i < splittedPath.length; i++) {
            sb.append(splittedPath[i] + fileSeparator);
        }

        sb.deleteCharAt(sb.length() - 1); // delete last '\'
        return sb.toString();
    }

    public void changeParents(String oldParents, String newParents, ChangeParentsWorker worker) {
        lastFailedSavedFiles.clear();

        lastModelState = WindowsShortcutModelState.CHANGED_ROOTS;

        AtomicInteger progress = new AtomicInteger();

        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = replaceBeginningPath(shortcut.getTargetFilePath(), oldParents, newParents);
            String shortcutPath = shortcut.getFilePath();

            try {
                ShellLink.createLink(originalFilePath, shortcutPath);
                shortcut.setTargetFilePath(originalFilePath);
                shortcut.setShortcutActionState(ShortcutActionState.MODIFIED);
            } catch (IOException e) {
                shortcut.setShortcutActionState(ShortcutActionState.FAILED_MODIFIED);
                lastFailedSavedFiles.add(new FailedFileDetails(shortcutPath, e.getMessage()));
                logger.debug("Exception in method changeParents: " + e.getMessage());
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        shortcutObservers.forEach(o -> o.onChangedRoot());
    }

    private String fixShortcutFileName(String fileName) {
        return fileName.replaceAll(" - Shortcut.lnk", "");
    }

    private String getSavingDestinationForFile(WindowsShortcutWrapper shortcut, String commonPathForSaving, List<String> minimumParentPath) {
        String fileSeparator = System.getProperty("file.separator");
        String shortcutPath = shortcut.getFilePath();
        String newFileName = fixShortcutFileName(shortcut.getFileName());

        if ((minimumParentPath != null) && !minimumParentPath.isEmpty()){
            String patternSeparator = Pattern.quote(fileSeparator);
            String[] splittedPath = shortcutPath.split(patternSeparator);
            StringBuilder parentDirectories = new StringBuilder("");
            for (int i = minimumParentPath.size(); i < splittedPath.length - 1; i++) {
                parentDirectories.append(splittedPath[i] + fileSeparator);
            }

            // create parent directories if does not exist
            File parentDirectory = new File(commonPathForSaving + fileSeparator + parentDirectories.toString());
            if (!parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }

            newFileName = parentDirectories.toString() + newFileName;
        }

        return commonPathForSaving + fileSeparator + newFileName;
    }

    public void copyTargetFiles(String commonPathForSaving, boolean ifSaveHierarchy, CreateCopiesWorker worker) {
        lastFailedSavedFiles.clear();

        lastModelState = WindowsShortcutModelState.CREATED_COPIES;

        AtomicInteger progress = new AtomicInteger();
        List<String> minimumParentPath = (ifSaveHierarchy ? getMinimumMatchingParents(false) : null);

        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = shortcut.getTargetFilePath();
            String pathForSaving = getSavingDestinationForFile(shortcut, commonPathForSaving, minimumParentPath);

            try {
                Files.copy(Paths.get(originalFilePath), Paths.get(pathForSaving), StandardCopyOption.REPLACE_EXISTING);
                shortcut.setShortcutActionState(ShortcutActionState.SAVED);
            } catch (Exception e) {
                shortcut.setShortcutActionState(ShortcutActionState.FAILED_SAVED);
                lastFailedSavedFiles.add(new FailedFileDetails(shortcut.getFilePath(), e.getMessage()));
                logger.debug("Exception in method copyTargetFiles: " + e.getMessage());
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        shortcutObservers.forEach(o -> o.onCreateCopies());
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
