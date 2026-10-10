package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;

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

    @Test
    public void lessonTiming_accessorsAndRepresentations_returnExpectedValues() {
        LessonTiming timing = new LessonTiming("monday1800-1930");

        assertEquals(DayOfWeek.MONDAY, timing.getDay());
        assertEquals(LocalTime.of(18, 0), timing.getStartTime());
        assertEquals(LocalTime.of(19, 30), timing.getEndTime());
        assertTrue(timing.startsAt(DayOfWeek.MONDAY, LocalTime.of(18, 0)));
        assertFalse(timing.startsAt(DayOfWeek.MONDAY, LocalTime.of(18, 30)));
        assertFalse(timing.startsAt(DayOfWeek.TUESDAY, LocalTime.of(18, 0)));
        assertEquals("Monday 18:00-19:30", timing.toString());
        assertEquals("Monday1800-1930", timing.toStorageString());
    }

    @Test
    public void compareTo_ordersByDayThenStartTime() {
        LessonTiming mondayMorning = new LessonTiming("Monday0900-1000");
        LessonTiming mondayEvening = new LessonTiming("Monday1800-1930");
        LessonTiming tuesdayMorning = new LessonTiming("Tuesday0900-1000");

        assertTrue(mondayMorning.compareTo(mondayEvening) < 0);
        assertTrue(mondayEvening.compareTo(tuesdayMorning) < 0);
        assertEquals(0, mondayMorning.compareTo(new LessonTiming("Monday0900-1100")));
    }

    @Test
    public void equals() {
        LessonTiming timing = new LessonTiming("Monday1800-1930");
        LessonTiming copy = new LessonTiming("monday1800-1930");

        assertEquals(timing, timing);
        assertEquals(timing, copy);
        assertEquals(timing.hashCode(), copy.hashCode());
        assertNotEquals(timing, new LessonTiming("Monday1800-2000"));
        assertNotEquals(timing, "Monday1800-1930");
    }
}
