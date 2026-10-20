package seedu.address.logic.commands;

import seedu.address.model.Model;

/** Opens the read-only list of independently stored classes. */
public class ClassListCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "list";
    public static final String MESSAGE_USAGE = "class list: Shows all classes and their stable IDs.";
    public static final String MESSAGE_SUCCESS = "Showing %d class(es).";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(String.format(MESSAGE_SUCCESS, model.getTuitionClassList().size()),
                CommandResult.ViewChange.CLASS_LIST);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassListCommand;
    }
}
