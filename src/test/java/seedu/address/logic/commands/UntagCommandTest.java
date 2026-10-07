package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class UntagCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_removeTag_success() {
        UntagCommand untagCommand = new UntagCommand(INDEX_SECOND_PERSON, getTagSet("owes-money"));
        Person personToUntag = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person untaggedPerson = new PersonBuilder(personToUntag).withTags("friends").build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToUntag, untaggedPerson);
        String expectedMessage = String.format(UntagCommand.MESSAGE_UNTAG_PERSON_SUCCESS,
                personToUntag.getName(), "owes-money");

        assertCommandSuccess(untagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_removeAllTags_success() {
        UntagCommand untagCommand = new UntagCommand(INDEX_SECOND_PERSON);
        Person personToUntag = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person untaggedPerson = new PersonBuilder(personToUntag).withTags().build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToUntag, untaggedPerson);
        String expectedMessage = String.format(UntagCommand.MESSAGE_UNTAG_ALL_SUCCESS, personToUntag.getName());

        assertCommandSuccess(untagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_missingTag_failure() {
        UntagCommand untagCommand = new UntagCommand(INDEX_FIRST_PERSON, getTagSet("exam-prep"));

        assertCommandFailure(untagCommand, model, UntagCommand.MESSAGE_TAG_NOT_FOUND);
    }

    @Test
    public void execute_invalidPersonIndex_failure() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        UntagCommand untagCommand = new UntagCommand(outOfBoundsIndex, getTagSet("friends"));

        assertCommandFailure(untagCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        UntagCommand standardCommand = new UntagCommand(INDEX_FIRST_PERSON, getTagSet("friends"));

        assertTrue(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON, getTagSet("friends"))));
        assertTrue(standardCommand.equals(standardCommand));
        assertFalse(standardCommand.equals(null));
        assertFalse(standardCommand.equals(new ClearCommand()));
        assertFalse(standardCommand.equals(new UntagCommand(INDEX_SECOND_PERSON, getTagSet("friends"))));
        assertFalse(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON, getTagSet("urgent"))));
        assertFalse(standardCommand.equals(new UntagCommand(INDEX_FIRST_PERSON)));
    }

    private static Set<Tag> getTagSet(String... tagNames) {
        Set<Tag> tags = new LinkedHashSet<>();
        List.of(tagNames).stream().map(Tag::new).forEach(tags::add);
        return tags;
    }
}
