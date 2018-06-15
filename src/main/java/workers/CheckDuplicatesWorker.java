package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class CheckDuplicatesWorker extends Task<Boolean> {
    private WindowsShortcutModel windowsShortcutModel;

    public CheckDuplicatesWorker(WindowsShortcutModel WindowsShortcutModel, ProgressForm progressForm) {
        super();
        this.windowsShortcutModel = WindowsShortcutModel;
        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.checkDuplicates(this);

        return true;
    }

    public void updateProgress(long workDone, long size){
        super.updateProgress(workDone, size);
    }
}
