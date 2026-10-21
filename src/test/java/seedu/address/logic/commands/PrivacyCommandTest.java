package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.TypicalPersons;

public class PrivacyCommandTest {

    private final Model model = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_enablePrivacyMode_success() {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPrivacyModeEnabled(true);

        assertCommandSuccess(new PrivacyCommand(true), model,
                PrivacyCommand.MESSAGE_PRIVACY_MODE_ENABLED, expectedModel);
        assertTrue(model.isPrivacyModeEnabled());
    }

    @Test
    public void execute_disablePrivacyMode_success() {
        model.setPrivacyModeEnabled(true);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new PrivacyCommand(false), model,
                PrivacyCommand.MESSAGE_PRIVACY_MODE_DISABLED, expectedModel);
        assertFalse(model.isPrivacyModeEnabled());
    }

    @Test
    public void equals() {
        PrivacyCommand enableCommand = new PrivacyCommand(true);

        assertTrue(enableCommand.equals(new PrivacyCommand(true)));
        assertTrue(enableCommand.equals(enableCommand));
        assertFalse(enableCommand.equals(new PrivacyCommand(false)));
        assertFalse(enableCommand.equals(null));
        assertFalse(enableCommand.equals(new ClearCommand()));
    }

    @Test
    public void hashCode_sameMode_sameHashCode() {
        assertEquals(new PrivacyCommand(true).hashCode(), new PrivacyCommand(true).hashCode());
    }
}
