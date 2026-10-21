package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TuitionClassTest {

    private static final TuitionClass MATH = tuitionClass("C001", "Math", "30", "Monday1800-1930");

    @Test
    public void accessorsAndRelationshipChecks_returnExpectedValues() {
        assertEquals(new ClassId("C001"), MATH.getClassId());
        assertEquals(new Subject("Math"), MATH.getSubject());
        assertEquals(new Cost("30"), MATH.getCost());
        assertEquals(new LessonTiming("Monday1800-1930"), MATH.getSchedule());
        assertTrue(MATH.hasSameDetails(tuitionClass("C009", "math", "99", "Monday1800-1930")));
        assertFalse(MATH.hasSameDetails(null));
        assertFalse(MATH.hasSameDetails(tuitionClass("C009", "Physics", "30", "Monday1800-1930")));
        assertFalse(MATH.hasSameDetails(tuitionClass("C009", "Math", "30", "Tuesday1800-1930")));
        assertTrue(MATH.overlapsWith(tuitionClass("C002", "Physics", "30", "Monday1900-2000")));
        assertFalse(MATH.overlapsWith(null));
    }

    @Test
    public void equalityHashCodeAndToString_coverAllFields() {
        TuitionClass copy = tuitionClass("C001", "Math", "30.00", "Monday1800-1930");
        assertEquals(MATH, MATH);
        assertEquals(MATH, copy);
        assertEquals(MATH.hashCode(), copy.hashCode());
        assertNotEquals(MATH, tuitionClass("C002", "Math", "30", "Monday1800-1930"));
        assertNotEquals(MATH, tuitionClass("C001", "Physics", "30", "Monday1800-1930"));
        assertNotEquals(MATH, tuitionClass("C001", "Math", "31", "Monday1800-1930"));
        assertNotEquals(MATH, tuitionClass("C001", "Math", "30", "Tuesday1800-1930"));
        assertNotEquals(MATH, null);
        assertTrue(MATH.toString().contains("C001"));
    }

    private static TuitionClass tuitionClass(String id, String subject, String cost, String timing) {
        return new TuitionClass(new ClassId(id), new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
