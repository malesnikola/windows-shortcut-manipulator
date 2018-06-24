package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class CreateCopiesWorker extends Task<Boolean> {
    private WindowsShortcutModel windowsShortcutModel;
    private String destinationPath;
    private boolean ifSaveHierarchy;

    public CreateCopiesWorker(WindowsShortcutModel windowsShortcutModel, String destinationPath, boolean ifSaveHierarchy, ProgressForm progressForm) {
        super();
        this.windowsShortcutModel = windowsShortcutModel;
        this.destinationPath = destinationPath;
        this.ifSaveHierarchy = ifSaveHierarchy;
        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.copyTargetFiles(destinationPath, ifSaveHierarchy, this);

        return true;
    }

    public void updateProgress(long workDone, long size){
        super.updateProgress(workDone, size);
    }
}
