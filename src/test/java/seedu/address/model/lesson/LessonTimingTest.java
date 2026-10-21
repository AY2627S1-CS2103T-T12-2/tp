package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
        assertEquals(ScheduleType.WEEKLY, timing.getType());
        assertEquals(LocalTime.of(18, 0), timing.getStartTime());
        assertEquals(LocalTime.of(19, 30), timing.getEndTime());
        assertTrue(timing.occursOn(LocalDate.of(2026, 10, 19)));
        assertFalse(timing.occursOn(LocalDate.of(2026, 10, 20)));
        assertTrue(timing.startsAt(DayOfWeek.MONDAY, LocalTime.of(18, 0)));
        assertFalse(timing.startsAt(DayOfWeek.MONDAY, LocalTime.of(18, 30)));
        assertFalse(timing.startsAt(DayOfWeek.TUESDAY, LocalTime.of(18, 0)));
        assertEquals("Monday 18:00-19:30", timing.toString());
        assertEquals("Monday1800-1930", timing.toStorageString());
    }

    @Test
    public void compareTo_ordersByDayStartAndEndTime() {
        LessonTiming mondayMorning = new LessonTiming("Monday0900-1000");
        LessonTiming mondayEvening = new LessonTiming("Monday1800-1930");
        LessonTiming tuesdayMorning = new LessonTiming("Tuesday0900-1000");

        assertTrue(mondayMorning.compareTo(mondayEvening) < 0);
        assertTrue(mondayEvening.compareTo(tuesdayMorning) < 0);
        assertTrue(mondayMorning.compareTo(new LessonTiming("Monday0900-1100")) < 0);
        assertEquals(0, mondayMorning.compareTo(new LessonTiming("Monday0900-1000")));
    }

    @Test
    public void overlapsWith_variousTimings_returnsExpectedResult() {
        LessonTiming timing = new LessonTiming("Monday1800-1930");

        assertTrue(timing.overlapsWith(new LessonTiming("Monday1700-1830")));
        assertTrue(timing.overlapsWith(new LessonTiming("Monday1900-2000")));
        assertTrue(timing.overlapsWith(new LessonTiming("Monday1830-1900")));
        assertTrue(timing.overlapsWith(new LessonTiming("Monday1700-2000")));
        assertTrue(timing.overlapsWith(new LessonTiming("Monday1800-1930")));
        assertFalse(timing.overlapsWith(new LessonTiming("Monday1700-1800")));
        assertFalse(timing.overlapsWith(new LessonTiming("Monday1930-2000")));
        assertFalse(timing.overlapsWith(new LessonTiming("Tuesday1800-1930")));
    }

    @Test
    public void overlapsWith_null_throwsNullPointerException() {
        LessonTiming timing = new LessonTiming("Monday1800-1930");

        assertThrows(NullPointerException.class, () -> timing.overlapsWith(null));
    }

    @Test
    public void equals() {
        LessonTiming timing = new LessonTiming("Monday1800-1930");
        LessonTiming copy = new LessonTiming("monday1800-1930");

        assertEquals(timing, timing);
        assertEquals(timing, copy);
        assertEquals(timing.hashCode(), copy.hashCode());
        assertNotEquals(timing, new LessonTiming("Tuesday1800-1930"));
        assertNotEquals(timing, new LessonTiming("Monday1900-2000"));
        assertNotEquals(timing, new LessonTiming("Monday1800-2000"));
        assertNotEquals(timing, "Monday1800-1930");
    }
}
