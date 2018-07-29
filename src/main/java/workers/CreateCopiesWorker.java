package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class CreateCopiesWorker extends AbstractWorker {
    private String destinationPath;
    private boolean ifSaveHierarchy;

    public CreateCopiesWorker(WindowsShortcutModel windowsShortcutModel, String destinationPath, boolean ifSaveHierarchy, ProgressForm progressForm) {
        super(windowsShortcutModel, progressForm);
        this.destinationPath = destinationPath;
        this.ifSaveHierarchy = ifSaveHierarchy;
        this.totalSizeOfTask = windowsShortcutModel.getTotalNumberOfAvailableImportedFiles();   // for this task total size is different from default
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.copyTargetFiles(destinationPath, ifSaveHierarchy, this);

        return true;
    }
}