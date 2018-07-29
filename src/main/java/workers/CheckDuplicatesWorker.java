package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class CheckDuplicatesWorker extends AbstractWorker {
    public CheckDuplicatesWorker(WindowsShortcutModel windowsShortcutModel, ProgressForm progressForm) {
        super(windowsShortcutModel, progressForm);
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.checkDuplicates(this);

        return true;
    }
}
