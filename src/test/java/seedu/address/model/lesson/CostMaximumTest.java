package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CostMaximumTest {

    @Test
    public void isValidCost_maximumBoundary_returnsExpectedResult() {
        assertTrue(Cost.isValidCost("1000000"));
        assertTrue(Cost.isValidCost("1000000.00"));
        assertFalse(Cost.isValidCost("1000000.01"));
        assertFalse(Cost.isValidCost("9999999"));
        assertFalse(Cost.isValidCost("9".repeat(5000)));
    }

    @Test
    public void constructor_costAboveMaximum_throwsException() {
        assertEquals("1000000", new Cost("1000000.00").toString());
        assertThrows(IllegalArgumentException.class, () -> new Cost("1000000.01"));
        assertThrows(IllegalArgumentException.class, () -> new Cost("9".repeat(5000)));
    }
}
