package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StudentIdTest {

    private static final String ID = "S-00000000-0000-0000-0000-000000000001";

    @Test
    public void constructorAndValidation_handleValidAndInvalidValues() {
        StudentId studentId = new StudentId(ID.toLowerCase());

        assertEquals(ID, studentId.toString());
        assertTrue(StudentId.isValidStudentId(ID));
        assertTrue(StudentId.isValidStudentId("  " + ID.toLowerCase() + "  "));
        assertFalse(StudentId.isValidStudentId(null));
        assertFalse(StudentId.isValidStudentId("S-123"));
        assertThrows(NullPointerException.class, () -> new StudentId(null));
        assertThrows(IllegalArgumentException.class, () -> new StudentId("S-123"));
        assertTrue(StudentId.isValidStudentId(StudentId.generate().toString()));
    }

    @Test
    public void equalsAndHashCode_useNormalizedValue() {
        StudentId studentId = new StudentId(ID);
        StudentId copy = new StudentId(ID.toLowerCase());
        StudentId other = new StudentId("S-00000000-0000-0000-0000-000000000002");

        assertEquals(studentId, studentId);
        assertEquals(studentId, copy);
        assertEquals(studentId.hashCode(), copy.hashCode());
        assertNotEquals(studentId, other);
        assertNotEquals(studentId, null);
        assertNotEquals(studentId, ID);
    }
}
