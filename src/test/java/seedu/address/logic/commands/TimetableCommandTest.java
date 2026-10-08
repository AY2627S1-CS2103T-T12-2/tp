package seedu.address.logic.commands;

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
}
