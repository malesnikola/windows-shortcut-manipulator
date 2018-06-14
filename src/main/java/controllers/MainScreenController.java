package main.java.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Callback;
import main.java.dialogs.ProgressForm;
import main.java.domain.FailedFileDetails;
import main.java.domain.WindowsShortcutDetails;
import main.java.model.WindowsShortcutModel;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainScreenController implements WindowsShortcutModel.WindowsShortcutObserver {
    private static Logger logger = Logger.getLogger(MainScreenController.class);

    private static ResourceBundle resourceBundle;

    private Scene scene;
    private String chosenLanguage; // en (English), rs (Serbian)

    private Task saveFilesWorker;
    private TableColumn chosenSortingColumn;

    // menu
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu languageMenu;
    @FXML
    private Menu helpMenu;

    // menu items
    @FXML
    private MenuItem openFilesMenuItem;
    @FXML
    private MenuItem openFolderMenuItem;
    @FXML
    private CheckMenuItem englishMenuItem;
    @FXML
    private CheckMenuItem serbianMenuItem;
    @FXML
    private MenuItem aboutMenuItem;

    // text flow
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextFlow consoleTextFlow;

    // progress bar
    @FXML
    private ProgressBar progressBar;

    // buttons
    @FXML
    private Button checkAvailabilityButton;
    @FXML
    private Button checkDuplicatesButton;
    @FXML
    private Button changeRootButton;
    @FXML
    private Button createCopiesButton;
    @FXML
    private Button chooseDirectoryButton;

    // choice box
    @FXML
    private ChoiceBox<String> ChooseRootChoiceBox;

    // text field
    @FXML
    private TextField newRootTextField;
    @FXML
    private TextField rootForCopiesTextField;

    // labels
    @FXML
    private Label changeRootLabel;
    @FXML
    private Label chooseRootLabel;
    @FXML
    private Label newRootLabel;
    @FXML
    private Label createCopiesLabel;
    @FXML
    private Label directoryLabel;
    @FXML
    private Label consoleLabel;

    // data table
    private ObservableList<WindowsShortcutDetails> tableData;
    @FXML
    private TableView<WindowsShortcutDetails> tableView;
    @FXML
    private TableColumn<WindowsShortcutDetails, String> shortcutFilePathColumn;
    @FXML
    private TableColumn<WindowsShortcutDetails, String> originalFilePathColumn;
    @FXML
    private TableColumn<WindowsShortcutDetails, String> availabilityColumn;
    @FXML
    private TableColumn<WindowsShortcutDetails, String> actionColumn;

    /**
     * Get resource bundle of application.
     * @return Returns chosen ResourceBundle.
     */
    public static ResourceBundle getResourceBundle(){
        return resourceBundle;
    }

    /**
     * Get localized string from Bundle_{chosenLanguage}.properties in charset cp1250 (because of Serbian latin letters).
     * @param key Key of the required string.
     * @return Returns value of localized sting if exist, else returns empty string.
     */
    private String getLocalizedString(String key) {
        try {
            String value = resourceBundle.getString(key);
            return new String(value.getBytes("ISO-8859-1"), "cp1250");
        } catch (Exception e) {
            logger.debug("Exception in method getLocalizedString: " + e.getMessage());
            return "";
        }
    }

    /**
     * Populate UI element with localized strings from Bundle_{chosenLanguage}.properties.
     */
    private void populateUIWithLocalizedStrings() {
        // menu
        fileMenu.setText(getLocalizedString("menu.file.text"));
        languageMenu.setText(getLocalizedString("menu.language.text"));
        helpMenu.setText(getLocalizedString("menu.help.text"));

        // menu items
        openFilesMenuItem.setText(getLocalizedString("menu.item.openfiles.text"));
        openFolderMenuItem.setText(getLocalizedString("menu.item.openfolder.text"));
        englishMenuItem.setText(getLocalizedString("menu.item.english.text"));
        serbianMenuItem.setText(getLocalizedString("menu.item.serbian.text"));
        aboutMenuItem.setText(getLocalizedString("menu.item.about.text"));

        // labels
        changeRootLabel.setText(getLocalizedString("label.changeRoot.text"));
        chooseRootLabel.setText(getLocalizedString("label.chooseRoot.text"));
        newRootLabel.setText(getLocalizedString("label.newRoot.text"));
        createCopiesLabel.setText(getLocalizedString("label.createCopies.text"));
        directoryLabel.setText(getLocalizedString("label.directory.text"));
        consoleLabel.setText(getLocalizedString("label.console.text"));

    }

    @FXML
    public void initialize() {
        // by default, languague is English
        resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("en", "EN"));
        chosenLanguage = "en";
        englishMenuItem.setSelected(true);
        populateUIWithLocalizedStrings();
    }

    /**
     * Set javafx scene.
     * @param scene Javafx scene.
     */
    public void setScene(Scene scene) {
        this.scene = scene;

        // add drag over event handling
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                // if at least one of the dragged files has ".mp3" extension enable importing
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // add drop over event handling
//        scene.setOnDragDropped(new EventHandler<DragEvent>() {
//            @Override
//            public void handle(DragEvent event) {
//                Dragboard db = event.getDragboard();
//                boolean success = false;
//                if (db.hasFiles()) {
//                    success = true;
//                    List<File> dbFiles = db.getFiles();
//
//                    ProgressForm progressForm = new ProgressForm(scene);
//                    Task importFilesWorker = new ImportFilesWorker(mp3Model, progressForm, dbFiles);
//
//                    // binds progress of progress form to progress of task
//                    progressForm.activateProgressBar(importFilesWorker);
//
//                    // disable all elements on scene
//                    scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));
//
//                    // open progress dialog
//                    progressForm.getDialogStage().show();
//
//                    // saveFiles new thread
//                    new Thread(importFilesWorker).start();
//                }
//
//                event.setDropCompleted(success);
//                event.consume();
//            }
//        });
    }

    /**
     * Set English as chosen language.
     * @param e
     */
    public void englishMenuItemCheckChanged(ActionEvent e){
        if (!"en".equals(chosenLanguage)) {
            resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("en", "EN"));
            chosenLanguage = "en";
            serbianMenuItem.setSelected(false);
            populateUIWithLocalizedStrings();
        }

        englishMenuItem.setSelected(true);
    }

    /**
     * Set Serbian as chosen language.
     * @param e
     */
    public void serbianMenuItemCheckChanged(ActionEvent e){
        if (!"rs".equals(chosenLanguage)) {
            resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("rs", "RS"));
            chosenLanguage = "rs";
            englishMenuItem.setSelected(false);
            populateUIWithLocalizedStrings();
        }

        serbianMenuItem.setSelected(true);
    }

    /**
     * Open about dialog with basic information of software.
     */
    public void openAboutDialog() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dialog_about.fxml"));
        try {
            Parent rootNode = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(rootNode));
            stage.setTitle(getLocalizedString("dialog.about.title"));
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear console.
     */
    public void clearConsole(){
        consoleTextFlow.getChildren().clear();
    }

    /**
     * Add localized error message on console.
     * @param currentTime String which represent current date time.
     * @param key Key of the required localized string.
     * @param failedFiles List of failed files with details.
     */
    private void addErrorsOnConsole(String currentTime, String key, List<FailedFileDetails> failedFiles) {
        Text importErrorText = new Text();
        importErrorText.setText(currentTime + getLocalizedString(key) + "\n");
        importErrorText.setFill(Color.RED);
        consoleTextFlow.getChildren().addAll(importErrorText);

        for (FailedFileDetails failedFileDetails : failedFiles) {
            Text fileErrorText = new Text("\t\t " + failedFileDetails.getFilePath() + " (" + failedFileDetails.getErrorMessage() + ")\n");
            fileErrorText.setFill(Color.RED);
            consoleTextFlow.getChildren().addAll(fileErrorText);
        }
    }

    /**
     * Add localized info on console based on previous user action.
     */
    private void addInfoOnConsole(){
        // get current time
        Calendar rightNowCalendar = Calendar.getInstance();
        int hours = rightNowCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNowCalendar.get(Calendar.MINUTE);
        int seconds = rightNowCalendar.get(Calendar.SECOND);
        String currentTime = ((hours < 10) ? ("0" + hours) : hours) + ":"
                + ((minutes < 10) ? ("0" + minutes) : minutes) + ":"
                + ((seconds < 10) ? ("0" + seconds) : seconds) + " - ";

        scrollPane.setVvalue(1.0); // 1.0 means 100% at the bottom
    }

    /**
     * Open file chooser dialog and import selected files into mp3Model.
     */
    public void openFiles() {

    }

    /**
     * Open directory chooser dialog and import all ".mp3" files from chosen directory into mp3Model.
     */
    public void openFolder() {

    }

    @Override
    public void onImportedFilesChanged() {

    }

    @Override
    public void onAnalysedAvailability() {

    }

    @Override
    public void onAnalysedCopies() {

    }

    @Override
    public void onChangedRoot() {

    }

    @Override
    public void onCreateCopies() {

    }
}
