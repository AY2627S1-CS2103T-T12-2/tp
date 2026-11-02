package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassMode;

/** Tests class-detail rendering for both privacy states. */
public class ClassDetailsFormatterTest {

    @Test
    public void constructor_utilityClass_isPrivate() throws Exception {
        var constructor = ClassDetailsFormatter.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }

    @Test
    public void format_privacyDisabled_displaysAllFields() {
        ClassDetails details = details();

        assertEquals("Mode: Online | Link: https://example.com/math | Location: Room 2 | Notes: Bring notes",
                ClassDetailsFormatter.format(details, false));
        assertEquals("Link: https://example.com/math | Location: Room 2 | Notes: Bring notes",
                ClassDetailsFormatter.formatSupplementary(details, false));
    }

    @Test
    public void format_privacyEnabledMasksLinkAndLocation_butKeepsModeAndNotes() {
        ClassDetails details = details();

        assertEquals("Mode: Online | Link: ******* | Location: ******* | Notes: Bring notes",
                ClassDetailsFormatter.format(details, true));
        assertEquals("Link: ******* | Location: ******* | Notes: Bring notes",
                ClassDetailsFormatter.formatSupplementary(details, true));
    }

    @Test
    public void format_emptyOptionalFields_omitsThemInBothPrivacyStates() {
        ClassDetails details = new ClassDetails(ClassMode.OFFLINE, "", "", "");

        assertEquals("Mode: Offline", ClassDetailsFormatter.format(details, false));
        assertEquals("Mode: Offline", ClassDetailsFormatter.format(details, true));
        assertEquals("", ClassDetailsFormatter.formatSupplementary(details, true));
    }

    private static ClassDetails details() {
        return new ClassDetails(ClassMode.ONLINE, "https://example.com/math", "Room 2", "Bring notes");
    }
}
