package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests validation and value semantics of Subject.
 */
public class SubjectTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Subject(null));
        assertThrows(NullPointerException.class, () -> Subject.isValidSubject(null));
    }

    @Test
    public void validValues_acceptedAndPreserved() {
        for (String value : List.of("A", "Math", "CS2103T", "Computer Science")) {
            assertTrue(Subject.isValidSubject(value), value);
            assertEquals(value, new Subject(value).toString());
        }
    }

    @Test
    public void invalidValues_rejected() {
        for (String value : List.of("", " ", "  ", "Math!", "Math,", "CS-2103T")) {
            assertFalse(Subject.isValidSubject(value), value);
            assertThrows(IllegalArgumentException.class, () -> new Subject(value), value);
        }
    }

    @Test
    public void equalsAndHashCode_compareValues() {
        Subject value = new Subject("A");
        Subject equalValue = new Subject("A");
        assertEquals(value, equalValue);
        assertEquals(value.hashCode(), equalValue.hashCode());
        assertTrue(value.equals(value));
        assertFalse(value.equals(null));
        assertFalse(value.equals("A"));
        assertFalse(value.equals(new Subject("Physics")));
    }
}
