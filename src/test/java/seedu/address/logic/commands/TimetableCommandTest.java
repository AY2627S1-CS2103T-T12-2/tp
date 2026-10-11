package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class TimetableCommandTest {

    private final Model model = new ModelManager();
    private final Model expectedModel = new ModelManager();

    @Test
    public void execute_switchesToTimetableWithoutChangingModel() {
        CommandResult expectedResult = new CommandResult(
                TimetableCommand.MESSAGE_SUCCESS, CommandResult.ViewChange.TIMETABLE);
        assertCommandSuccess(new TimetableCommand(), model, expectedResult, expectedModel);
    }

    @Test
    public void equals() {
        TimetableCommand command = new TimetableCommand();

        assertEquals(command, command);
        assertEquals(command, new TimetableCommand());
        assertEquals(command.hashCode(), new TimetableCommand().hashCode());
        assertNotEquals(command, new ListCommand());
        assertNotEquals(command, null);
    }
}
