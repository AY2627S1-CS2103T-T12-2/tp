package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "All students, classes, and enrolments have been cleared.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        AddressBook clearedAddressBook = new AddressBook();
        clearedAddressBook.setNextClassSequence(model.getAddressBook().getNextClassSequence());
        model.setAddressBook(clearedAddressBook);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
