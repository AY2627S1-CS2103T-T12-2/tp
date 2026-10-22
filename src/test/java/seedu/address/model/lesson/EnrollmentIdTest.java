package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EnrollmentIdTest {

    private static final String ID = "E-00000000-0000-0000-0000-000000000001";

    @Test
    public void constructorAndValidation_handleValidAndInvalidValues() {
        EnrollmentId enrollmentId = new EnrollmentId(ID.toLowerCase());

        assertEquals(ID, enrollmentId.toString());
        assertTrue(EnrollmentId.isValidEnrollmentId(ID));
        assertTrue(EnrollmentId.isValidEnrollmentId("  " + ID.toLowerCase() + "  "));
        assertFalse(EnrollmentId.isValidEnrollmentId(null));
        assertFalse(EnrollmentId.isValidEnrollmentId("E-123"));
        assertThrows(NullPointerException.class, () -> new EnrollmentId(null));
        assertThrows(IllegalArgumentException.class, () -> new EnrollmentId("E-123"));
        assertTrue(EnrollmentId.isValidEnrollmentId(EnrollmentId.generate().toString()));
    }

    @Test
    public void equalsAndHashCode_useNormalizedValue() {
        EnrollmentId enrollmentId = new EnrollmentId(ID);
        EnrollmentId copy = new EnrollmentId(ID.toLowerCase());
        EnrollmentId other = new EnrollmentId("E-00000000-0000-0000-0000-000000000002");

        assertEquals(enrollmentId, enrollmentId);
        assertEquals(enrollmentId, copy);
        assertEquals(enrollmentId.hashCode(), copy.hashCode());
        assertNotEquals(enrollmentId, other);
        assertNotEquals(enrollmentId, null);
        assertNotEquals(enrollmentId, ID);
    }
}
