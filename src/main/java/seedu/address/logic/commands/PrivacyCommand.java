package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.model.Model;

/** Enables or disables privacy mode. */
public class PrivacyCommand extends Command {

    public static final String COMMAND_WORD = "privacy";
    public static final String MODE_ON = "on";
    public static final String MODE_OFF = "off";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Turns privacy mode on or off.\n"
            + "Parameters: MODE (must be 'on' or 'off')\n"
            + "Example: " + COMMAND_WORD + " " + MODE_ON;

    public static final String MESSAGE_PRIVACY_MODE_ENABLED = "Privacy mode is now enabled.";
    public static final String MESSAGE_PRIVACY_MODE_DISABLED = "Privacy mode is now disabled.";

    private final boolean privacyModeEnabled;

    public PrivacyCommand(boolean privacyModeEnabled) {
        this.privacyModeEnabled = privacyModeEnabled;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setPrivacyModeEnabled(privacyModeEnabled);
        return new CommandResult(privacyModeEnabled
                ? MESSAGE_PRIVACY_MODE_ENABLED
                : MESSAGE_PRIVACY_MODE_DISABLED);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof PrivacyCommand otherCommand
                && privacyModeEnabled == otherCommand.privacyModeEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privacyModeEnabled);
    }
}
