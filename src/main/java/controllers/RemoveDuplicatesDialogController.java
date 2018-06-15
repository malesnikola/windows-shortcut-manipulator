package main.java.controllers;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import main.java.domain.DuplicateFileDetails;
import main.java.domain.DuplicateFileInfo;
import main.java.domain.WindowsShortcutDetails;
import main.java.domain.WindowsShortcutWrapper;
import main.java.model.WindowsShortcutModel;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ResourceBundle;

public class RemoveDuplicatesDialogController {
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

    //Create checkbox
    private CheckBox select_all = new CheckBox();

    @FXML
    public void initialize() {
        resourceBundle = MainScreenController.getResourceBundle();
        windowsShortcutModel = WindowsShortcutModel.getInstance();
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
        List<DuplicateFileInfo> duplicateFiles = windowsShortcutModel.getDuplicateFiles();
        tableData = FXCollections.observableArrayList();
        duplicateFiles.stream().forEach(f -> tableData.add(DuplicateFileDetails.deserialize(f)));

        tableView.setEditable(true);
        //selectionColumn.setEditable(true);

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
}
