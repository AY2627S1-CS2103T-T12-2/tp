package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        AddressBook expectedAddressBook = new AddressBook();
        expectedAddressBook.setNextClassSequence(model.getAddressBook().getNextClassSequence());
        expectedModel.setAddressBook(expectedAddressBook);

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_existingClass_preservesNextClassId() throws CommandException {
        Model model = new ModelManager();
        new ClassAddCommand(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930")).execute(model);

        new ClearCommand().execute(model);

        assertEquals(new ClassId("C002"), model.allocateClassId());
    }

    @Test
    public void execute_enrolledStudentAndClass_removesAllReportedData() throws CommandException {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        ClassId classId = model.allocateClassId();
        model.addTuitionClass(new TuitionClass(classId, new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930")));
        new ClassEnrolCommand(Index.fromOneBased(1), classId).execute(model);

        String feedback = new ClearCommand().execute(model).getFeedbackToUser();

        assertEquals("All students, classes, and enrolments have been cleared.", feedback);
        assertEquals(0, model.getAddressBook().getPersonList().size());
        assertEquals(0, model.getTuitionClassList().size());
    }

}
