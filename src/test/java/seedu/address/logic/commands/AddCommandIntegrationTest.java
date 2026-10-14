package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_personWithOverlappingClass_throwsCommandException() {
        Lesson existingLesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson overlappingLesson = new Lesson(new Subject("Physics"), new Cost("40"),
                new LessonTiming("Monday1900-2000"));
        Person existingStudent = new PersonBuilder().withLessons(existingLesson).build();
        Person studentToAdd = new PersonBuilder().withName("Bob Choo").withPhone("91234567")
                .withEmail("bob@example.com").withLessons(overlappingLesson).build();
        model = new ModelManager(new AddressBookBuilder().withPerson(existingStudent).build(), new UserPrefs());
        String expectedMessage = "Cannot add Physics (Monday 19:00-20:00) because it overlaps with:"
                + System.lineSeparator() + "- Math (Monday 18:00-19:30)"
                + System.lineSeparator() + "Choose a non-overlapping time.";

        assertCommandFailure(new AddCommand(studentToAdd), model, expectedMessage);
    }

    @Test
    public void execute_personJoiningExistingClass_success() {
        Lesson existingLesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person existingStudent = new PersonBuilder().withLessons(existingLesson).build();
        Person studentToAdd = new PersonBuilder().withName("Bob Choo").withPhone("91234567")
                .withEmail("bob@example.com").withLessons(existingLesson).build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(existingStudent).build();
        model = new ModelManager(addressBook, new UserPrefs());
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        expectedModel.addPerson(studentToAdd);

        assertCommandSuccess(new AddCommand(studentToAdd), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(studentToAdd)), expectedModel);
    }

}
