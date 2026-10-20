package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ClassIdTest {

    @Test
    public void constructor_normalizesCaseAndPadding() {
        assertEquals("C001", new ClassId(" c1 ").toString());
        assertEquals(new ClassId("C001"), new ClassId("c0001"));
        assertEquals(1, new ClassId("C001").getSequence());
    }

    @Test
    public void constructor_invalidValues_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new ClassId(null));
        assertThrows(IllegalArgumentException.class, () -> new ClassId(""));
        assertThrows(IllegalArgumentException.class, () -> new ClassId("A001"));
        assertThrows(IllegalArgumentException.class, () -> new ClassId("C000"));
        assertThrows(IllegalArgumentException.class, () -> new ClassId("C999999999999999999999"));
        assertThrows(IllegalArgumentException.class, () -> ClassId.fromSequence(0));
    }

    @Test
    public void validationComparisonAndEquality_workForBoundaryCases() {
        assertFalse(ClassId.isValidClassId(null));
        assertFalse(ClassId.isValidClassId("invalid"));
        assertFalse(ClassId.isValidClassId("C999999999999999999999"));
        assertTrue(ClassId.isValidClassId("c002"));
        assertTrue(new ClassId("C002").compareTo(new ClassId("C010")) < 0);
        assertEquals(new ClassId("C002"), new ClassId("c2"));
        assertEquals(new ClassId("C002").hashCode(), new ClassId("c2").hashCode());
        assertNotEquals(new ClassId("C002"), new ClassId("C003"));
        assertNotEquals(new ClassId("C002"), "C002");
    }
}
