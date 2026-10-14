package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LessonTest {

    private static final Lesson MATH = new Lesson(new Subject("Math"), new Cost("30"),
            new LessonTiming("Monday1800-1930"));
    private static final LessonTiming DIFFERENT_TIMING = new LessonTiming("Tuesday1800-1930");

    @Test
    public void isSameClass_lessonWithSameIdentity_returnsTrue() {
        Lesson differentCost = new Lesson(new Subject("math"), new Cost("40"), MATH.getTiming());

        assertTrue(MATH.isSameClass(MATH));
        assertTrue(MATH.isSameClass(differentCost));
    }

    @Test
    public void isSameClass_lessonWithDifferentIdentity_returnsFalse() {
        Lesson differentSubject = new Lesson(new Subject("Physics"), new Cost("30"), MATH.getTiming());
        Lesson differentTiming = new Lesson(MATH.getSubject(), new Cost("30"), DIFFERENT_TIMING);

        assertFalse(MATH.isSameClass((Lesson) null));
        assertFalse(MATH.isSameClass(differentSubject));
        assertFalse(MATH.isSameClass(differentTiming));
    }

    @Test
    public void isSameClass_subjectAndTiming_returnsExpectedResult() {
        assertTrue(MATH.isSameClass(new Subject("math"), MATH.getTiming()));
        assertFalse(MATH.isSameClass(new Subject("Physics"), MATH.getTiming()));
        assertFalse(MATH.isSameClass(MATH.getSubject(), DIFFERENT_TIMING));
    }

    @Test
    public void equals_sameObjectOrProperties_returnsTrue() {
        Lesson copy = new Lesson(new Subject("Math"), new Cost("30.00"),
                new LessonTiming("Monday1800-1930"));

        assertTrue(MATH.equals(MATH));
        assertTrue(MATH.equals(copy));
        assertEquals(MATH.hashCode(), copy.hashCode());
    }

    @Test
    public void equals_differentPropertyOrType_returnsFalse() {
        Lesson differentSubject = new Lesson(new Subject("Physics"), new Cost("30"), MATH.getTiming());
        Lesson differentCost = new Lesson(MATH.getSubject(), new Cost("40"), MATH.getTiming());
        Lesson differentTiming = new Lesson(MATH.getSubject(), MATH.getCost(), DIFFERENT_TIMING);

        assertNotEquals(MATH, differentSubject);
        assertNotEquals(MATH, differentCost);
        assertNotEquals(MATH, differentTiming);
        assertNotEquals(MATH, "Math");
        assertNotEquals(MATH, null);
    }

    @Test
    public void cost_equivalentDecimalValues_areEqual() {
        assertEquals(new Cost("30"), new Cost("30.00"));
    }
}
