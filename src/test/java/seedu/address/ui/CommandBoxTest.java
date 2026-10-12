package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandBoxTest {

    @Test
    public void getCommandSuggestion_findInputs_returnsExpectedSuggestion() {
        assertEquals("", CommandBox.getCommandSuggestion(null));
        assertEquals("", CommandBox.getCommandSuggestion(""));
        assertEquals("find", CommandBox.getCommandSuggestion("fin"));
        assertEquals("find KEYWORD | find t/TAG (e.g. priority) | find c/DAY TIME (e.g. Monday 1800)",
                CommandBox.getCommandSuggestion("find"));
        assertEquals("find KEYWORD | find t/TAG (e.g. priority) | find c/DAY TIME (e.g. Monday 1800)",
                CommandBox.getCommandSuggestion("find "));
        assertEquals("find t/TAG (e.g. priority)", CommandBox.getCommandSuggestion("find t/"));
        assertEquals("find c/DAY TIME (e.g. Monday 1800; time uses HHmm)",
                CommandBox.getCommandSuggestion("find c/"));
        assertEquals("t/TAG (e.g. priority)", CommandBox.getCommandSuggestion("t/"));
        assertEquals("c/DAY TIME (e.g. Monday 1800; time uses HHmm)", CommandBox.getCommandSuggestion("c/"));
        assertEquals("", CommandBox.getCommandSuggestion("list"));
    }

    @Test
    public void isFindCommand() {
        assertTrue(CommandBox.isFindCommand("find"));
        assertTrue(CommandBox.isFindCommand(" find Alex "));
        assertFalse(CommandBox.isFindCommand("findAlex"));
        assertFalse(CommandBox.isFindCommand("list"));
    }
}
