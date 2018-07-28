package main.java.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.java.controllers.MainScreenController;
import main.java.model.WindowsShortcutModel;
import org.apache.log4j.BasicConfigurator;

/**
 * Required settings for IDE (in order to prevent bugs with latinic letters):
 * Global encoding: windows-1250
 * Project encoding: windows-1250
 *
 * Set "resources" folder to be "Resources root" -> In IntelliJ right click on "resources" folder in tree, then choose "Mark directory as Resources Root"
 *
 * For building executable .jar file:
 * 1) File -> Project structure -> Artifacts
 *    Remove all artifact, then select '+' -> JAR -> From modules with dependencies
 *    Select this class (main.java.main.Main) as main class ("Main Class" field), and press "OK".
 *    If there was an error “MANIFEST.MF already exists in VFS", try again without selecting main class (leave "Main Class" field empty)
 * 2) Build -> Build Artifacts -> Rebuild
 * 3) Executable .jar is available inside project folder in "out\artifacts\windows_shortcut_manipulator_jar" folder
 */
public class Main extends Application {
    private Parent rootNode;

    MainScreenController mainScreenController;

    @Override
    public void init() throws Exception{
        BasicConfigurator.configure();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/screen_main.fxml"));
        rootNode = loader.load();
        WindowsShortcutModel.getInstance(); // prevent synchronization problem
        mainScreenController = loader.getController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        // set title for app
        primaryStage.setTitle("Windows Shortcut Manipulator");
        // set image for app
        primaryStage.getIcons().add(new Image("/images/WindowsShortcutManipulator.png"));
        // create scene
        Scene scene = new Scene(rootNode);
        mainScreenController.setScene(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
