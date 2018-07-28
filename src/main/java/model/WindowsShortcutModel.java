package main.java.model;

import main.java.domain.FailedFileDetails;
import main.java.domain.FileSize;
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
     * Last model state represent last executed action (e.g. "IMPORTED" means that last action was importing files).
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

    /**
     * Contains set of registered shortcutObservers.
     */
    private Set<WindowsShortcutObserver> shortcutObservers = new HashSet<>();

    /**
     * Contains set of registered manipulationWithDuplicatesObservers.
     */
    private Set<ManipulationWithDuplicatesObserver> manipulationWithDuplicatesObservers = new HashSet<>();

    public static WindowsShortcutModel getInstance() {
        if (windowsShortcutModel == null) {
            windowsShortcutModel = new WindowsShortcutModel();
        }

        return windowsShortcutModel;
    }

    /**
     * Get all imported files.
     * @return Returns HashMap where key is ".lnk" file path (full file name) and value is WindowsShortcutWrapper.
     */
    public Map<String, WindowsShortcutWrapper> getImportedFiles() {
        return importedFiles;
    }

    /**
     * Get all duplicate files.
     * @return HashMap where key is ".lnk" file path (full file name) and value is WindowsShortcutWrapper.
     */
    public Map<String, String> getDuplicateFiles() {
        return duplicateFiles;
    }

    /**
     * Check if exist at least one duplicates.
     * @return Returns true if exist at least one duplicates, otherwise false.
     */
    public boolean ifSomeFilesAreDuplicates() {
        return !duplicateFiles.isEmpty();
    }

    /**
     * Get list of last failed loaded files.
     * @return List of FailedFileDetails.
     */
    public List<FailedFileDetails> getLastFailedLoadingFiles() {
        return lastFailedLoadingFiles;
    }

    /**
     * Get list of last failed saved files.
     * @return List of FailedFileDetails.
     */
    public List<FailedFileDetails> getLastFailedSavedFiles() {
        return lastFailedSavedFiles;
    }

    /**
     * Get list of last failed removed files.
     * @return List of FailedFileDetails.
     */
    public List<FailedFileDetails> getLastFailedRemovedFiles() {
        return lastFailedRemovedFiles;
    }

    /**
     * Check if last removing files (duplicate files) action has any failure.
     * @return Returns true if at least one file didn't successfully removed, otherwise false.
     */
    public boolean ifSomeFilesFailedRemoved() { return !lastFailedRemovedFiles.isEmpty(); }

    public WindowsShortcutModelState getLastModelState() {
        return lastModelState;
    }

    /**
     * Recursively populate actualFiles from originalFiles.
     * @param originalFiles Contains list of all files and folders which user selected.
     * @param actualFiles At the end of method, this list will contain all files which user selected
     *                    and all files which are underneath selected folders.
     */
    private void populateListWithActualFiles(List<File> originalFiles, List<File> actualFiles) {
        for (File originalFile : originalFiles) {
            if (originalFile.isDirectory()) {
                // get all children in folder and recursively call same method
                File[] childrenFiles = originalFile.listFiles();
                populateListWithActualFiles(Arrays.asList(childrenFiles), actualFiles);
            }
            else {
                // add actual file
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
            // get all files which user selected and all files which are underneath selected folders
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

            if ((previousSizeOfImportedFiles != importedFiles.size()) || !lastFailedLoadingFiles.isEmpty()) {
                // notify shortcutObservers if at least one file is imported, or at list one file cannot be imported
                shortcutObservers.forEach(WindowsShortcutObserver::onImportedFilesChanged);
            }
        }
    }

    /**
     * Remove imported files (only from software, not form disk).
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

    /**
     * Check availability of all imported files.
     * @param worker Worker which is bounded with progress form.
     */
    public void checkAvailability(CheckAvailabilityWorker worker) {
        lastModelState = WindowsShortcutModelState.CHECKED_AVAILABILITY;

        AtomicInteger progress = new AtomicInteger();

        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            shortcut.updateAvailabilityAndSize();

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        if (!importedFiles.isEmpty()) {
            // notify shortcutObservers if at least one file is checked
            shortcutObservers.forEach(WindowsShortcutObserver::onCheckedAvailability);
        }
    }

    /**
     * Check if there are some files which targeting the same original file.
     * @param worker Worker which is bounded with progress form.
     */
    public void checkDuplicates(CheckDuplicatesWorker worker) {
        lastModelState = WindowsShortcutModelState.CHECKED_DUPLICATES;

        duplicateFiles.clear();     // clear duplicate files
        Map<String, String> foundTargetFiles = new HashMap<>(); // key = original file path; value = shortcut file path
        Set<String> foundedTargetPaths = new HashSet();         // set of original (targeting) file paths
        AtomicInteger progress = new AtomicInteger();
        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = shortcut.getTargetFilePath();
            String shortcutFilePath = shortcut.getFilePath();

            if (foundTargetFiles.containsKey(originalFilePath)){
                // if we already had found originalFilePath, that means that this is duplicate
                // add this duplicate to the duplicateFiles
                duplicateFiles.put(shortcutFilePath, originalFilePath);
                if (foundedTargetPaths.add(originalFilePath)) {
                    // if this is the first duplicate (secondly found file with originalFilePath)
                    //  add firstly found file with same originalFilePath into duplicateFiles
                    duplicateFiles.put(foundTargetFiles.get(originalFilePath), originalFilePath);
                }
            } else {
                // if originalFilePath is found for the first time just add originalFilePath and shortcutFilePath in map
                foundTargetFiles.put(originalFilePath, shortcutFilePath);
            }

            if (worker != null) {
                worker.updateProgress(progress.incrementAndGet(), importedFiles.size());
            }
        }

        shortcutObservers.forEach(WindowsShortcutObserver::onCheckedDuplicates);
    }

    /**
     * Try to remove file from disk.
     * @param filePath File path.
     * @return True if file was removed, otherwise false.
     */
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

    /**
     * Remove selected shortcut files (from software and from disk) which is duplicates.
     * @param filePaths List of shortcuts file path.
     */
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

    /**
     * When manipulation with duplicates is finished, update last model state to REMOVED_DUPLICATES which means that last user action was removing duplicates.
     */
    public void finishRemoveDuplicates() {
        lastModelState = WindowsShortcutModelState.REMOVED_DUPLICATES;
    }

    /**
     * Get list of parent folders sorted by hierarchy in system, which exist in paths for all imported files.
     * @param ifCheckingTargetPaths True if we checking paths of original (targeting) files; false if we checking paths of shortcut files.
     * @return List of parent folders sorted by hierarchy in system.
     */
    public List<String> getMinimumMatchingParents(boolean ifCheckingTargetPaths) {
        List<String> response = new LinkedList<>();
        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String path = (ifCheckingTargetPaths ? shortcut.getTargetFilePath() : shortcut.getFilePath());
            String patternSeparator = Pattern.quote(System.getProperty("file.separator"));
            String[] splittedPath = path.split(patternSeparator);   // contains array of all parent folders (sorted by hierarchy) and file name

            if (response.isEmpty()) {
                // add all parents of first file without file name
                response = new LinkedList<String>(Arrays.asList(splittedPath));
                response.remove(response.size() - 1);
            } else {
                Iterator<String> iterator = response.iterator();
                // check does every parent of current file exist in minimum matching parents
                for (int cnt = 0; (cnt < splittedPath.length) && iterator.hasNext(); cnt++) {
                    String parent = iterator.next();
                    if (!splittedPath[cnt].equals(parent)) {
                        // if current parent is different, remove this parent all his children
                        iterator.remove();
                        while (iterator.hasNext()) {
                            iterator.next();
                            iterator.remove();
                        }

                        break;
                    }
                }

                // if at this point (when we visit at least two files) there is no matching parents we can return empty list.
                if (response.isEmpty()) {
                    return response;
                }
            }
        }

        return response;
    }

    /**
     * Replace beginning old parents (folders) in path with new parents.
     * @param path Old file path.
     * @param oldParents Beginning old parents which have to be changed with new parents.
     * @param newParents New parents.
     * @return New path which present old path where beginning old parents is replaced with the new once.
     */
    private String replaceBeginningPath(String path, String oldParents, String newParents) {
        String fileSeparator = System.getProperty("file.separator");
        String patternSeparator = Pattern.quote(fileSeparator);
        String[] splittedPath = path.split(patternSeparator);
        String[] splittedOldParents = oldParents.split(patternSeparator);
        String[] splittedNewParents = newParents.split(patternSeparator);
        StringBuilder sb = new StringBuilder();
        // first part of the new path is contained of new parents
        for (int i = 0; i < splittedNewParents.length; i++) {
            sb.append(splittedNewParents[i] + fileSeparator);
        }

        // second part starts from folder which is children of last parent in oldParents
        for(int i = splittedOldParents.length; i < splittedPath.length; i++) {
            sb.append(splittedPath[i] + fileSeparator);
        }

        sb.deleteCharAt(sb.length() - 1); // delete last file separator
        return sb.toString();
    }

    /**
     * Change original (targeting) file path parents in all imported shortcut files.
     * @param oldParents Beginning old parents which have to be changed with new parents.
     * @param newParents New parents.
     * @param worker Worker which is bounded with progress form.
     */
    public void changeParents(String oldParents, String newParents, ChangeParentsWorker worker) {
        lastFailedSavedFiles.clear();

        lastModelState = WindowsShortcutModelState.CHANGED_ROOTS;

        AtomicInteger progress = new AtomicInteger();

        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = replaceBeginningPath(shortcut.getTargetFilePath(), oldParents, newParents);   // new original (targeting) file
            String shortcutPath = shortcut.getFilePath();   // shortcut path is the same as before

            try {
                ShellLink.createLink(originalFilePath, shortcutPath);   // create new shortcut on disk and override existing one
                shortcut.setTargetFilePath(originalFilePath);
                shortcut.setShortcutActionState(ShortcutActionState.MODIFIED);  // change last action on file
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

    /**
     * Remove " - Shortcut.lnk" from file name for creating real copy of file.
     * @param fileName File name.
     * @return File name without " - Shortcut.lnk".
     */
    private String fixShortcutFileName(String fileName) {
        return fileName.replaceAll(" - Shortcut.lnk", "");
    }

    /**
     * Get saving destination for forwarded shortcut file for saving real copy of original file.
     * @param shortcut Shortcut file.
     * @param commonPathForSaving Common folder for saving all real copies of original files.
     * @param minimumParentPath Same shortcut path parents of all imported shortcut files.
     * @return
     */
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

    /**
     * Create real copies of original files.
     * @param commonPathForSaving Common folder for saving all files.
     * @param ifSaveHierarchy True if at the destination folder for saving we keep folder hierarchy as shortcut files, otherwise false.
     * @param worker Worker which is bounded with progress form.
     */
    public void copyTargetFiles(String commonPathForSaving, boolean ifSaveHierarchy, CreateCopiesWorker worker) {
        lastFailedSavedFiles.clear();

        lastModelState = WindowsShortcutModelState.CREATED_COPIES;

        AtomicInteger progress = new AtomicInteger();
        List<String> minimumParentPath = (ifSaveHierarchy ? getMinimumMatchingParents(false) : null);

        for (WindowsShortcutWrapper shortcut : importedFiles.values()) {
            String originalFilePath = shortcut.getTargetFilePath();
            String pathForSaving = getSavingDestinationForFile(shortcut, commonPathForSaving, minimumParentPath);   // get path for saving

            try {
                Files.copy(Paths.get(originalFilePath), Paths.get(pathForSaving), StandardCopyOption.REPLACE_EXISTING); // save real copy of original file
                shortcut.setShortcutActionState(ShortcutActionState.SAVED); // change last action
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

    /**
     * Get total number of imported shortcut files whose original (targeting) files are available.
     * @return Number of available imported files.
     */
    public int getTotalNumberOfAvailableImportedFiles() {
        return (int) importedFiles.values()
                .stream()
                .filter(f -> (f.getFileState() == FileState.AVAILABLE) || (f.getFileState() == FileState.CASE_SENSITIVE))
                .count();
    }

    /**
     * Get total size of all available original (targeting) files which is imported through shortcut files.
     * @return Total FileSize of available original files.
     */
    public FileSize getTotalSizeOfOriginalFiles() {
        long totalSizeInBytes = importedFiles.values().stream()
                .mapToLong(f -> f.getFileSize().getSizeInBytes())
                .sum();

        return FileSize.fromLong(totalSizeInBytes);
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

        void onCheckedDuplicates();

        void onChangedRoot();

        void onCreateCopies();
    }
}
