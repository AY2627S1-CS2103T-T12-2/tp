package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests validation and value semantics of Lesson.
 */
public class LessonTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Lesson(null));
        assertThrows(NullPointerException.class, () -> Lesson.isValidLesson(null));
    }

    @Test
    public void validValues_acceptedAndPreserved() {
        for (String value : List.of("Monday0000", "Tuesday1800", "Wednesday1200", "Thursday0900", "Friday2359",
                "Saturday1000", "Sunday2359", "monday1800", "MONDAY1800")) {
            assertTrue(Lesson.isValidLesson(value), value);
            assertEquals(value, new Lesson(value).toString());
        }
    }

    @Test
    public void invalidValues_rejected() {
        for (String value : List.of("", " ", "Funday1800", "Monday2400", "Monday1860", "Monday180",
                "Monday18:00", "Monday 1800")) {
            assertFalse(Lesson.isValidLesson(value), value);
            assertThrows(IllegalArgumentException.class, () -> new Lesson(value), value);
        }
    }

    @Test
    public void equalsAndHashCode_compareValues() {
        Lesson value = new Lesson("Monday0000");
        Lesson equalValue = new Lesson("Monday0000");
        assertEquals(value, equalValue);
        assertEquals(value.hashCode(), equalValue.hashCode());
        assertTrue(value.equals(value));
        assertFalse(value.equals(null));
        assertFalse(value.equals("Monday0000"));
        assertFalse(value.equals(new Lesson("Sunday0000")));
    }
}
