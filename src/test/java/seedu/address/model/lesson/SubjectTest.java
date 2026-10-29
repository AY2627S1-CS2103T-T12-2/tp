package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SubjectTest {

    @Test
    public void constructor_repeatedSpaces_normalizesSubject() {
        Subject subject = new Subject("Computer   Science");

        assertEquals("Computer Science", subject.value);
        assertEquals("Computer Science", subject.toString());
        assertEquals(new Subject("Computer Science"), subject);
        assertEquals(new Subject("Computer Science").hashCode(), subject.hashCode());
    }

    @Test
    public void constructor_nullOrInvalidSubject_throwsException() {
        assertThrows(NullPointerException.class, () -> new Subject(null));
        assertThrows(IllegalArgumentException.class, () -> new Subject(""));
        assertThrows(IllegalArgumentException.class, () -> new Subject(" Computer Science"));
        assertThrows(IllegalArgumentException.class, () -> new Subject("Computer!"));
    }

    @Test
    public void validationAndEquality_handleBoundaryCases() {
        Subject computerScience = new Subject("Computer Science");

        assertTrue(Subject.isValidSubject("Computer Science"));
        assertFalse(Subject.isValidSubject(""));
        assertEquals(computerScience, computerScience);
        assertEquals(computerScience, new Subject("computer science"));
        assertNotEquals(computerScience, new Subject("Computer Studies"));
        assertNotEquals(computerScience, "Computer Science");
        assertNotEquals(computerScience, null);
    }
}
