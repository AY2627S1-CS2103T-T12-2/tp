package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class LessonScheduleComparatorTest {

    @Test
    public void compare_mixedSchedules_ordersWeeklyThenOneOffChronologically() {
        LessonSchedule mondayLate = new LessonTiming("Monday1800-1930");
        LessonSchedule mondayEarly = new LessonTiming("Monday0900-1000");
        LessonSchedule tuesday = new LessonTiming("Tuesday0900-1000");
        LessonSchedule earlierOneOff = new OneOffLessonSchedule("2026-10-20T1800-1930");
        LessonSchedule laterOneOff = new OneOffLessonSchedule("2026-10-21T0900-1000");
        List<LessonSchedule> schedules = new ArrayList<>(
                List.of(laterOneOff, tuesday, mondayLate, earlierOneOff, mondayEarly));

        schedules.sort(LessonScheduleComparator.INSTANCE);

        assertEquals(List.of(mondayEarly, mondayLate, tuesday, earlierOneOff, laterOneOff), schedules);
    }
}
