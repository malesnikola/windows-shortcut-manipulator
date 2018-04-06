package main.java.dialogs;

import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * Progress form is dialog form with one indicator and bounded task.
 * This for will update indicator according to the task, and close after task is finished.
 * If user press 'ESCAPE' while task is working, task will be canceled and form will be closed.
 */
public class ProgressForm {
    private final Scene parentScene;
    private Stage dialogStage;

    private final ProgressIndicator progressIndicator = new ProgressIndicator();
    private Task<?> task;

    /**
     * Create new progress form
     * @param parentScene Parent stage where progress form is called.
     */
    public ProgressForm(Scene parentScene) {
        this.parentScene = parentScene;
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);

        // reset progress
        progressIndicator.setProgress(-1F);

        Scene scene = new Scene(progressIndicator);
        dialogStage.setScene(scene);

        // add key event handling to enable user to cancel task and close form
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ESCAPE:
                        if (task != null) {
                            task.cancel();
                        }

                        break;
                }
            }
        });

        // disable closing form
        dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });

    }

    /**
     * Bind indicator with task progress.
     * @param task Task
     */
    public void activateProgressBar(final Task<?> task)  {
        this.task = task;
        progressIndicator.progressProperty().bind(task.progressProperty());
        dialogStage.show();
    }

    /**
     * Enable all parent elements and close dialog.
     */
    public void closeDialogStage(){
        parentScene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(false));
        dialogStage.close();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

}
