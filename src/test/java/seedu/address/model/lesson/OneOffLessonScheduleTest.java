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

public class OneOffLessonScheduleTest {

    @Test
    public void isValidOneOffLessonSchedule_validAndInvalidValues_returnsExpectedResult() {
        assertTrue(OneOffLessonSchedule.isValidOneOffLessonSchedule("2026-10-20T1800-1930"));
        assertTrue(OneOffLessonSchedule.isValidOneOffLessonSchedule("2024-02-29T0900-1030"));
        assertFalse(OneOffLessonSchedule.isValidOneOffLessonSchedule(null));
        assertFalse(OneOffLessonSchedule.isValidOneOffLessonSchedule("2025-02-29T0900-1030"));
        assertFalse(OneOffLessonSchedule.isValidOneOffLessonSchedule("20-10-2026T1800-1930"));
        assertFalse(OneOffLessonSchedule.isValidOneOffLessonSchedule("2026-10-20T1930-1800"));
        assertFalse(OneOffLessonSchedule.isValidOneOffLessonSchedule("2026-10-20T1800-1800"));
        assertFalse(OneOffLessonSchedule.isValidOneOffLessonSchedule("2026-10-20T2400-2500"));
    }

    @Test
    public void constructor_invalidValue_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new OneOffLessonSchedule(null));
        assertThrows(IllegalArgumentException.class, () -> new OneOffLessonSchedule("2026-02-29T1800-1930"));
    }

    @Test
    public void accessorsAndRepresentations_returnExpectedValues() {
        OneOffLessonSchedule schedule = new OneOffLessonSchedule("2026-10-20T1800-1930");

        assertEquals(ScheduleType.ONE_OFF, schedule.getType());
        assertEquals(DayOfWeek.TUESDAY, schedule.getDay());
        assertEquals(LocalDate.of(2026, 10, 20), schedule.getDate().orElseThrow());
        assertEquals(LocalTime.of(18, 0), schedule.getStartTime());
        assertEquals(LocalTime.of(19, 30), schedule.getEndTime());
        assertTrue(schedule.occursOn(LocalDate.of(2026, 10, 20)));
        assertFalse(schedule.occursOn(LocalDate.of(2026, 10, 27)));
        assertEquals("2026-10-20T1800-1930", schedule.toStorageString());
        assertEquals("20 Oct 2026 18:00-19:30", schedule.toString());
    }

    @Test
    public void overlapsWith_allScheduleCombinations_returnsExpectedResult() {
        OneOffLessonSchedule schedule = new OneOffLessonSchedule("2026-10-20T1800-1930");

        assertTrue(schedule.overlapsWith(new OneOffLessonSchedule("2026-10-20T1900-2000")));
        assertFalse(schedule.overlapsWith(new OneOffLessonSchedule("2026-10-27T1900-2000")));
        assertFalse(schedule.overlapsWith(new OneOffLessonSchedule("2026-10-20T1930-2000")));
        assertTrue(schedule.overlapsWith(new LessonTiming("Tuesday1900-2000")));
        assertTrue(new LessonTiming("Tuesday1900-2000").overlapsWith(schedule));
        assertFalse(schedule.overlapsWith(new LessonTiming("Monday1900-2000")));
        assertThrows(NullPointerException.class, () -> schedule.overlapsWith(null));
    }

    @Test
    public void compareToAndEquals_useDateAndTimes() {
        OneOffLessonSchedule schedule = new OneOffLessonSchedule("2026-10-20T1800-1930");
        OneOffLessonSchedule copy = new OneOffLessonSchedule("2026-10-20T1800-1930");

        assertEquals(schedule, copy);
        assertEquals(schedule.hashCode(), copy.hashCode());
        assertTrue(schedule.compareTo(new OneOffLessonSchedule("2026-10-20T1800-2000")) < 0);
        assertEquals(0, schedule.compareTo(copy));
        assertTrue(schedule.compareTo(new OneOffLessonSchedule("2026-10-20T1900-2000")) < 0);
        assertTrue(schedule.compareTo(new OneOffLessonSchedule("2026-10-21T0900-1000")) < 0);
        assertNotEquals(schedule, new OneOffLessonSchedule("2026-10-20T1800-2000"));
        assertNotEquals(schedule, "2026-10-20T1800-1930");
    }
}
