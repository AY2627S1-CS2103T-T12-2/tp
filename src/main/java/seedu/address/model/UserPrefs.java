package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.core.GuiSettings;

/**
 * Represents User's preferences.
 */
public class UserPrefs implements ReadOnlyUserPrefs {

    private GuiSettings guiSettings = new GuiSettings();
    private boolean privacyModeEnabled;

    /**
     * Creates a {@code UserPrefs} with default values.
     */
    public UserPrefs() {}

    /**
     * Creates a {@code UserPrefs} with the prefs in {@code userPrefs}.
     */
    public UserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        setGuiSettings(userPrefs.getGuiSettings());
        setPrivacyModeEnabled(userPrefs.isPrivacyModeEnabled());
    }

    public GuiSettings getGuiSettings() {
        return guiSettings;
    }

    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        this.guiSettings = guiSettings;
    }

    @Override
    public boolean isPrivacyModeEnabled() {
        return privacyModeEnabled;
    }

    public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
        this.privacyModeEnabled = privacyModeEnabled;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UserPrefs otherUserPrefs)) {
            return false;
        }

        return guiSettings.equals(otherUserPrefs.guiSettings)
                && privacyModeEnabled == otherUserPrefs.privacyModeEnabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guiSettings, privacyModeEnabled);
    }

    @Override
    public String toString() {
        return "Gui Settings : " + guiSettings + "; Privacy Mode Enabled : " + privacyModeEnabled;
    }

}
