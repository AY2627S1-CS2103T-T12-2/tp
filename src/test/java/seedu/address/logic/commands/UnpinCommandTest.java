package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class UnpinCommandTest {

    private Person pinnedPerson;
    private Model model;

    @BeforeEach
    public void setUp() {
        pinnedPerson = new PersonBuilder().withPinned(true).build();
        model = new ModelManager(new AddressBookBuilder().withPerson(pinnedPerson).build(), new UserPrefs());
    }

    @Test
    public void execute_validIndex_success() {
        UnpinCommand unpinCommand = new UnpinCommand(INDEX_FIRST_PERSON);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(pinnedPerson, pinnedPerson.withPinned(false));
        String expectedMessage = String.format(UnpinCommand.MESSAGE_UNPIN_PERSON_SUCCESS, pinnedPerson.getName());

        assertCommandSuccess(unpinCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new UnpinCommand(outOfBoundsIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_alreadyUnpinned_throwsCommandException() {
        Person unpinnedPerson = new PersonBuilder().build();
        model = new ModelManager(new AddressBookBuilder().withPerson(unpinnedPerson).build(), new UserPrefs());

        assertCommandFailure(new UnpinCommand(INDEX_FIRST_PERSON), model,
                UnpinCommand.MESSAGE_ALREADY_UNPINNED);
    }

    @Test
    public void equals() {
        UnpinCommand unpinFirstCommand = new UnpinCommand(INDEX_FIRST_PERSON);

        assertTrue(unpinFirstCommand.equals(new UnpinCommand(INDEX_FIRST_PERSON)));
        assertTrue(unpinFirstCommand.equals(unpinFirstCommand));
        assertFalse(unpinFirstCommand.equals(null));
        assertFalse(unpinFirstCommand.equals(new ClearCommand()));
        assertFalse(unpinFirstCommand.equals(new UnpinCommand(INDEX_SECOND_PERSON)));
    }

    @Test
    public void toStringMethod() {
        UnpinCommand unpinCommand = new UnpinCommand(INDEX_FIRST_PERSON);
        String expected = UnpinCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, unpinCommand.toString());
    }
}
