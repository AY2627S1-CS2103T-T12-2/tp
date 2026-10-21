package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassMode;

public class ClassDetailsUpdateTest {

    private static final ClassDetails ONLINE = new ClassDetails(ClassMode.ONLINE,
            "https://example.com/math", "Room 2", "Bring notes");

    @Test
    public void omittedFields_inheritAllExistingDetails() {
        assertTrue(ClassDetailsUpdate.NONE.isEmpty());
        assertEquals(ONLINE, ClassDetailsUpdate.NONE.applyTo(ONLINE));
    }

    @Test
    public void explicitEmptyFields_clearOnlySuppliedValues() {
        ClassDetailsUpdate clearLink = new ClassDetailsUpdate(Optional.empty(), Optional.of(""),
                Optional.empty(), Optional.empty());
        ClassDetailsUpdate clearLocation = new ClassDetailsUpdate(Optional.empty(), Optional.empty(),
                Optional.of(""), Optional.empty());
        ClassDetailsUpdate clearNotes = new ClassDetailsUpdate(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(""));
        assertFalse(clearLink.isEmpty());
        assertFalse(clearLocation.isEmpty());
        assertFalse(clearNotes.isEmpty());
        assertEquals(new ClassDetails(ClassMode.ONLINE, "", "Room 2", "Bring notes"), clearLink.applyTo(ONLINE));
        assertEquals(new ClassDetails(ClassMode.ONLINE, ONLINE.meetingLink(), "", "Bring notes"),
                clearLocation.applyTo(ONLINE));
        assertEquals(new ClassDetails(ClassMode.ONLINE, ONLINE.meetingLink(), "Room 2", ""),
                clearNotes.applyTo(ONLINE));
    }

    @Test
    public void offlineMode_discardsInheritedLinkAndKeepsOtherFields() {
        ClassDetailsUpdate offline = new ClassDetailsUpdate(Optional.of(ClassMode.OFFLINE), Optional.empty(),
                Optional.empty(), Optional.empty());
        assertFalse(offline.isEmpty());
        assertEquals(new ClassDetails(ClassMode.OFFLINE, "", "Room 2", "Bring notes"), offline.applyTo(ONLINE));
    }
}
