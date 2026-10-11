package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/** Switches the application to the read-only timetable view. */
public class TimetableCommand extends Command {

    public static final String COMMAND_WORD = "timetable";
    public static final String MESSAGE_SUCCESS = "Showing timetable.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.ViewChange.TIMETABLE);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TimetableCommand;
    }

    @Override
    public int hashCode() {
        return TimetableCommand.class.hashCode();
    }
}
