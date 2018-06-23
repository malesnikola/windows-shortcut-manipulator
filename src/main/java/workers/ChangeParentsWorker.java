package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class ChangeParentsWorker extends Task<Boolean> {
    private WindowsShortcutModel windowsShortcutModel;
    private String oldParents;
    private String newParents;

    public ChangeParentsWorker(WindowsShortcutModel windowsShortcutModel, String oldParent, String newParents, ProgressForm progressForm) {
        super();
        this.windowsShortcutModel = windowsShortcutModel;
        this.oldParents = oldParent;
        this.newParents = newParents;
        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.changeParents(oldParents, newParents, this);

        return true;
    }

    public void updateProgress(long workDone, long size){
        super.updateProgress(workDone, size);
    }
}