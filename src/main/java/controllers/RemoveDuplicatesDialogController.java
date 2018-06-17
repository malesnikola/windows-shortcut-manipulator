package main.java.controllers;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import main.java.domain.*;
import main.java.model.WindowsShortcutModel;
import org.apache.log4j.Logger;

import java.util.LinkedList;
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
    private TableColumn<DuplicateFileDetails, String> shortcutFilePathColumn;
    @FXML
    private TableColumn<DuplicateFileDetails, String> originalFilePathColumn;
    @FXML
    private TableColumn<DuplicateFileDetails, Boolean> selectionColumn;

    // button
    @FXML
    private Button removeDuplicatesButton;

    //Create checkbox
    private CheckBox select_all;

    @FXML
    public void initialize() {
        resourceBundle = MainScreenController.getResourceBundle();
        windowsShortcutModel = WindowsShortcutModel.getInstance();
        windowsShortcutModel.registerManipulationWithDuplicatesObserver(this);
        updateTable();
    }

    public void selectAllBoxes(ActionEvent e) {
        //Iterate through all items in ObservableList
        for (DuplicateFileDetails item : tableData) {
            //And change "selected" boolean
            item.setIsSelected(((CheckBox) e.getSource()).isSelected());
        }
    }

    private void updateTable() {
        Map<String, String> duplicateFiles = windowsShortcutModel.getDuplicateFiles();
        tableData = FXCollections.observableArrayList();
        duplicateFiles.forEach((k, v) -> tableData.add(new DuplicateFileDetails(k, v)));

        tableView.setEditable(true);
        //selectionColumn.setEditable(true);

        select_all = new CheckBox();
        selectionColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectionColumn));
        selectionColumn.setGraphic(select_all);
        //Select all checkboxes when checkbox in header is pressed
        select_all.setOnAction(e -> selectAllBoxes(e));

        shortcutFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("shortcutFilePath"));
        originalFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("originalFilePath"));
        selectionColumn.setCellValueFactory(new PropertyValueFactory<>("isSelected"));

        tableView.setItems(null);
        tableView.setItems(tableData);

//        // sort table
//        tableView.getSortOrder().add(chosenSortingColumn);
//        tableView.sort();
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
                StringBuilder stringBuilder = new StringBuilder("These files couldn't be removed:" + fileSeparator);
                for (FailedFileDetails file : failedRemovedFiles) {
                    stringBuilder.append(file.getFilePath() + " - " + file.getErrorMessage() + fileSeparator);
                }

                stringBuilder.append(fileSeparator + "Please try again.");

                Alert alert = new Alert(Alert.AlertType.INFORMATION, stringBuilder.toString(), ButtonType.OK);
                alert.showAndWait();
            }

            updateTable();
        });
    }
}
