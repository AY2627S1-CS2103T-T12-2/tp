package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Unpins a person identified using their displayed index.
 */
public class UnpinCommand extends Command {

    public static final String COMMAND_WORD = "unpin";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Unpins the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_UNPIN_PERSON_SUCCESS = "Unpinned student: %1$s";
    public static final String MESSAGE_ALREADY_UNPINNED = "This student is already unpinned.";

    private final Index targetIndex;

    public UnpinCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUnpin = lastShownList.get(targetIndex.getZeroBased());
        if (!personToUnpin.isPinned()) {
            throw new CommandException(MESSAGE_ALREADY_UNPINNED);
        }

        model.setPerson(personToUnpin, personToUnpin.withPinned(false));
        return new CommandResult(String.format(MESSAGE_UNPIN_PERSON_SUCCESS, personToUnpin.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UnpinCommand otherUnpinCommand)) {
            return false;
        }
        return targetIndex.equals(otherUnpinCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
