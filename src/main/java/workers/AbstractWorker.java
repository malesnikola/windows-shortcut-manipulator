package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract worker is common worker for all task with ProgressForm.
 * Every other worker with ProgressForm should extends this class.
 */
public abstract class AbstractWorker extends Task<Boolean> implements Progress {
    protected WindowsShortcutModel windowsShortcutModel;
    protected AtomicInteger progress = new AtomicInteger();
    protected long totalSizeOfTask;

    protected AbstractWorker(WindowsShortcutModel windowsShortcutModel, ProgressForm progressForm) {
        super();
        this.windowsShortcutModel = windowsShortcutModel;
        this.totalSizeOfTask = windowsShortcutModel.getImportedFiles().size(); // by default total size of task is equal to size of imported files

        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    public void updateProgress() {
        super.updateProgress(progress.incrementAndGet(), totalSizeOfTask);
    }

    @Override
    public void setTotalSizeOfTask(long size) {
        totalSizeOfTask = size;
    }
}
