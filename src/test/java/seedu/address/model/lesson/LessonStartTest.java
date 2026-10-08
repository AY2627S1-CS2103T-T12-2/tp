package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
