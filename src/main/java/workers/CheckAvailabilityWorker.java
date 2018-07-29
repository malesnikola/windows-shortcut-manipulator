package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class CheckAvailabilityWorker extends AbstractWorker {
    public CheckAvailabilityWorker(WindowsShortcutModel windowsShortcutModel, ProgressForm progressForm) {
        super(windowsShortcutModel, progressForm);
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.checkAvailability(this);

        return true;
    }
}
