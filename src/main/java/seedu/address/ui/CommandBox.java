package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";
    private static final String FIND_FORMAT_HINT =
            "find KEYWORD | find t/TAG (e.g. priority) | find c/DAY TIME (e.g. Monday 1800)";
    private static final String FIND_TAG_HINT = "find t/TAG (e.g. priority)";
    private static final String FIND_LESSON_HINT = "find c/DAY TIME (e.g. Monday 1800; time uses HHmm)";
    private static final String FIND_HOME_HINT = "Type 'list' to return to the home page.";

    private final CommandExecutor commandExecutor;

    @FXML
    private TextField commandTextField;

    @FXML
    private TextField commandSuggestionTextField;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> {
            setStyleToDefault();
            updateCommandSuggestion();
        });
        updateCommandSuggestion();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
            commandSuggestionTextField.setText(isFindCommand(commandText) ? FIND_HOME_HINT : "");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    private void updateCommandSuggestion() {
        commandSuggestionTextField.setText(getCommandSuggestion(commandTextField.getText()));
    }

    static String getCommandSuggestion(String commandText) {
        if (commandText == null || commandText.isEmpty()) {
            return "";
        }

        if ("find".startsWith(commandText) && commandText.length() < "find".length()) {
            return "find";
        }

        if (commandText.equals("find") || commandText.equals("find ")) {
            return FIND_FORMAT_HINT;
        }

        if (commandText.equals("find t/")) {
            return FIND_TAG_HINT;
        }

        if (commandText.equals("find c/")) {
            return FIND_LESSON_HINT;
        }

        if (commandText.equals("t/")) {
            return "t/TAG (e.g. priority)";
        }

        if (commandText.equals("c/")) {
            return "c/DAY TIME (e.g. Monday 1800; time uses HHmm)";
        }

        return "";
    }

    static boolean isFindCommand(String commandText) {
        String trimmedCommandText = commandText.trim();
        return trimmedCommandText.equals(FindCommand.COMMAND_WORD)
                || trimmedCommandText.startsWith(FindCommand.COMMAND_WORD + " ");
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
