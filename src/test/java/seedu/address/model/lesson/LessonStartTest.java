package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LessonStartTest {

    @Test
    public void isValidLessonStart_strictTwentyFourHourTime() {
        assertTrue(LessonStart.isValidLessonStart("Monday0000"));
        assertTrue(LessonStart.isValidLessonStart("Sunday2359"));
        assertFalse(LessonStart.isValidLessonStart("Monday2400"));
        assertFalse(LessonStart.isValidLessonStart("Monday1260"));
    }

    @Test
    public void matches_comparesDayAndStartTime() {
        LessonStart lessonStart = new LessonStart("Monday1800");

        assertTrue(lessonStart.matches(new LessonTiming("Monday1800-1930")));
        assertFalse(lessonStart.matches(new LessonTiming("Monday1830-1930")));
        assertFalse(lessonStart.matches(new LessonTiming("Tuesday1800-1930")));
    }

    @Test
    public void equals() {
        LessonStart lessonStart = new LessonStart("Monday1800");
        LessonStart copy = new LessonStart("monday1800");

        assertEquals(lessonStart, lessonStart);
        assertEquals(lessonStart, copy);
        assertEquals(lessonStart.hashCode(), copy.hashCode());
        assertEquals("MONDAY1800", lessonStart.toString());
        assertNotEquals(lessonStart, new LessonStart("Monday1830"));
        assertNotEquals(lessonStart, "Monday1800");
    }
}
