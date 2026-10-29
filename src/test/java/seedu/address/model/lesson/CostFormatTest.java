package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CostFormatTest {

    @Test
    public void constructor_supportedNumericFormats_normalizesValue() {
        assertEquals("30", new Cost("030").toString());
        assertEquals("0.5", new Cost(".5").toString());
        assertEquals("0.5", new Cost("00.50").toString());
    }

    @Test
    public void isValidCost_numericBoundaries_returnsExpectedResult() {
        for (String value : List.of("0", "00", "030", ".5", ".50", "0.5", "30.00")) {
            assertTrue(Cost.isValidCost(value), value);
        }
        for (String value : List.of("", ".", "1.", "-.5", "+1", "1.234", "1e2")) {
            assertFalse(Cost.isValidCost(value), value);
            assertThrows(IllegalArgumentException.class, () -> new Cost(value), value);
        }
    }
}
