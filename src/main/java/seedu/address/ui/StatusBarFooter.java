package seedu.address.ui;

import java.nio.file.Path;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * A UI for the status bar that is displayed at the footer of the application.
 */
public class StatusBarFooter extends UiPart<Region> {

    public static final String PRIVACY_STATUS_TEXT = "Privacy mode enabled";

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label saveLocationStatus;

    @FXML
    private Label privacyStatus;

    /**
     * Creates a {@code StatusBarFooter} with the given {@code Path}.
     */
    public StatusBarFooter(Path saveLocation) {
        this(saveLocation, false);
    }

    /**
     * Creates a {@code StatusBarFooter} with the given path and privacy state.
     */
    public StatusBarFooter(Path saveLocation, boolean privacyModeEnabled) {
        super(FXML);
        saveLocationStatus.setText(saveLocation.toAbsolutePath().normalize().toString());
        privacyStatus.setText(PRIVACY_STATUS_TEXT);
        setPrivacyModeEnabled(privacyModeEnabled);
    }

    /** Shows or hides the persistent privacy mode indicator. */
    public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
        privacyStatus.setVisible(privacyModeEnabled);
        privacyStatus.setManaged(privacyModeEnabled);
    }

}
