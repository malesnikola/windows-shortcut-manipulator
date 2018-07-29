package main.java.workers;

import javafx.concurrent.Task;
import main.java.dialogs.ProgressForm;
import main.java.model.WindowsShortcutModel;

public class ChangeParentsWorker extends AbstractWorker {
    private String oldParents;
    private String newParents;

    public ChangeParentsWorker(WindowsShortcutModel windowsShortcutModel, String oldParent, String newParents, ProgressForm progressForm) {
        super(windowsShortcutModel, progressForm);
        this.oldParents = oldParent;
        this.newParents = newParents;
    }

    @Override
    protected Boolean call() throws Exception {
        windowsShortcutModel.changeParents(oldParents, newParents, this);

        return true;
    }
}