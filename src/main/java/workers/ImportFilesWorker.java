package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

import java.io.File;
import java.util.List;

public class ImportFilesWorker extends Task<Boolean>{

    private WindowsShortcutModel windowsShortcutModel;

    private List<File> files;

    public ImportFilesWorker(WindowsShortcutModel WindowsShortcutModel, ProgressForm progressForm, List<File> files) {
        super();
        this.windowsShortcutModel = WindowsShortcutModel;
        this.files = files;
        this.setOnCancelled(event -> {
            progressForm.closeDialogStage();
        });

        this.setOnSucceeded(event -> {
            progressForm.closeDialogStage();
        });
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.importFiles(files, this);

        return true;
    }

    public void updateProgress(long workDone){
        super.updateProgress(workDone, files.size());
    }
}
