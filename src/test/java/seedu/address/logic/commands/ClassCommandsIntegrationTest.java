package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStart;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

public class ClassCommandsIntegrationTest {

    private static final Lesson LESSON = new Lesson(new Subject("Math"), new Cost("30"),
            new LessonTiming("Monday1800-1930"));
    private static final Lesson OTHER_LESSON = new Lesson(new Subject("English"), new Cost("25"),
            new LessonTiming("Wednesday1600-1730"));
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void enrol_validClass_addsClassAndReturnsSuccessMessage() throws CommandException {
        Person originalPerson = model.getFilteredPersonList().get(0);

        CommandResult result = new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Person updatedPerson = model.getAddressBook().getPersonList().get(0);

        assertEquals(String.format(ClassEnrolCommand.MESSAGE_SUCCESS, originalPerson.getName(), LESSON),
                result.getFeedbackToUser());
        assertTrue(updatedPerson.getLessons().contains(LESSON));
        assertEquals(originalPerson.getName(), updatedPerson.getName());
        assertEquals(originalPerson.getPhone(), updatedPerson.getPhone());
        assertEquals(originalPerson.getEmail(), updatedPerson.getEmail());
        assertEquals(originalPerson.getAddress(), updatedPerson.getAddress());
        assertEquals(originalPerson.getTags(), updatedPerson.getTags());
    }

    @Test
    public void enrol_sameClassCanBelongToMultipleStudents() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void enrol_filteredList_usesDisplayedIndex() throws CommandException {
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);

        assertFalse(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void findByClassStart_listsStudentsWithClass() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        new FindCommand(new LessonStart("Monday1800")).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void enrol_sameClassWithDifferentCost_failure() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Lesson conflictingLesson = new Lesson(new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"));

        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(2), conflictingLesson), model,
                ClassEnrolCommand.MESSAGE_CLASS_COST_CONFLICT);
    }

    @Test
    public void enrol_overlappingClass_failure() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Lesson overlappingLesson = new Lesson(new Subject("Physics"), new Cost("40"),
                new LessonTiming("Monday1900-2000"));
        String expectedMessage = "Cannot add Physics (Monday 19:00-20:00) because it overlaps with:"
                + System.lineSeparator() + "- Math (Monday 18:00-19:30)"
                + System.lineSeparator() + "Choose a non-overlapping time.";

        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(2), overlappingLesson), model,
                expectedMessage);
    }

    @Test
    public void enrol_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ClassEnrolCommand(invalidIndex, LESSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void enrol_sameClassForSameStudent_failure() throws CommandException {
        ClassEnrolCommand command = new ClassEnrolCommand(Index.fromOneBased(1), LESSON);
        command.execute(model);

        assertCommandFailure(command, model, ClassEnrolCommand.MESSAGE_DUPLICATE_CLASS);
    }

    @Test
    public void enrolCommand_equals() {
        ClassEnrolCommand command = new ClassEnrolCommand(Index.fromOneBased(1), LESSON);
        Lesson differentLesson = new Lesson(new Subject("Physics"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));

        assertEquals(command, command);
        assertEquals(command, new ClassEnrolCommand(Index.fromOneBased(1), LESSON));
        assertNotEquals(command, new ClassEnrolCommand(Index.fromOneBased(2), LESSON));
        assertNotEquals(command, new ClassEnrolCommand(Index.fromOneBased(1), differentLesson));
        assertNotEquals(command, null);
    }

    @Test
    public void unenrol_existingClass_removesOnlySelectedStudent() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);

        CommandResult result = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming()).execute(model);

        assertEquals(String.format(ClassUnenrolCommand.MESSAGE_SUCCESS,
                model.getAddressBook().getPersonList().get(0).getName(), LESSON.getSubject(), LESSON.getTiming()),
                result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void unenrol_studentWithOtherClass_preservesOtherClass() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        new ClassUnenrolCommand(Index.fromOneBased(1),
                new Subject("mAtH"), LESSON.getTiming()).execute(model);

        assertEquals(Set.of(OTHER_LESSON), model.getAddressBook().getPersonList().get(0).getLessons());
    }

    @Test
    public void unenrol_filteredList_usesDisplayedIndex() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassUnenrolCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming()).execute(model);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertFalse(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void unenrol_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ClassUnenrolCommand(invalidIndex, LESSON.getSubject(), LESSON.getTiming()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void unenrol_studentNotEnrolled_failure() {
        ClassUnenrolCommand command = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming());

        assertCommandFailure(command, model, ClassUnenrolCommand.MESSAGE_NOT_ENROLLED);
    }

    @Test
    public void unenrolCommand_equals() {
        ClassUnenrolCommand command = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming());

        assertEquals(command, command);
        assertEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), new Subject("math"), LESSON.getTiming()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(2), LESSON.getSubject(), LESSON.getTiming()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), OTHER_LESSON.getSubject(), LESSON.getTiming()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), OTHER_LESSON.getTiming()));
        assertNotEquals(command, null);
    }

    @Test
    public void delete_existingClass_removesEveryEnrolmentIncludingFilteredOutStudents() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(1));

        CommandResult result = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getTiming()).execute(model);

        assertEquals(String.format(ClassDeleteCommand.MESSAGE_SUCCESS,
                LESSON.getSubject(), LESSON.getTiming(), 2, "students"), result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getLessons().contains(LESSON)));
    }

    @Test
    public void delete_onlyMatchingClass_preservesOtherClassesAndUnenrolledStudents() throws CommandException {
        Person untouchedPerson = model.getAddressBook().getPersonList().get(1);
        enrolStudentsInLesson(LESSON, 1);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        CommandResult result = new ClassDeleteCommand(
                new Subject("MATH"), LESSON.getTiming()).execute(model);

        assertEquals(String.format(ClassDeleteCommand.MESSAGE_SUCCESS,
                new Subject("MATH"), LESSON.getTiming(), 1, "student"), result.getFeedbackToUser());
        assertEquals(Set.of(OTHER_LESSON), model.getAddressBook().getPersonList().get(0).getLessons());
        assertEquals(untouchedPerson, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void delete_missingClass_failure() {
        ClassDeleteCommand command = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getTiming());

        assertCommandFailure(command, model, ClassDeleteCommand.MESSAGE_CLASS_NOT_FOUND);
    }

    @Test
    public void deleteCommand_equals() {
        ClassDeleteCommand command = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getTiming());

        assertEquals(command, command);
        assertEquals(command, new ClassDeleteCommand(new Subject("math"), LESSON.getTiming()));
        assertNotEquals(command, new ClassDeleteCommand(OTHER_LESSON.getSubject(), LESSON.getTiming()));
        assertNotEquals(command, new ClassDeleteCommand(LESSON.getSubject(), OTHER_LESSON.getTiming()));
        assertNotEquals(command, null);
    }

    @Test
    public void timetable_switchesViewWithoutChangingModel() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        CommandResult result = new TimetableCommand().execute(model);

        assertEquals(TimetableCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(CommandResult.ViewChange.TIMETABLE, result.getViewChange());
        assertEquals(expectedModel, model);
    }

    private void enrolStudentsInLesson(Lesson lesson, int... oneBasedIndexes) throws CommandException {
        for (int oneBasedIndex : oneBasedIndexes) {
            new ClassEnrolCommand(Index.fromOneBased(oneBasedIndex), lesson).execute(model);
        }
    }
}
