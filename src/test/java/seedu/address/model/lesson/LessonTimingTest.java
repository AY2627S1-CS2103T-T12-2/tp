package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LessonTimingTest {

    @Test
    public void isValidLessonTiming() {
        assertTrue(LessonTiming.isValidLessonTiming("Monday1800-1930"));
        assertTrue(LessonTiming.isValidLessonTiming("friday0900-1030"));
        assertFalse(LessonTiming.isValidLessonTiming("Monday1800"));
        assertFalse(LessonTiming.isValidLessonTiming("Monday1930-1800"));
        assertFalse(LessonTiming.isValidLessonTiming("Monday2500-2600"));
        assertFalse(LessonTiming.isValidLessonTiming("Monday2400-0100"));
        assertFalse(LessonTiming.isValidLessonTiming("Monday1800-2400"));
    }
}
