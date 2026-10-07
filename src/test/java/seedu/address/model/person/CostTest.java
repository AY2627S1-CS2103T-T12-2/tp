package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests validation and value semantics of Cost.
 */
public class CostTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Cost(null));
        assertThrows(NullPointerException.class, () -> Cost.isValidCost(null));
    }

    @Test
    public void validValues_acceptedAndPreserved() {
        for (String value : List.of("1", "30", "1000")) {
            assertTrue(Cost.isValidCost(value), value);
            assertEquals(value, new Cost(value).toString());
        }
    }

    @Test
    public void invalidValues_rejected() {
        for (String value : List.of("", " ", "0", "00", "030", "-30", "30.50", "abc", "3 0")) {
            assertFalse(Cost.isValidCost(value), value);
            assertThrows(IllegalArgumentException.class, () -> new Cost(value), value);
        }
    }

    @Test
    public void equalsAndHashCode_compareValues() {
        Cost value = new Cost("1");
        Cost equalValue = new Cost("1");
        assertEquals(value, equalValue);
        assertEquals(value.hashCode(), equalValue.hashCode());
        assertTrue(value.equals(value));
        assertFalse(value.equals(null));
        assertFalse(value.equals("1"));
        assertFalse(value.equals(new Cost("40")));
    }
}
