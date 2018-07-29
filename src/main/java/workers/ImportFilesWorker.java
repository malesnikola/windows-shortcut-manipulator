package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

import java.io.File;
import java.util.List;

public class ImportFilesWorker extends AbstractWorker {
    private List<File> files;

    public ImportFilesWorker(WindowsShortcutModel windowsShortcutModel, ProgressForm progressForm, List<File> files) {
        super(windowsShortcutModel, progressForm);
        this.files = files;
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.importFiles(files, this);

        return true;
    }
}
