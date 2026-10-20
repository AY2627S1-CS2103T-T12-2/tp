package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
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
    public void execute_batchAllEligible_addsTagToEverySelectedStudent() {
        Tag tagToAdd = new Tag("math-2026");
        Person firstStudent = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondStudent = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        TagCommand tagCommand = new TagCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), tagToAdd);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstStudent, addTag(firstStudent, tagToAdd));
        expectedModel.setPerson(secondStudent, addTag(secondStudent, tagToAdd));
        String expectedMessage = String.format(TagCommand.MESSAGE_BATCH_SUCCESS, tagToAdd.tagName,
                firstStudent.getName() + ", " + secondStudent.getName(),
                TagCommand.MESSAGE_NO_STUDENTS, TagCommand.MESSAGE_NO_STUDENTS);

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_batchMixedEligibility_updatesEligibleStudentsAndReportsEveryOutcome() {
        Tag tagToAdd = new Tag("friends");
        Person alreadyTaggedStudent = model.getFilteredPersonList().get(0);
        Person originalSecondStudent = model.getFilteredPersonList().get(1);
        Person studentAtTagLimit = new PersonBuilder(originalSecondStudent)
                .withTags("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")
                .build();
        model.setPerson(originalSecondStudent, studentAtTagLimit);
        Person eligibleStudent = model.getFilteredPersonList().get(2);
        TagCommand tagCommand = new TagCommand(
                List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, Index.fromOneBased(3)), tagToAdd);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(eligibleStudent, addTag(eligibleStudent, tagToAdd));
        String expectedMessage = String.format(TagCommand.MESSAGE_BATCH_SUCCESS, tagToAdd.tagName,
                eligibleStudent.getName(), alreadyTaggedStudent.getName(), studentAtTagLimit.getName());

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_batchNoEligibleStudents_returnsNoChangeMessage() {
        Tag tagToAdd = new Tag("friends");
        Person alreadyTaggedStudent = model.getFilteredPersonList().get(0);
        Person originalSecondStudent = model.getFilteredPersonList().get(1);
        Person studentAtTagLimit = new PersonBuilder(originalSecondStudent)
                .withTags("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")
                .build();
        model.setPerson(originalSecondStudent, studentAtTagLimit);
        TagCommand tagCommand = new TagCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), tagToAdd);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expectedMessage = String.format(TagCommand.MESSAGE_BATCH_NO_CHANGE, tagToAdd.tagName,
                alreadyTaggedStudent.getName(), studentAtTagLimit.getName());

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_batchFilteredList_usesDisplayedIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Tag tagToAdd = new Tag("math-2026");
        TagCommand tagCommand = new TagCommand(List.of(INDEX_FIRST_PERSON), tagToAdd);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        expectedModel.setPerson(personToTag, addTag(personToTag, tagToAdd));
        String expectedMessage = String.format(TagCommand.MESSAGE_BATCH_SUCCESS, tagToAdd.tagName,
                personToTag.getName(), TagCommand.MESSAGE_NO_STUDENTS, TagCommand.MESSAGE_NO_STUDENTS);

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_batchInvalidIndex_failureAndDoesNotModifyStudents() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        TagCommand tagCommand = new TagCommand(
                List.of(INDEX_FIRST_PERSON, outOfBoundsIndex), new Tag("math-2026"));

        assertCommandFailure(tagCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
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
    public void execute_existingPersonAboveTagLimit_failure() {
        Person legacyPerson = new PersonBuilder()
                .withTags("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                        "eleven")
                .build();
        model = new ModelManager(new AddressBookBuilder().withPerson(legacyPerson).build(), new UserPrefs());
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("twelve"));

        assertCommandFailure(tagCommand, model, Tag.MESSAGE_TAG_LIMIT);
    }

    @Test
    public void execute_addTag_preservesStudentDetails() {
        Person personToTag = createStudentWithDetails(getTagSet("friends"));
        model = new ModelManager(new AddressBookBuilder().withPerson(personToTag).build(), new UserPrefs());
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, getTagSet("exam-prep"));
        Person taggedPerson = createStudentWithDetails(getTagSet("friends", "exam-prep"));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToTag, taggedPerson);
        String expectedMessage = String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS,
                personToTag.getName(), "exam-prep");

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
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

        TagCommand batchCommand = new TagCommand(
                List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), new Tag("exam-prep"));
        assertTrue(batchCommand.equals(new TagCommand(
                List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), new Tag("exam-prep"))));
        assertFalse(batchCommand.equals(new TagCommand(
                List.of(INDEX_SECOND_PERSON, INDEX_FIRST_PERSON), new Tag("exam-prep"))));
        assertFalse(batchCommand.equals(new TagCommand(
                List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), new Tag("urgent"))));
        assertFalse(new TagCommand(List.of(INDEX_FIRST_PERSON), new Tag("exam-prep"))
                .equals(new TagCommand(INDEX_FIRST_PERSON, getTagSet("exam-prep"))));
    }

    @Test
    public void batchConstructor_emptyIndexes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TagCommand(List.of(), new Tag("math-2026")));
    }

    @Test
    public void batchConstructor_duplicateIndexes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TagCommand(List.of(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON), new Tag("math-2026")));
    }

    @Test
    public void toStringMethod() {
        Set<Tag> tagsToAdd = getTagSet("exam-prep");
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, tagsToAdd);
        String expected = TagCommand.class.getCanonicalName() + "{targetIndexes=[" + INDEX_FIRST_PERSON
                + "], tagsToAdd=" + tagsToAdd + ", isBatchCommand=false}";

        assertEquals(expected, tagCommand.toString());
    }

    private static Set<Tag> getTagSet(String... tagNames) {
        Set<Tag> tags = new LinkedHashSet<>();
        List.of(tagNames).stream().map(Tag::new).forEach(tags::add);
        return tags;
    }

    private static Person addTag(Person person, Tag tag) {
        Set<Tag> updatedTags = new LinkedHashSet<>(person.getTags());
        updatedTags.add(tag);
        return person.withTags(updatedTags);
    }

    private static Person createStudentWithDetails(Set<Tag> tags) {
        Person basePerson = new PersonBuilder().build();
        Lesson lesson = new Lesson(new Subject("Mathematics"), new Cost("40"),
                new LessonTiming("Monday1800-1900"));
        return new Person(basePerson.getName(), basePerson.getPhone(), basePerson.getEmail(),
                basePerson.getAddress(), tags, Set.of(lesson),
                Set.of(new seedu.address.model.person.Subject("Mathematics")),
                Optional.of(new seedu.address.model.person.Cost("40")),
                Optional.of(new seedu.address.model.person.Lesson("Monday1800")));
    }
}
