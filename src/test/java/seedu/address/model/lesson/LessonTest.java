package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LessonTest {

    private static final Lesson MATH = new Lesson(new Subject("Math"), new Cost("30"),
            new LessonTiming("Monday1800-1930"));

    @Test
    public void equals_sameProperties_returnsTrue() {
        Lesson copy = new Lesson(new Subject("Math"), new Cost("30.00"),
                new LessonTiming("Monday1800-1930"));
        assertTrue(MATH.equals(copy));
    }

    @Test
    public void equals_differentSubject_returnsFalse() {
        Lesson other = new Lesson(new Subject("Physics"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        assertFalse(MATH.equals(other));
    }

    @Test
    public void cost_equivalentDecimalValues_areEqual() {
        assertEquals(new Cost("30"), new Cost("30.00"));
    }
}
