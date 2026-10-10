package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

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

public class ClassCommandsIntegrationTest {

    private static final Lesson LESSON = new Lesson(new Subject("Math"), new Cost("30"),
            new LessonTiming("Monday1800-1930"));
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void addClass_sameClassCanBelongToMultipleStudents() throws CommandException {
        new AddClassCommand(Index.fromOneBased(1), LESSON).execute(model);
        new AddClassCommand(Index.fromOneBased(2), LESSON).execute(model);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void findByClassStart_listsStudentsWithClass() throws CommandException {
        new AddClassCommand(Index.fromOneBased(1), LESSON).execute(model);
        new FindCommand(new LessonStart("Monday1800")).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void addClass_sameClassWithDifferentCost_failure() throws CommandException {
        new AddClassCommand(Index.fromOneBased(1), LESSON).execute(model);
        Lesson conflictingLesson = new Lesson(new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"));

        assertCommandFailure(new AddClassCommand(Index.fromOneBased(2), conflictingLesson), model,
                AddClassCommand.MESSAGE_CLASS_COST_CONFLICT);
    }

    @Test
    public void addClass_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new AddClassCommand(invalidIndex, LESSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void addClass_sameClassForSameStudent_failure() throws CommandException {
        AddClassCommand command = new AddClassCommand(Index.fromOneBased(1), LESSON);
        command.execute(model);

        assertCommandFailure(command, model, AddClassCommand.MESSAGE_DUPLICATE_CLASS);
    }

    @Test
    public void addClassCommand_equals() {
        AddClassCommand command = new AddClassCommand(Index.fromOneBased(1), LESSON);
        Lesson differentLesson = new Lesson(new Subject("Physics"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));

        assertEquals(command, command);
        assertEquals(command, new AddClassCommand(Index.fromOneBased(1), LESSON));
        assertNotEquals(command, new AddClassCommand(Index.fromOneBased(2), LESSON));
        assertNotEquals(command, new AddClassCommand(Index.fromOneBased(1), differentLesson));
        assertNotEquals(command, null);
    }

    @Test
    public void timetable_switchesViewWithoutChangingModel() throws CommandException {
        new AddClassCommand(Index.fromOneBased(1), LESSON).execute(model);
        new AddClassCommand(Index.fromOneBased(2), LESSON).execute(model);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        CommandResult result = new TimetableCommand().execute(model);

        assertEquals(TimetableCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(CommandResult.ViewChange.TIMETABLE, result.getViewChange());
        assertEquals(expectedModel, model);
    }
}
