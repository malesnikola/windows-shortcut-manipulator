package main.java.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.StageStyle;
import main.java.domain.*;
import main.java.model.WindowsShortcutModel;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RemoveDuplicatesDialogController implements WindowsShortcutModel.ManipulationWithDuplicatesObserver {
    private static Logger logger = Logger.getLogger(RemoveDuplicatesDialogController.class);

    private ResourceBundle resourceBundle;

    private WindowsShortcutModel windowsShortcutModel;

    // data table
    private ObservableList<DuplicateFileDetails> tableData;
    @FXML
    private TableView<DuplicateFileDetails> tableView;
    @FXML
    private TableColumn<DuplicateFileDetails, Boolean> selectionColumn;
    @FXML
    private TableColumn<DuplicateFileDetails, String> shortcutFilePathColumn;
    @FXML
    private TableColumn<DuplicateFileDetails, String> originalFilePathColumn;

    private TableColumn chosenSortingColumn1;
    private TableColumn chosenSortingColumn2;

    // label
    @FXML
    private Label listOfDuplicatesLabel;

    // button
    @FXML
    private Button removeDuplicatesButton;

    // checkbox for selecting all
    private CheckBox selectAllCheckBox;

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
        // labels
        listOfDuplicatesLabel.setText(getLocalizedString("dialog.removeCopies.label.listOfDuplicates.text"));

        // buttons
        removeDuplicatesButton.setText(getLocalizedString("dialog.removeCopies.button.removeDuplicates.text"));

        // data table
        tableView.setPlaceholder(new Label(getLocalizedString("table.placeholder.text")));

        // columns in data table
        selectionColumn.setText(getLocalizedString("dialog.removeCopies.table.column.select.text"));
        shortcutFilePathColumn.setText(getLocalizedString("dialog.removeCopies.table.column.shortcutFile.text"));
        originalFilePathColumn.setText(getLocalizedString("dialog.removeCopies.table.column.originalFile.text"));
    }

    @FXML
    public void initialize() {
        resourceBundle = MainScreenController.getResourceBundle();
        windowsShortcutModel = WindowsShortcutModel.getInstance();
        windowsShortcutModel.registerManipulationWithDuplicatesObserver(this);
        tableView.setEditable(true);

        // add key event handling for clearing selection
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
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
                chosenSortingColumn1 = tableView.getSortOrder().get(0);
                if (tableView.getSortOrder().size() > 1) {
                    chosenSortingColumn2 = tableView.getSortOrder().get(1);
                }
            }
        });

        // by default sorting column is original file name and shortcut file name
        shortcutFilePathColumn.setSortType(TableColumn.SortType.ASCENDING);
        chosenSortingColumn1 = originalFilePathColumn;
        chosenSortingColumn2 = shortcutFilePathColumn;

        populateUIWithLocalizedStrings();
        updateTable();
    }

    public void selectAllBoxes(ActionEvent e) {
        // iterate through all items in ObservableList
        for (DuplicateFileDetails item : tableData) {
            // and change "selected" boolean
            item.setIsSelected(((CheckBox) e.getSource()).isSelected());
        }
    }

    private void updateTable() {
        Map<String, String> duplicateFiles = windowsShortcutModel.getDuplicateFiles();
        tableData = FXCollections.observableArrayList();
        duplicateFiles.forEach((k, v) -> tableData.add(new DuplicateFileDetails(k, v)));

        selectAllCheckBox = new CheckBox();
        selectionColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
        selectionColumn.setGraphic(selectAllCheckBox);

        // select all checkboxes when checkbox in header is pressed
        selectAllCheckBox.setOnAction(e -> selectAllBoxes(e));

        shortcutFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("shortcutFilePath"));
        originalFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("originalFilePath"));
        selectionColumn.setCellValueFactory(new PropertyValueFactory<>("isSelected"));

        tableView.setItems(null);
        tableView.setItems(tableData);

        // sort table
        tableView.getSortOrder().clear();
        tableView.getSortOrder().add(chosenSortingColumn1);
        tableView.getSortOrder().add(chosenSortingColumn2);
        tableView.sort();
    }

    private List<String> getSelectedFiles() {
        return tableData.stream()
                .filter(file -> file.isIsSelected())
                .map(file -> file.getShortcutFilePath())
                .collect(Collectors.toList());
    }

    public void removeDuplicates() {
        windowsShortcutModel.removeDuplicateFiles(getSelectedFiles());
    }

    @Override
    public void onRemovedDuplicates() {
        Platform.runLater(() -> {
            if (windowsShortcutModel.ifSomeFilesFailedRemoved()) {
                List<FailedFileDetails> failedRemovedFiles = windowsShortcutModel.getLastFailedRemovedFiles();
                String fileSeparator = System.getProperty("line.separator");
                StringBuilder stringBuilder = new StringBuilder();
                for (FailedFileDetails file : failedRemovedFiles) {
                    stringBuilder.append(file.getFilePath() + " - " + file.getErrorMessage() + fileSeparator);
                }

                stringBuilder.append(fileSeparator + getLocalizedString("dialog.removeCopies.message.pleaseTryAgain"));

                Alert alert = new Alert(Alert.AlertType.ERROR, stringBuilder.toString(), ButtonType.OK);
                alert.setHeaderText(getLocalizedString("dialog.removeCopies.message.theseFiles."));
                alert.setTitle(getLocalizedString("dialog.removeCopies.message.errorRemove"));
                alert.setResizable(true);
                alert.initStyle(StageStyle.UTILITY);
                alert.getDialogPane().getChildren().stream()
                        .filter(node -> node instanceof Label)
                        .forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
                alert.showAndWait();
            }

            updateTable();
        });
    }
}
