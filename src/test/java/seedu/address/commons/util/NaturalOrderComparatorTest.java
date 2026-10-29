package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class NaturalOrderComparatorTest {
    @Test
    public void compare_numericAndTextTokens_ordersConsistently() {
        var comparator = NaturalOrderComparator.INSTANCE;
        for (String text : List.of("!", "A", ":")) {
            assertTrue(comparator.compare("2", text) == -comparator.compare(text, "2"));
        }
        assertTrue(comparator.compare("2", "A") < 0);
        assertTrue(comparator.compare("A", "2") > 0);
        assertTrue(comparator.compare("!", "2") < 0);
    }

    @Test
    public void compare_numericRuns_caseInsensitiveAndNoOverflow() {
        var comparator = NaturalOrderComparator.INSTANCE;
        assertEquals(List.of("student 1", "Student 2", "Student 10"),
                List.of("Student 10", "Student 2", "student 1").stream().sorted(comparator).toList());
        assertEquals(0, comparator.compare("Student 02", "student 2"));
        assertTrue(comparator.compare("Student 999999999999999999999999", "Student 1000000000000000000000000") < 0);
        assertTrue(comparator.compare("A2 B3", "a2 B10") < 0);
        assertTrue(comparator.compare("A", "A2") < 0);
        assertTrue(comparator.compare("A2", "A") > 0);
        assertEquals(0, comparator.compare("", ""));
    }
}
