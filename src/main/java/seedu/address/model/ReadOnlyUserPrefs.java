package seedu.address.model;

import seedu.address.commons.core.GuiSettings;

/**
 * Unmodifiable view of user prefs.
 */
public interface ReadOnlyUserPrefs {

    GuiSettings getGuiSettings();

    /** Returns whether privacy mode is enabled. */
    boolean isPrivacyModeEnabled();

}
