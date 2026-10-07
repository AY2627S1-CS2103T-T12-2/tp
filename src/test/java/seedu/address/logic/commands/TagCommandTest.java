package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
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
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class TagCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_multipleTagsUnfilteredList_success() {
        Set<Tag> tagsToAdd = getTagSet("exam-prep", "needs-follow-up");
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, tagsToAdd);
        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person taggedPerson = new PersonBuilder(personToTag)
                .withTags("friends", "exam-prep", "needs-follow-up").build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToTag, taggedPerson);
        String expectedMessage = String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS,
                personToTag.getName(), "exam-prep, needs-follow-up");

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("exam-prep"));
        Person taggedPerson = new PersonBuilder(personToTag)
                .withTags("owes-money", "friends", "exam-prep").build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(personToTag, taggedPerson);
        String expectedMessage = String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS,
                personToTag.getName(), "exam-prep");

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_existingTag_failure() {
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("friends"));

        assertCommandFailure(tagCommand, model, TagCommand.MESSAGE_DUPLICATE_TAG);
    }

    @Test
    public void execute_tagLimitExceeded_failure() {
        Person personWithTenTags = new PersonBuilder()
                .withTags("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")
                .build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(personWithTenTags).build();
        model = new ModelManager(addressBook, new UserPrefs());
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("eleven"));

        assertCommandFailure(tagCommand, model, Tag.MESSAGE_TAG_LIMIT);
    }

    @Test
    public void execute_invalidPersonIndex_failure() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        TagCommand tagCommand = new TagCommand(outOfBoundsIndex, getTagSet("exam-prep"));

        assertCommandFailure(tagCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        TagCommand standardCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("exam-prep"));

        assertTrue(standardCommand.equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("exam-prep"))));
        assertTrue(standardCommand.equals(standardCommand));
        assertFalse(standardCommand.equals(null));
        assertFalse(standardCommand.equals(new ClearCommand()));
        assertFalse(standardCommand.equals(new TagCommand(INDEX_SECOND_PERSON, getTagSet("exam-prep"))));
        assertFalse(standardCommand.equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("urgent"))));
    }

    private static Set<Tag> getTagSet(String... tagNames) {
        Set<Tag> tags = new LinkedHashSet<>();
        List.of(tagNames).stream().map(Tag::new).forEach(tags::add);
        return tags;
    }
}
