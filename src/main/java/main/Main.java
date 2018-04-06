package main.java.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.java.controllers.MainScreenController;
import org.apache.log4j.BasicConfigurator;

public class Main extends Application {
    private Parent rootNode;

    MainScreenController mainScreenController;

    @Override
    public void init() throws Exception{
        BasicConfigurator.configure();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/screen_main.fxml"));
        rootNode = loader.load();
        mainScreenController = loader.getController();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        // set title for app
        primaryStage.setTitle("Windows Shortcut Manipulator");
        // set image for app
        // rimaryStage.getIcons().add(new Image("/images/Mp3TagGenerator.png"));
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
