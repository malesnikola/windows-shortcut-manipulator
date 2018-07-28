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
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Callback;
import main.java.dialogs.ProgressForm;
import main.java.domain.FailedFileDetails;
import main.java.domain.FileSize;
import main.java.domain.WindowsShortcutDetails;
import main.java.domain.WindowsShortcutWrapper;
import main.java.enums.FileState;
import main.java.enums.ShortcutActionState;
import main.java.enums.WindowsShortcutModelState;
import main.java.model.WindowsShortcutModel;
import main.java.util.Constants;
import main.java.util.FileUtil;
import main.java.util.TimeUtil;
import main.java.workers.*;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainScreenController implements WindowsShortcutModel.WindowsShortcutObserver {
    private static Logger logger = Logger.getLogger(MainScreenController.class);

    private static ResourceBundle resourceBundle;

    private WindowsShortcutModel windowsShortcutModel;

    private int totalNumberOfImportedFiles; // total number of imported files whose original (targeting) file is available
    private FileSize freeSpaceOnDisk;       // free space on disk which is selected for saving copies
    private FileSize totalSizeOfFiles;      // total size of all original imported files

    private Scene scene;
    private String chosenLanguage; // en (English), rs (Serbian)

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

    // buttons
    @FXML
    private Button checkAvailabilityButton;
    @FXML
    private Button checkDuplicatesButton;
    @FXML
    private Button changeParentsButton;
    @FXML
    private Button chooseDirectoryButton;
    @FXML
    private Button createCopiesButton;
    @FXML
    private Button clearConsoleButton;

    // choice box
    @FXML
    private ChoiceBox<String> ChooseParentsChoiceBox;

    // text field
    @FXML
    private TextField newParentsTextField;
    @FXML
    private TextField directoryForCopiesTextField;

    // check box
    @FXML
    private CheckBox keepHierarchyCheckBox;

    // labels
    @FXML
    private Label changeParentsLabel;
    @FXML
    private Label chooseParentsLabel;
    @FXML
    private Label newParentsLabel;
    @FXML
    private Label createCopiesLabel;
    @FXML
    private Label directoryLabel;
    @FXML
    private Label numberOfFilesLabel;
    @FXML
    private Label numberOfFilesValueLabel;
    @FXML
    private Label freeSpaceOnDiskLabel;
    @FXML
    private Label freeSpaceOnDiskValueLabel;
    @FXML
    private Label sizeOfFilesLabel;
    @FXML
    private Label sizeOfFilesValueLabel;
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
    private TableColumn availabilityColumn;
    private TableColumn actionColumn;

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
        changeParentsLabel.setText(getLocalizedString("label.changeParents.text"));
        chooseParentsLabel.setText(getLocalizedString("label.chooseParents.text"));
        newParentsLabel.setText(getLocalizedString("label.newParents.text"));
        createCopiesLabel.setText(getLocalizedString("label.createCopies.text"));
        directoryLabel.setText(getLocalizedString("label.directory.text"));
        numberOfFilesLabel.setText(getLocalizedString("label.numberOfFiles.text"));
        freeSpaceOnDiskLabel.setText(getLocalizedString("label.freeSpaceOnDisk.text"));
        sizeOfFilesLabel.setText(getLocalizedString("label.sizeOfFIles.text"));
        consoleLabel.setText(getLocalizedString("label.console.text"));

        // buttons
        checkAvailabilityButton.setText(getLocalizedString("button.checkAvailability.text"));
        checkDuplicatesButton.setText(getLocalizedString("button.checkDuplicates.text"));
        changeParentsButton.setText(getLocalizedString("button.changeParents.text"));
        chooseDirectoryButton.setText(getLocalizedString("button.chooseDirectory.text"));
        createCopiesButton.setText(getLocalizedString("button.createCopies.text"));
        clearConsoleButton.setTooltip(new Tooltip(getLocalizedString("button.clearConsole.tooltip")));

        // check boxes
        keepHierarchyCheckBox.setText(getLocalizedString("checkbox.keepHierarchy.text"));

        // data table
        tableView.setPlaceholder(new Label(getLocalizedString("table.placeholder.text")));

        // columns in data table
        shortcutFilePathColumn.setText(getLocalizedString("table.column.shortcutFilePath.text"));
        originalFilePathColumn.setText(getLocalizedString("table.column.originalFilePath.text"));
        availabilityColumn.setText(getLocalizedString("table.column.availability.text"));
        actionColumn.setText(getLocalizedString("table.column.action.text"));
    }

    /**
     * Update fields and labels for total number of imported files and total size of original files.
     */
    private void updateSizeInfo() {
        // update fields
        totalNumberOfImportedFiles = windowsShortcutModel.getTotalNumberOfAvailableImportedFiles();
        totalSizeOfFiles = windowsShortcutModel.getTotalSizeOfOriginalFiles();
        // update labels
        numberOfFilesValueLabel.setText(totalNumberOfImportedFiles + "");
        sizeOfFilesValueLabel.setText(totalSizeOfFiles.toString());
    }

    /**
     * Update field and label for free space on selected disk for creating copies.
     */
    private void updateFreeSpaceInfo() {
        String destinationPath = directoryForCopiesTextField.getText();
        try {
            freeSpaceOnDisk = FileSize.getFreeDiskSpace(destinationPath);
            freeSpaceOnDiskValueLabel.setText(freeSpaceOnDisk.toString());
        } catch (IllegalAccessException e) {
            freeSpaceOnDisk = null;
            freeSpaceOnDiskValueLabel.setText("");
        }
    }

    @FXML
    public void initialize() {
        windowsShortcutModel = WindowsShortcutModel.getInstance();
        windowsShortcutModel.registerWindowsShortcutObserver(this);

        // by default, language is English
        resourceBundle = ResourceBundle.getBundle("bundles.Bundle", new Locale("en", "EN"));
        chosenLanguage = "en";
        englishMenuItem.setSelected(true);

        // enable multiple selection in table
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // add key event handling for deleting files and for clearing selection
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DELETE:
                    ObservableList<WindowsShortcutDetails> selectedItems = tableView.getSelectionModel().getSelectedItems();
                    List<String> filePathsForRemove = selectedItems
                            .stream()
                            .map(WindowsShortcutDetails::getShortcutFilePath)
                            .collect(Collectors.toList());
                    windowsShortcutModel.removeImportedFiles(filePathsForRemove);
                    break;
                case ESCAPE:
                    tableView.getSelectionModel().clearSelection();
                    break;
                default:
                    break;
            }
        });

        // add mouse event handling for clearing selection on empty row click
        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();

            // move up through the node hierarchy until a TableRow or scene root is found
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }

            // clear selection on click anywhere but on a filled row
            if (source == null || (source instanceof TableRow && ((TableRow) source).isEmpty())) {
                tableView.getSelectionModel().clearSelection();
            }
        });

        // save user selected column for sorting
        tableView.setOnSort(event -> {
            if (tableView.getSortOrder().size() == 1) {
                chosenSortingColumn = tableView.getSortOrder().get(0);
            }
        });

        // by default sorting column is file name
        shortcutFilePathColumn.setSortType(TableColumn.SortType.ASCENDING);
        chosenSortingColumn = shortcutFilePathColumn;

        // add listener for every text change on directoryForCopiesTextField
        directoryForCopiesTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFreeSpaceInfo();
        });

        initNewTableColumn();

        updateSizeInfo();
        updateFreeSpaceInfo();
        populateUIWithLocalizedStrings();
    }

    private void initNewTableColumn() {
        availabilityColumn = new TableColumn(getLocalizedString("table.column.availability.text"));
        availabilityColumn.setPrefWidth(100);
        availabilityColumn.setCellValueFactory(new PropertyValueFactory<WindowsShortcutDetails,String>("availability"));

        // add cell factory for coloring states:
        //   To obtain the TableCell we need to replace the Default CellFactory with one that returns a new TableCell instance,
        //   and @Override the updateItem(String item, boolean empty) method.
        availabilityColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<WindowsShortcutDetails, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            FileState state = FileState.fromString(item);
                            this.setTextFill(state.getColor());
                            setText(item);
                            this.setStyle("-fx-alignment: CENTER;");
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });

        actionColumn = new TableColumn(getLocalizedString("table.column.action.text"));
        actionColumn.setPrefWidth(100);
        actionColumn.setCellValueFactory(new PropertyValueFactory<WindowsShortcutDetails,String>("lastAction"));

        // add cell factory for coloring last actions:
        //   To obtain the TableCell we need to replace the Default CellFactory with one that returns a new TableCell instance,
        //   and @Override the updateItem(String item, boolean empty) method.
        actionColumn.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<WindowsShortcutDetails, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            ShortcutActionState state = ShortcutActionState.fromString(item);
                            this.setTextFill(state.getColor());
                            setText(item);
                            this.setStyle("-fx-alignment: CENTER;");
                        } else {
                            setText("");
                        }
                    }
                };
            }
        });

        // add availability column
        tableView.getColumns().addAll(availabilityColumn, actionColumn);
    }

    /**
     * Update list of parents which exist in path of all shortcut files.
     */
    private void updateDropdownParentList() {
        // get list of parents (folders in path) in hierarchy order which exist in path of all inserted files
        List<String> parents = windowsShortcutModel.getMinimumMatchingParents(true);
        ObservableList<String> choiceBoxData = FXCollections.observableArrayList();
        // add all absolute paths based on parents
        if (!parents.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String parent : parents) {
                sb.append(parent + File.separator);
                choiceBoxData.add(sb.toString());
            }
        }

        // clear new parent paths field
        newParentsTextField.clear();
        // set new parents
        ChooseParentsChoiceBox.setItems(choiceBoxData);
    }

    /**
     * Update table view with data.
     */
    private void updateTable() {
        tableData = FXCollections.observableArrayList();
        for (WindowsShortcutWrapper shortcutFile : windowsShortcutModel.getImportedFiles().values()) {
            tableData.add(WindowsShortcutDetails.deserialize(shortcutFile));
        }

        shortcutFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("shortcutFilePath"));
        originalFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("originalFilePath"));

        tableView.setItems(null);
        tableView.setItems(tableData);

        // sort table
        tableView.getSortOrder().add(chosenSortingColumn);
        tableView.sort();
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
                // if there is at least one of the dragged files
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        // add drop over event handling
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    List<File> dbFiles = db.getFiles();

                    ProgressForm progressForm = new ProgressForm(scene);
                    Task importFilesWorker = new ImportFilesWorker(windowsShortcutModel, progressForm, dbFiles);

                    // binds progress of progress form to progress of task
                    progressForm.activateProgressBar(importFilesWorker);

                    // disable all elements on scene
                    scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

                    // open progress dialog
                    progressForm.getDialogStage().show();

                    // saveFiles new thread
                    new Thread(importFilesWorker).start();
                }

                event.setDropCompleted(success);
                event.consume();
            }
        });
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
     * Check availability for original files of all imported files.
     */
    public void checkAvailability() {
        ProgressForm progressForm = new ProgressForm(scene);

        Task checkAvailabilityWorker = new CheckAvailabilityWorker(windowsShortcutModel, progressForm);

        // binds progress of progress form to progress of task:
        progressForm.activateProgressBar(checkAvailabilityWorker);

        // disable all elements on scene
        scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

        // open progress dialog
        progressForm.getDialogStage().show();

        // start new thread
        new Thread(checkAvailabilityWorker).start();
    }

    /**
     * Check if any inserted file has same original (targeting) file as any other inserted file.
     * If there is a duplicates, new dialog with duplicates will po up, otherwise message will be shown in console that there is no duplicates.
     */
    public void checkDuplicates() {
        ProgressForm progressForm = new ProgressForm(scene);

        Task checkDuplicatesWorker = new CheckDuplicatesWorker(windowsShortcutModel, progressForm);

        // binds progress of progress form to progress of task:
        progressForm.activateProgressBar(checkDuplicatesWorker);

        // disable all elements on scene
        scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

        // open progress dialog
        progressForm.getDialogStage().show();

        // start new thread
        new Thread(checkDuplicatesWorker).start();
    }

    /**
     * Get alert dialog with error information.
     * @param type AlertType - for example Warning or Error...
     * @param titleText Title text on dialog.
     * @param headerText Header text of dialog.
     * @param text Main text on dialog.
     * @param buttonType Array of buttons which will be presented on dialog.
     * @return New Alert dialog.
     */
    private Alert getAlertDialog(Alert.AlertType type, String titleText, String headerText, String text, ButtonType... buttonType) {
        Alert alert = new Alert(type, text, buttonType);
        alert.setHeaderText(headerText);
        alert.setTitle(titleText);
        alert.setResizable(false);
        alert.initStyle(StageStyle.UTILITY);
        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));

        return alert;
    }

    /**
     * Change parents of all imported shortcut files.
     */
    public void changeParents() {
        String oldParents = ChooseParentsChoiceBox.getValue(); // get selected old parents (part of shortcut path)
        String newParents = newParentsTextField.getText();  // get selected new parents
        // check conditions
        if (oldParents == null || oldParents.isEmpty() || oldParents.equals("")) {
            Alert alert = getAlertDialog(Alert.AlertType.ERROR, getLocalizedString("change.parents.title.text"), "", getLocalizedString("error.please.select.parents"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if (!FileUtil.ifFolderIsValid(newParents)) {
            Alert alert = getAlertDialog(Alert.AlertType.ERROR, getLocalizedString("change.parents.title.text"), "", getLocalizedString("error.bad.format.for.new..parents"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        if (oldParents.equals(newParents)) {
            Alert alert = getAlertDialog(Alert.AlertType.ERROR, getLocalizedString("change.parents.title.text"), "", getLocalizedString("error.old.and.new.parents.are.the.same"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // prompt warning if user really want to change parent path
        Alert alert = getAlertDialog(Alert.AlertType.CONFIRMATION, getLocalizedString("warning"), "", getLocalizedString("warning.are.you.sure.you.waant.to.change.parents"), ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.NO) {
            return;
        }

        ProgressForm progressForm = new ProgressForm(scene);

        Task checkDuplicatesWorker = new ChangeParentsWorker(windowsShortcutModel, oldParents, newParents, progressForm);

        // binds progress of progress form to progress of task:
        progressForm.activateProgressBar(checkDuplicatesWorker);

        // disable all elements on scene
        scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

        // open progress dialog
        progressForm.getDialogStage().show();

        // start new thread
        new Thread(checkDuplicatesWorker).start();
    }

    /**
     * Create copies of original (targeting) files
     */
    public void createCopies() {
        // check conditions
        if (tableData == null || tableData.isEmpty()) {
            Alert alert = getAlertDialog(Alert.AlertType.ERROR, getLocalizedString("create.copies.title.text"), "", getLocalizedString("no.files.imported"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        String destinationPath = directoryForCopiesTextField.getText();
        if (!FileUtil.ifFolderIsValid(destinationPath)) {
            Alert alert = getAlertDialog(Alert.AlertType.ERROR, getLocalizedString("create.copies.title.text"), "", getLocalizedString("error.bad.destination.folder"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // check if there is enough space on selected disk
        if (freeSpaceOnDisk.getSizeInBytes() < totalSizeOfFiles.getSizeInBytes()) {
            Alert alert = getAlertDialog(Alert.AlertType.ERROR, getLocalizedString("create.copies.title.text"), "", getLocalizedString("error.not.enough.space.on.disk"), ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // prompt warning if user really want to save
        Alert alert = getAlertDialog(Alert.AlertType.CONFIRMATION, getLocalizedString("warning"), "", getLocalizedString("warning.are.you.sure.you.waant.to.create.copies"), ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.NO) {
            return;
        }

        ProgressForm progressForm = new ProgressForm(scene);

        Task checkDuplicatesWorker = new CreateCopiesWorker(windowsShortcutModel, destinationPath, keepHierarchyCheckBox.isSelected(), progressForm);

        // binds progress of progress form to progress of task:
        progressForm.activateProgressBar(checkDuplicatesWorker);

        // disable all elements on scene
        scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

        // open progress dialog
        progressForm.getDialogStage().show();

        // start new thread
        new Thread(checkDuplicatesWorker).start();
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
        String currentTime = TimeUtil.getCurrentTimeString();

        WindowsShortcutModelState lastModelState = windowsShortcutModel.getLastModelState();
        switch (lastModelState) {
            case IMPORTED:
                if (windowsShortcutModel.getLastFailedLoadingFiles().size() > 0) {
                    addErrorsOnConsole(currentTime,"files.cannot.be.imported", windowsShortcutModel.getLastFailedLoadingFiles());
                } else {
                    Text importFilesSuccessText = new Text(currentTime + getLocalizedString("files.imported.successfully") + "\n");
                    importFilesSuccessText.setFill(Color.BLUE);
                    consoleTextFlow.getChildren().addAll(importFilesSuccessText);
                }

                break;
            case REMOVED:
                Text removedFilesSuccessText = new Text(currentTime + getLocalizedString("files.removed.successfully") + "\n");
                removedFilesSuccessText.setFill(Color.ORANGE);
                consoleTextFlow.getChildren().addAll(removedFilesSuccessText);

                break;
            case CHECKED_AVAILABILITY:
                Text checkedAvailabilitySuccessText = new Text(currentTime + getLocalizedString("files.checkedAvailability.successfully") + "\n");
                checkedAvailabilitySuccessText.setFill(Color.GREEN);
                consoleTextFlow.getChildren().addAll(checkedAvailabilitySuccessText);

                break;
            case CHECKED_DUPLICATES:
                Text checkedDuplicatesSuccessText = new Text(currentTime + getLocalizedString("files.checkedDuplicates.successfully") + "\n");
                checkedDuplicatesSuccessText.setFill(Color.GREEN);
                consoleTextFlow.getChildren().addAll(checkedDuplicatesSuccessText);

                break;
            case REMOVED_DUPLICATES:
                Text removedDuplicatesSuccessText = new Text(currentTime + getLocalizedString("files.manipulationWithDuplicates.successfully") + "\n");
                removedDuplicatesSuccessText.setFill(Color.GREEN);
                consoleTextFlow.getChildren().addAll(removedDuplicatesSuccessText);

                break;
            case CHANGED_ROOTS:
                if (!windowsShortcutModel.getLastFailedSavedFiles().isEmpty()) {
                    addErrorsOnConsole(currentTime,"files.cannot.changed.parents", windowsShortcutModel.getLastFailedSavedFiles());
                } else {
                    Text importFilesSuccessText = new Text(currentTime + getLocalizedString("files.changedParentsSuccessfully") + "\n");
                    importFilesSuccessText.setFill(Color.GREEN);
                    consoleTextFlow.getChildren().addAll(importFilesSuccessText);
                }

                break;

            case CREATED_COPIES:
                if (!windowsShortcutModel.getLastFailedSavedFiles().isEmpty()) {
                    addErrorsOnConsole(currentTime,"files.cannot.be.saved", windowsShortcutModel.getLastFailedSavedFiles());
                } else {
                    Text importFilesSuccessText = new Text(currentTime + getLocalizedString("files.saved.successfully") + "\n");
                    importFilesSuccessText.setFill(Color.GREEN);
                    consoleTextFlow.getChildren().addAll(importFilesSuccessText);
                }

                break;

            default:
                break;
        }

        scrollPane.setVvalue(1.0); // 1.0 means 100% at the bottom
    }

    /**
     * Open file chooser dialog and import selected files into model.
     */
    public void openFiles() {
        FileChooser fileChooser = new FileChooser();
        // set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(Constants.LNK_FILE_TYPE_DESCRIPTION, "*" + Constants.LNK_FILE_TYPE_EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);
        // Open dialog for choosing shortcut files
        List<File> files = fileChooser.showOpenMultipleDialog(tableView.getScene().getWindow());
        if (files != null) {
            ProgressForm progressForm = new ProgressForm(scene);
            Task importFilesWorker = new ImportFilesWorker(windowsShortcutModel, progressForm, files);

            // binds progress of progress form to progress of task
            progressForm.activateProgressBar(importFilesWorker);

            // disable all elements on scene
            scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

            // open progress dialog
            progressForm.getDialogStage().show();

            // saveFiles new thread
            new Thread(importFilesWorker).start();
        }
    }

    /**
     * Open directory chooser dialog and import all ".lnk" files from chosen directory into model.
     */
    public void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(tableView.getScene().getWindow());
        if (selectedDirectory != null) {
            File[] selectedFiles = selectedDirectory.listFiles();
            List<File> filesForImport = Arrays.asList(selectedFiles);

            ProgressForm progressForm = new ProgressForm(scene);
            Task importFilesWorker = new ImportFilesWorker(windowsShortcutModel, progressForm, filesForImport);

            // binds progress of progress form to progress of task
            progressForm.activateProgressBar(importFilesWorker);

            // disable all elements on scene
            scene.getRoot().getChildrenUnmodifiable().forEach(c -> c.setDisable(true));

            // open progress dialog
            progressForm.getDialogStage().show();

            // saveFiles new thread
            new Thread(importFilesWorker).start();
        }
    }

    /**
     * Choose folder for saving original (targeting) files copies.
     */
    public void chooseFolderForSaving() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(tableView.getScene().getWindow());
        if (selectedDirectory != null) {
            directoryForCopiesTextField.setText(selectedDirectory.getPath());
        }
    }

    @Override
    public void onImportedFilesChanged() {
        Platform.runLater(() -> {
            updateDropdownParentList();
            updateTable();
            addInfoOnConsole();
            updateSizeInfo();
        });
    }

    @Override
    public void onCheckedAvailability() {
        Platform.runLater(() -> {
            updateTable();
            addInfoOnConsole();
        });
    }

    @Override
    public void onCheckedDuplicates() {
        Platform.runLater(() -> {
            if (windowsShortcutModel.ifSomeFilesAreDuplicates()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dialog_remove_copies.fxml"));
                try {
                    Parent rootNode = loader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(rootNode));
                    stage.setTitle(getLocalizedString("dialog.removeCopies.title"));
                    stage.initStyle(StageStyle.UTILITY);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setResizable(true);
                    //stage.resizableProperty().setValue(Boolean.TRUE);
                    stage.setOnCloseRequest(e -> windowsShortcutModel.finishRemoveDuplicates());
                    stage.showAndWait();

                    updateTable();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            addInfoOnConsole();
        });
    }

    @Override
    public void onChangedRoot() {
        Platform.runLater(() -> {
            updateDropdownParentList();
            updateTable();
            addInfoOnConsole();
        });
    }

    @Override
    public void onCreateCopies() {
        Platform.runLater(() -> {
            updateTable();
            addInfoOnConsole();
        });
    }
}
