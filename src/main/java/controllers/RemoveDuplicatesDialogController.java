package main.java.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private TableColumn<DuplicateFileDetails, String> selectionColumn;

    @FXML
    public void initialize() {
        resourceBundle = MainScreenController.getResourceBundle();
        windowsShortcutModel = WindowsShortcutModel.getInstance();
        updateTable();
    }

    private void updateTable() {
        List<DuplicateFileInfo> duplicateFiles = windowsShortcutModel.getDuplicateFiles();
        tableData = FXCollections.observableArrayList();
        duplicateFiles.stream().forEach(f -> tableData.add(DuplicateFileDetails.deserialize(f)));

        shortcutFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("shortcutFilePath"));
        originalFilePathColumn.setCellValueFactory(new PropertyValueFactory<>("originalFilePath"));
        selectionColumn.setCellValueFactory(new PropertyValueFactory<>("select"));

        tableView.setItems(null);
        tableView.setItems(tableData);

//        // sort table
//        tableView.getSortOrder().add(chosenSortingColumn);
//        tableView.sort();
    }
}
