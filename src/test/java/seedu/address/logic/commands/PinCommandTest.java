package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class PinCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToPin = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        PinCommand pinCommand = new PinCommand(INDEX_FIRST_PERSON);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToPin, personToPin.withPinned(true));
        String expectedMessage = String.format(PinCommand.MESSAGE_PIN_PERSON_SUCCESS, personToPin.getName());

        assertCommandSuccess(pinCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person personToPin = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        PinCommand pinCommand = new PinCommand(INDEX_FIRST_PERSON);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(personToPin, personToPin.withPinned(true));
        String expectedMessage = String.format(PinCommand.MESSAGE_PIN_PERSON_SUCCESS, personToPin.getName());

        assertCommandSuccess(pinCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexScoredSearch_preservesEqualScoreOrder() throws Exception {
        Person anna = new PersonBuilder().withName("Anna").withPinned(true).build();
        Person ada = new PersonBuilder().withName("Ada").build();
        Person ava = new PersonBuilder().withName("Ava").build();
        model = new ModelManager(new AddressBookBuilder()
                .withPerson(anna)
                .withPerson(ada)
                .withPerson(ava)
                .build(), new UserPrefs());
        new FindCommand(new NameContainsKeywordsPredicate(List.of("a"))).execute(model);

        new PinCommand(Index.fromOneBased(3)).execute(model);

        assertEquals(List.of(anna, ava.withPinned(true), ada), model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new PinCommand(outOfBoundsIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_alreadyPinned_throwsCommandException() {
        Person pinnedPerson = new PersonBuilder().withPinned(true).build();
        model = new ModelManager(new AddressBookBuilder().withPerson(pinnedPerson).build(), new UserPrefs());

        assertCommandFailure(new PinCommand(INDEX_FIRST_PERSON), model, PinCommand.MESSAGE_ALREADY_PINNED);
    }

    @Test
    public void equals() {
        PinCommand pinFirstCommand = new PinCommand(INDEX_FIRST_PERSON);

        assertTrue(pinFirstCommand.equals(new PinCommand(INDEX_FIRST_PERSON)));
        assertTrue(pinFirstCommand.equals(pinFirstCommand));
        assertFalse(pinFirstCommand.equals(null));
        assertFalse(pinFirstCommand.equals(new ClearCommand()));
        assertFalse(pinFirstCommand.equals(new PinCommand(INDEX_SECOND_PERSON)));
    }

    @Test
    public void toStringMethod() {
        PinCommand pinCommand = new PinCommand(INDEX_FIRST_PERSON);
        String expected = PinCommand.class.getCanonicalName() + "{targetIndex=" + INDEX_FIRST_PERSON + "}";
        assertEquals(expected, pinCommand.toString());
    }
}
