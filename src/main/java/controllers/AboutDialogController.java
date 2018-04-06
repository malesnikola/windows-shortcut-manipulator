package main.java.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import main.java.util.Constants;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

public class AboutDialogController {
    private static Logger logger = Logger.getLogger(MainScreenController.class);

    private ResourceBundle resourceBundle;

    @FXML
    private Label appNameLabel;
    @FXML
    private Label appVersionLabel;
    @FXML
    private Label copyrightLabel;

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
        appNameLabel.setText(getLocalizedString("dialog.about.appname.text"));
        appVersionLabel.setText(getLocalizedString("dialog.about.appversion.text") + " " + Constants.APP_VERSION);
        copyrightLabel.setText(getLocalizedString("dialog.about.copyright.text"));
    }

    @FXML
    public void initialize() {
        resourceBundle = MainScreenController.getResourceBundle();
        populateUIWithLocalizedStrings();
    }

}
